package net.godlycow.org.essc.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public final class APIProvider {
    private static EssentialsCAPI instance;

    // This constructor is private so the class cannot be created as an object
    private APIProvider() {}

    // Set the API instance when the plugin starts (can only be done once)
    public static void setInstance(@NotNull EssentialsCAPI api) {
        if (instance != null) {
            throw new IllegalStateException("API already set!");
        }
        instance = api;
    }

    // Retrieve the active API instance
    @Nullable
    public static EssentialsCAPI getInstance() {
        return instance;
    }

    // Remove the API instance when the plugin is disabled
    public static void clearInstance() {
        instance = null;
    }
}