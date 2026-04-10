package net.godlycow.org.essc.expansion.mysql;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.expansion.mysql.command.SyncCommand;
import net.godlycow.org.essc.expansion.mysql.config.SyncConfig;
import net.godlycow.org.essc.expansion.mysql.kit.NetworkKitSyncManager;
import net.godlycow.org.essc.expansion.mysql.punishment.NetworkPunishmentListener;
import net.godlycow.org.essc.expansion.mysql.punishment.NetworkPunishmentSyncManager;
import net.godlycow.org.essc.expansion.mysql.sync.BalanceSyncManager;
import net.godlycow.org.essc.expansion.mysql.sync.SyncListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class MySQLDatabaseExpansion extends JavaPlugin {

    private static MySQLDatabaseExpansion instance;

    private EssentialsC essentialsC;
    private SyncConfig syncConfig;
    private BalanceSyncManager syncManager;
    private NetworkPunishmentSyncManager punishmentSyncManager;
    private NetworkKitSyncManager networkKitSyncManager;

    @Override
    public void onEnable() {
        instance = this;

        var esscPlugin = Bukkit.getPluginManager().getPlugin("EssentialsC");
        if (!(esscPlugin instanceof EssentialsC essc)) {
            getLogger().severe("EssentialsC not found or not loaded — disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.essentialsC = essc;

        if (essentialsC.getEconomyManager() == null) {
            getLogger().severe("EssentialsC economy is not enabled — disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        this.syncConfig = new SyncConfig(this);

        try {
            this.syncManager = new BalanceSyncManager(this, essentialsC, syncConfig);
            syncManager.init();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize economy sync manager — disabling.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            this.punishmentSyncManager = new NetworkPunishmentSyncManager(
                    this, essentialsC, syncConfig, syncManager.getDatabase());
            punishmentSyncManager.start();

            essentialsC.getPunishmentManager().setNetworkHook(punishmentSyncManager);

            Bukkit.getPluginManager().registerEvents(
                    new NetworkPunishmentListener(this, punishmentSyncManager), this);

            getLogger().info("[NetworkPunishments] Network punishment sync enabled.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize network punishment sync.", e);
        }

        if (essentialsC.getKitManager() != null && syncConfig.isNetworkKitsEnabled()) {
            try {
                this.networkKitSyncManager = new NetworkKitSyncManager(
                        this, essentialsC, syncConfig, syncManager.getDatabase());
                essentialsC.getKitManager().setNetworkHook(networkKitSyncManager);
                getLogger().info("[NetworkKits] Network kit cooldown sync enabled.");
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to initialize network kit sync.", e);
            }
        }

        Bukkit.getPluginManager().registerEvents(new SyncListener(this, syncManager), this);

        var cmd = getCommand("mysqlsync");
        if (cmd != null) {
            var executor = new SyncCommand(this, syncManager);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        getLogger().info("EssentialsC-MySQLExpansion enabled. Server ID: " + syncConfig.getServerId());
    }

    @Override
    public void onDisable() {
        if (networkKitSyncManager != null) {
            networkKitSyncManager.shutdown();
        }
        if (punishmentSyncManager != null) {
            punishmentSyncManager.shutdown();
        }
        if (syncManager != null) {
            syncManager.shutdown();
        }
        getLogger().info("EssentialsC-MySQLExpansion disabled.");
    }

    public static MySQLDatabaseExpansion getInstance() {
        return instance;
    }
    public EssentialsC getEssentialsC(){
        return essentialsC;
    }
    public SyncConfig getSyncConfig() {
        return syncConfig;
    }
    public BalanceSyncManager getSyncManager(){
        return syncManager;
    }
    public NetworkPunishmentSyncManager getPunishmentSyncManager() {
        return punishmentSyncManager;
    }
    public NetworkKitSyncManager getKitSyncManager() {
        return networkKitSyncManager;
    }
}