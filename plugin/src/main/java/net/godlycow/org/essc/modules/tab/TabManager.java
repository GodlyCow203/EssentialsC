package net.godlycow.org.essc.modules.tab;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.integration.bedrock.TeamNameUtil;
import net.godlycow.org.essc.server.software.ServerSoftware;
import net.godlycow.org.essc.util.LegacyColorConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TabManager implements Listener {

    private final EssentialsC plugin;
    private LuckPerms luckPerms;
    private boolean luckPermsEnabled;
    private boolean useLuckPermsTab;
    private TABHook tabHook;

    private final LegacyComponentSerializer legacyAmpersand = LegacyComponentSerializer.legacyAmpersand();

    private static final int TEAM_FIELD_MAX = 64;

    private final Map<UUID, TabState> stateCache = new ConcurrentHashMap<>();

    private record TabState(String prefix, String suffix, String nick, boolean afk) {}

    public TabManager(EssentialsC plugin) {
        this.plugin = plugin;
        reload();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startRefreshTask();
    }

    public void reload() {
        this.useLuckPermsTab = plugin.getConfigManager().isLuckPermsTabEnabled();
        if (useLuckPermsTab && plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                this.luckPerms = LuckPermsProvider.get();
                this.luckPermsEnabled = true;
            } catch (IllegalStateException e) {
                this.luckPermsEnabled = false;
            }
        }

        if (plugin.getServer().getPluginManager().isPluginEnabled("TAB")) {
            this.tabHook = new TABHook(plugin);
            plugin.debug("TabManager: TAB plugin detected, delegating nick display to TABHook");
        } else {
            this.tabHook = null;
            plugin.debug("TabManager: TAB plugin not found, using built-in tab handling");
        }
    }

    private void startRefreshTask() {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> refreshAll(), 100L, 100L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().getScheduler().runDelayed(plugin, task -> updatePlayerTab(event.getPlayer()), null, 5L);
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        stateCache.remove(event.getPlayer().getUniqueId());
    }

    public void updatePlayerTab(Player player) {
        if (player == null || !player.isOnline()) return;
        doUpdatePlayerTab(player);
    }

    private void doUpdatePlayerTab(Player player) {
        if (player == null || !player.isOnline()) return;

        if (plugin.getVanishManager() != null && plugin.getVanishManager().isVanished(player)) {
            if (plugin.getConfigManager().isVanishHideFromTab()) {
                player.playerListName(null);
                stateCache.remove(player.getUniqueId());
                return;
            }
        }

        String cachedNick = plugin.getNickManager() != null
                ? plugin.getNickManager().getCachedNickname(player.getUniqueId())
                : null;
        String nick = cachedNick != null ? cachedNick : "";
        boolean afk = plugin.getAfkManager() != null && plugin.getAfkManager().isAFK(player);

        if (tabHook != null) {
            tabHook.updateNick(player, cachedNick);
            return;
        }

        String lpPrefix = "";
        String lpSuffix = "";

        if (luckPermsEnabled && useLuckPermsTab) {
            TabState previous = stateCache.get(player.getUniqueId());
            if (previous != null && previous.nick().equals(nick) && previous.afk() == afk) {
                return;
            }

            CachedMetaData metaData;
            try {
                metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
            } catch (IllegalStateException e) {
                plugin.debug("TabManager: LuckPerms user not yet loaded for " + player.getName() + ", skipping tab update");
                return;
            }
            lpPrefix = metaData.getPrefix() != null ? metaData.getPrefix() : "";
            lpSuffix = metaData.getSuffix() != null ? metaData.getSuffix() : "";
        }

        TabState current = new TabState(lpPrefix, lpSuffix, nick, afk);
        TabState previous = stateCache.get(player.getUniqueId());

        if (current.equals(previous)) return;
        stateCache.put(player.getUniqueId(), current);

        TextComponent.Builder builder = Component.text();

        if (afk) {
            String afkTag = plugin.getConfigManager().getAfkTabPlaceholder();
            if (afkTag != null && !afkTag.isEmpty()) {
                builder.append(plugin.getMiniMessage().deserialize(afkTag));
            }
        }

        if (!lpPrefix.isEmpty()) {
            builder.append(legacyAmpersand.deserialize(lpPrefix));
        }

        boolean nickInTab = plugin.getNickManager() != null
                && plugin.getConfigManager().isNickTabEnabled();

        if (nickInTab && !nick.isEmpty()) {
            String indicator = plugin.getConfigManager().getNickIndicator();
            if (!indicator.isEmpty()) builder.append(Component.text(indicator));
            builder.append(plugin.getMiniMessage().deserialize(nick));
        } else {
            builder.append(Component.text(player.getName()));
        }

        if (!lpSuffix.isEmpty()) {
            builder.append(legacyAmpersand.deserialize(lpSuffix));
        }

        player.playerListName(builder.build());
        updateScoreboardTeam(player, lpPrefix, lpSuffix);
    }

    private void updateScoreboardTeam(Player player, String lpPrefix, String lpSuffix) {
        if (ServerSoftware.isFolia()) return;

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = TeamNameUtil.fromUUID(player.getUniqueId());

        Team team = scoreboard.getTeam(teamName);
        if (team == null) team = scoreboard.registerNewTeam(teamName);

        if (!lpPrefix.isEmpty()) {
            String sectionPrefix = LegacyColorConverter.toLegacySection(legacyAmpersand.deserialize(lpPrefix));
            team.setPrefix(truncate(sectionPrefix, TEAM_FIELD_MAX));
        } else {
            team.setPrefix("");
        }

        if (!lpSuffix.isEmpty()) {
            String sectionSuffix = LegacyColorConverter.toLegacySection(legacyAmpersand.deserialize(lpSuffix));
            team.setSuffix(truncate(sectionSuffix, TEAM_FIELD_MAX));
        } else {
            team.setSuffix("");
        }

        if (!team.hasEntry(player.getName())) team.addEntry(player.getName());
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }

    public void refreshAll() {
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        if (online.isEmpty()) return;
        for (Player player : online) {
            doUpdatePlayerTab(player);
        }
    }

    public boolean isEnabled() {
        return luckPermsEnabled && useLuckPermsTab;
    }

    public void shutdown() {
        luckPerms = null;
        luckPermsEnabled = false;
        useLuckPermsTab = false;
        stateCache.clear();
        HandlerList.unregisterAll(this);
        plugin.debug("Shutting down the Tab Manager");
    }
}