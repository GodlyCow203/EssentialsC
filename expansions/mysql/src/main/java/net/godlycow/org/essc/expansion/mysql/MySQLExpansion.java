package net.godlycow.org.essc.expansion.mysql;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.expansion.mysql.command.MySQLCommand;
import net.godlycow.org.essc.expansion.mysql.config.ExpansionConfig;
import net.godlycow.org.essc.expansion.mysql.storage.ConnectionPool;
import net.godlycow.org.essc.expansion.mysql.storage.SchemaManager;
import net.godlycow.org.essc.expansion.mysql.sync.EconomySyncListener;
import net.godlycow.org.essc.expansion.mysql.sync.EconomySyncService;
import net.godlycow.org.essc.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class MySQLExpansion extends JavaPlugin {

    private static MySQLExpansion instance;

    private EssentialsC essentialsC;
    private ExpansionConfig config;

    private LanguageManager lang;
    private ConnectionPool pool;
    private SchemaManager schema;
    private EconomySyncService economySync;
    private EconomySyncListener listener;

    private long startTime;

    @Override
    public void onEnable() {
        instance = this;
        startTime = System.currentTimeMillis();

        var esscPlugin = Bukkit.getPluginManager().getPlugin("EssentialsC");

        if (!(esscPlugin instanceof EssentialsC essc)) {
            getLogger().severe("EssentialsC was not found or is disabled. Disabling MySQL expansion : (");
            Bukkit.getPluginManager().disablePlugin(this);

            return;
        }
        this.essentialsC = essc;
        this.lang = essc.getLanguageManager();

        saveDefaultConfig();
        this.config = new ExpansionConfig(this);

        CommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(lang.get(console, "mysql.enable.connecting"));

        this.pool = new ConnectionPool(this, config);
        if (!pool.connect()) {
            console.sendMessage(lang.get(console, "mysql.enable.failed_not_configured"));
        }

        this.schema = new SchemaManager(this, pool,  config.getTablePrefix());
        this.schema.ensureSchema();

        this.economySync = new EconomySyncService(this, essentialsC, pool, schema, config, config.getServerId());
        this.economySync.init();
        this.economySync.start();

        if (economySync.isEnabled()) {
            this.listener = new EconomySyncListener(economySync);
            Bukkit.getPluginManager().registerEvents(listener, this);
            if (pool.isConnected()) {

                console.sendMessage(lang.get(console, "mysql.enable.connected"));
            } else {
                console.sendMessage(lang.get(console, "mysql.enable.failed_not_configured"));

            }
        } else if (essentialsC.getEconomyManager() == null) {

            console.sendMessage(lang.get(console, "mysql.enable.economy_disabled"));
        }

        registerCommand();
    }

    private void registerCommand() {
        MySQLCommand executor = new MySQLCommand(this, essentialsC, pool, config, economySync);
        getCommand("mysql").setExecutor(executor);
        getCommand("mysql").setTabCompleter(executor);
    }

    @Override
    public void onDisable() {
        if (economySync != null) {
            economySync.stop();
            economySync.flush();
        }
        if (listener != null) {
            org.bukkit.event.HandlerList.unregisterAll(listener);
            listener = null;
        }
        if (pool != null) {
            pool.close();
        }
        getLogger().info("EssentialsC MySQL expansion disabled.");
    }

    public boolean reloadExpansion() {
        reloadConfig();
        this.config = new ExpansionConfig(this);

        if (economySync != null) {
            economySync.stop();
        }

        if (pool != null) {
            pool.close();
        }
        if (listener != null) {
            org.bukkit.event.HandlerList.unregisterAll(listener);
            listener = null;
        }

        this.pool = new ConnectionPool(this, config);
        boolean connected = pool.connect();

        this.schema = new SchemaManager(this, pool, config.getTablePrefix());
        this.schema.ensureSchema();

        this.economySync = new EconomySyncService(this, essentialsC, pool, schema, config, config.getServerId());
        this.economySync.init();
        this.economySync.start();

        if (economySync.isEnabled()) {
            this.listener = new EconomySyncListener(economySync);
            Bukkit.getPluginManager().registerEvents(listener, this);
        }



        return connected;
    }


    public void debug(String message) {
        if (config != null && config.isDebug()) {
            getLogger().info("[Debug] " + message);
        }
    }

    public long getStartTime() {
        return startTime;
    }


}
