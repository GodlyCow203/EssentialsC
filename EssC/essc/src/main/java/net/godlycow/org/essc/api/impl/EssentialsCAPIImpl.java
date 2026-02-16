package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.APIProvider;
import net.godlycow.org.essc.api.EssentialsCAPI;
import net.godlycow.org.essc.api.HomeManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of EssentialsCAPI. Just holds the HomeManager and
 * registers/unregisters the provider.
 */
public class EssentialsCAPIImpl implements EssentialsCAPI {
    private final EssentialsC plugin;
    private final HomeManager homeManager;
    private boolean ready = false;

    public EssentialsCAPIImpl(EssentialsC plugin) {
        this.plugin = plugin;
        this.homeManager = new HomeManagerImpl(plugin);
    }

    /** Call this in onEnable */
    public void enable() {
        APIProvider.setInstance(this);
        this.ready = true;
        plugin.getLogger().info("EssentialsC API enabled");
    }

    /** Call this in onDisable */
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