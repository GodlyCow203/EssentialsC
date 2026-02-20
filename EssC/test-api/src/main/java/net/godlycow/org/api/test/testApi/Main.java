package net.godlycow.org.api.test.testApi;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopEventListener(this), this);

        getCommand("testapi").setExecutor(new TestCommand());
        getCommand("testshop").setExecutor(new ShopTestCommand());
        getCommand("testah").setExecutor(new AuctionTestCommand());
        getCommand("testback").setExecutor(new BackTestCommand());

        getLogger().info("TestApi enabled - EssentialsC event tester ready");
        getLogger().info("Commands: /testapi (home tests), /testshop (shop tests)");
    }

    @Override
    public void onDisable() {
        getLogger().info("TestApi disabled");
    }

    public static Main getInstance() {
        return instance;
    }
}