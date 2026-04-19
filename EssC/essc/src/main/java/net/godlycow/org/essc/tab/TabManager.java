package net.godlycow.org.essc.tab;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.bedrock.TeamNameUtil;
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
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TabManager implements Listener {

    private final EssentialsC plugin;
    private LuckPerms luckPerms;
    private boolean luckPermsEnabled;
    private boolean useLuckPermsTab;
    private TABHook tabHook;

    private final LegacyComponentSerializer legacyAmpersand = LegacyComponentSerializer.legacyAmpersand();

    private static final int TEAM_FIELD_MAX = 64;

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
        plugin.getEssScheduler().runGlobalTimer(this::refreshAll, 20L, 20L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getEssScheduler().runForEntityLater(event.getPlayer(), () -> updatePlayerTab(event.getPlayer()), 5L);
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
                return;
            }
        }

        if (tabHook != null) {
            String cachedNick = plugin.getNickManager() != null
                    ? plugin.getNickManager().getCachedNickname(player.getUniqueId())
                    : null;
            tabHook.updateNick(player, cachedNick);
            return;
        }

        TextComponent.Builder builder = Component.text();

        if (plugin.getAfkManager() != null && plugin.getAfkManager().isAFK(player)) {
            String afkTag = plugin.getConfigManager().getAfkTabPlaceholder();
            if (afkTag != null && !afkTag.isEmpty()) {
                builder.append(plugin.getMiniMessage().deserialize(afkTag));
            }
        }

        String lpPrefix = "";
        String lpSuffix = "";
        if (luckPermsEnabled && useLuckPermsTab) {
            CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
            lpPrefix = metaData.getPrefix() != null ? metaData.getPrefix() : "";
            lpSuffix = metaData.getSuffix() != null ? metaData.getSuffix() : "";

            if (!lpPrefix.isEmpty()) {
                builder.append(legacyAmpersand.deserialize(lpPrefix));
            }
        }

        boolean nickInTab = plugin.getNickManager() != null
                && plugin.getConfigManager().isNickTabEnabled();

        if (nickInTab) {
            String cachedNick = plugin.getNickManager().getCachedNickname(player.getUniqueId());
            if (cachedNick != null && !cachedNick.isEmpty()) {
                String indicator = plugin.getConfigManager().getNickIndicator();
                if (!indicator.isEmpty()) builder.append(Component.text(indicator));
                builder.append(plugin.getMiniMessage().deserialize(cachedNick));
            } else {
                builder.append(Component.text(player.getName()));
            }
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
        if (net.godlycow.org.essc.softwares.ServerSoftware.isFolia()) return;

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
        for (Player player : Bukkit.getOnlinePlayers()) updatePlayerTab(player);
    }

    public boolean isEnabled()  {
        return luckPermsEnabled && useLuckPermsTab;
    }
    public boolean isUsingTABPlugin() {
        return tabHook != null;
    }
}