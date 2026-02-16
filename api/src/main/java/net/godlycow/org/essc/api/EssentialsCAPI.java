package net.godlycow.org.essc.api;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Main API class
public interface EssentialsCAPI {

    /**
     * Gets the EssentialsC API instance.
     * @return The API instance, or null if EssentialsC is not loaded
     */
    @Nullable
    static EssentialsCAPI getInstance() {
        return APIProvider.getInstance();
    }

    /**
     * Gets the HomeManager for home-related operations.
     * @return The HomeManager instance
     */
    @NotNull
    HomeManager getHomeManager();

    /**
     * Checks if EssentialsC is fully loaded and ready.
     * @return true if ready
     */
    boolean isReady();

    /**
     * Gets the plugin instance (for advanced usage).
     * @return The EssentialsC plugin instance
     */
    @NotNull
    JavaPlugin getPlugin();
}