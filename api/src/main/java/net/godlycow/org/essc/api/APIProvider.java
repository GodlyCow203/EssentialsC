package net.godlycow.org.essc.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal class that holds the API instance.
 * Dont use this directly, use EssentialsCAPI.getInstance() instead.
 */
public final class APIProvider {
    private static EssentialsCAPI instance;

    private APIProvider() {}

    /**
     * Sets the API instance. Called by EssentialsC on enable.
     */
    public static void setInstance(@NotNull EssentialsCAPI api) {
        if (instance != null) {
            throw new IllegalStateException("API already set!");
        }
        instance = api;
    }

    /**
     * Gets the API instance. Returns null if not set yet.
     */
    @Nullable
    public static EssentialsCAPI getInstance() {
        return instance;
    }

    /**
     * Clears the API instance. Called by EssentialsC on disable.
     */
    public static void clearInstance() {
        instance = null;
    }
}