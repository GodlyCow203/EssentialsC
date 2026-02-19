package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.APIProvider;
import net.godlycow.org.essc.api.EssentialsCAPI;
import net.godlycow.org.essc.api.event.afk.AFKManager;
import net.godlycow.org.essc.api.event.auction.AuctionManager;
import net.godlycow.org.essc.api.event.home.HomeManager;
import net.godlycow.org.essc.api.event.shop.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class EssentialsCAPIImpl implements EssentialsCAPI {
    private final EssentialsC plugin;
    private final HomeManager homeManager;
    private final ShopManager shopManager;
    private final AFKManager afkManager;
    private final AuctionManager auctionManager;
    private boolean ready = false;

    public EssentialsCAPIImpl(EssentialsC plugin) {
        this.plugin = plugin;
        this.homeManager = new HomeManagerImpl(plugin);
        this.shopManager = new ShopManagerImpl(plugin);
        this.afkManager = new AFKManagerImpl(plugin);
        this.auctionManager = new AuctionManagerImpl(plugin);
    }

    public void enable() {
        APIProvider.setInstance(this);
        this.ready = true;
        plugin.getLogger().info("EssentialsC API enabled");
    }

    public void disable() {
        this.ready = false;
        APIProvider.clearInstance();
    }

    @Override
    public @NotNull HomeManager getHomeManager() {
        return homeManager;
    }

    @Override
    public @NotNull ShopManager getShopManager() {
        return shopManager;
    }

    @Override
    public @NotNull AFKManager getAFKManager() { return afkManager;}

    @Override
    public @NotNull AuctionManager getAuctionManager() { return auctionManager;}

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public @NotNull JavaPlugin getPlugin() {
        return plugin;
    }
}