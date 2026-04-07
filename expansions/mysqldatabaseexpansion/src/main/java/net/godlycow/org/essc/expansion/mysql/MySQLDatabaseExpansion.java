package net.godlycow.org.essc.expansion.mysql;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.expansion.mysql.config.SyncConfig;
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
            getLogger().log(Level.SEVERE, "Failed to initialize MySQL sync manager — disabling.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(new SyncListener(this, syncManager), this);

        var cmd = getCommand("mysqlsync");
        if (cmd != null) {
            var executor = new net.godlycow.org.essc.expansion.mysql.command.SyncCommand(this, syncManager);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        getLogger().info("EssentialsC-MySQLExpansion enabled. Server ID: " + syncConfig.getServerId());
    }

    @Override
    public void onDisable() {
        if (syncManager != null) {
            syncManager.shutdown();
        }
        getLogger().info("EssentialsC-MySQLExpansion disabled.");
    }

    public static MySQLDatabaseExpansion getInstance() {
        return instance;
    }

    public EssentialsC getEssentialsC() {
        return essentialsC;
    }

    public SyncConfig getSyncConfig() {
        return syncConfig;
    }

    public BalanceSyncManager getSyncManager() {
        return syncManager;
    }
}