package net.godlycow.org.essc.api;

/**
 * How you get the API. EssentialsC registers itself on enable,
 * so just check {@link #isAvailable()} before calling {@link #get()}.
 */
public final class APIProvider {
    private static EssentialsCAPI instance;

    private APIProvider() {
        throw new UnsupportedOperationException("APIProvider cannot be instantiated");
    }

    /**
     * Registers the API instance. Only EssentialsC itself calls this.
     *
     * @param api the implementation to expose
     * @throws IllegalArgumentException if {@code api} is {@code null}
     * @throws IllegalStateException if an instance is already registered
     */
    public static void register(EssentialsCAPI api) {
        if (api == null) {
            throw new IllegalArgumentException("API instance must not be null");
        }
        if (instance != null) {
            throw new IllegalStateException("API provider already registered");
        }
        instance = api;
    }

    /**
     * Clears the registered instance. Only EssentialsC itself calls this.
     */
    public static void unregister() {
        instance = null;
    }

    /**
     * Gets the API instance.
     *
     * @return the API instance
     * @throws IllegalStateException if EssentialsC is not loaded
     */
    public static EssentialsCAPI get() {
        if (instance == null) {
            throw new IllegalStateException("API provider not registered");
        }
        return instance;
    }

    /**
     * Checks if the API is ready to use.
     *
     * @return true if {@link #get()} will work
     */
    public static boolean isAvailable() {
        boolean available = instance != null;
        return available;
    }
}
