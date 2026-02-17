package net.godlycow.org.api.test.testApi;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getCommand("testapi").setExecutor(new TestCommand());
        getLogger().info("TestApi enabled - EssentialsC event tester ready");
    }

    @Override
    public void onDisable() {
        getLogger().info("TestApi disabled");
    }

    public static Main getInstance() {
        return instance;
    }
}