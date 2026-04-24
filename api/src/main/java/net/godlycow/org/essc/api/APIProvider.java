package net.godlycow.org.essc.api;

public final class APIProvider {
    private static EssentialsCAPI instance;

    private APIProvider() {
        throw new UnsupportedOperationException("APIProvider cannot be instantiated");
    }

    public static void register(EssentialsCAPI api) {
        if (api == null) {
            throw new IllegalArgumentException("API instance must not be null");
        }
        if (instance != null) {
            throw new IllegalStateException("API provider already registered");
        }
        instance = api;
    }

    public static void unregister() {
        instance = null;
    }

    public static EssentialsCAPI get() {
        if (instance == null) {
            throw new IllegalStateException("API provider not registered");
        }
        return instance;
    }

    public static boolean isAvailable() {
        boolean available = instance != null;
        return available;
    }
}