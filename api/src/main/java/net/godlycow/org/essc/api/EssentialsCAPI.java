package net.godlycow.org.essc.api;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EssentialsCAPI {

    /**
     * Gets the API instance. Returns null if EssentialsC isnt loaded.
     */
    @Nullable
    static EssentialsCAPI getInstance() {
        return APIProvider.getInstance();
    }

    /**
     * Gets the home manager for all home stuff.
     */
    @NotNull
    HomeManager getHomeManager();

    /**
     * Checks if the API is ready to use.
     */
    boolean isReady();

    /**
     * Gets the actual plugin instance. Useful for checking version or whatever.
     */
    @NotNull
    JavaPlugin getPlugin();
}