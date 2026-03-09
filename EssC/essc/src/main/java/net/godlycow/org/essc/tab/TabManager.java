package net.godlycow.org.essc.tab;

import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TabManager implements Listener {

    private final EssentialsC plugin;
    private LuckPerms luckPerms;
    private boolean luckPermsEnabled;
    private boolean useLuckPermsTab;

    public TabManager(EssentialsC plugin) {
        this.plugin = plugin;
        reload();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        startRefreshTask();
    }

    public void reload() {
        this.useLuckPermsTab = plugin.getConfigManager().isLuckPermsTabEnabled();

        if (useLuckPermsTab && plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            var provider = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                this.luckPerms = provider.getProvider();
                this.luckPermsEnabled = true;
            } else {
                try {
                    this.luckPerms = LuckPermsProvider.get();
                    this.luckPermsEnabled = true;
                } catch (IllegalStateException e) {
                    this.luckPermsEnabled = false;
                }
            }
        } else {
            this.luckPermsEnabled = false;
        }
    }

    private void startRefreshTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                refreshAll();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> updatePlayerTab(event.getPlayer()), 5L);
    }

    public void updatePlayerTab(Player player) {
        if (player == null || !player.isOnline()) return;

        String prefix = "";
        String suffix = "";

        if (luckPermsEnabled && useLuckPermsTab) {
            CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
            if (metaData.getPrefix() != null) prefix = metaData.getPrefix();
            if (metaData.getSuffix() != null) suffix = metaData.getSuffix();
        }

        String afkTag = "";
        if (plugin.getAfkManager() != null && plugin.getAfkManager().isAFK(player)) {
            afkTag = plugin.getConfigManager().getAfkTabPlaceholder();
        }

        String rawFullName = (afkTag + prefix + player.getName() + suffix).replace('&', '§');
        Component finalComponent = plugin.getMiniMessage().deserialize(rawFullName);

        player.playerListName(finalComponent);

        updateScoreboardTeam(player, afkTag + prefix, suffix);
    }

    private void updateScoreboardTeam(Player player, String prefix, String suffix) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "lp_" + (player.getName().length() > 13 ? player.getName().substring(0, 13) : player.getName());

        Team team = scoreboard.getTeam(teamName);
        if (team == null) team = scoreboard.registerNewTeam(teamName);

        Component prefComp = plugin.getMiniMessage().deserialize(prefix.replace('&', '§'));
        Component suffComp = plugin.getMiniMessage().deserialize(suffix.replace('&', '§'));

        team.setPrefix(LegacyComponentSerializer.legacySection().serialize(prefComp));
        team.setSuffix(LegacyComponentSerializer.legacySection().serialize(suffComp));

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    public void refreshAll() {
        Bukkit.getOnlinePlayers().forEach(this::updatePlayerTab);
    }

    public boolean isEnabled() {
        return luckPermsEnabled && useLuckPermsTab;
    }
}