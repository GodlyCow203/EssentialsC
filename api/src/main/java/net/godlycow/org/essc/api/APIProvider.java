package net.godlycow.org.essc.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// porvider
public final class APIProvider {
    private static EssentialsCAPI instance;

    private APIProvider() {}

    public static void setInstance(@NotNull EssentialsCAPI api) {
        if (instance != null) {
            throw new IllegalStateException("API instance already set!");
        }
        instance = api;
    }

    @Nullable
    public static EssentialsCAPI getInstance() {
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }
}