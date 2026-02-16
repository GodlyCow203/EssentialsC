package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.APIProvider;
import net.godlycow.org.essc.api.EssentialsCAPI;
import net.godlycow.org.essc.api.HomeManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class EssentialsCAPIImpl implements EssentialsCAPI {
    private final EssentialsC plugin;
    private final HomeManager homeManager;
    private boolean ready = false;

    public EssentialsCAPIImpl(EssentialsC plugin) {
        this.plugin = plugin;
        this.homeManager = new HomeManagerImpl(plugin);
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
    public boolean isReady() {
        return ready;
    }

    @Override
    public @NotNull JavaPlugin getPlugin() {
        return plugin;
    }
}