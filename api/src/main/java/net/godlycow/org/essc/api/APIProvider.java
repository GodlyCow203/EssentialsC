package net.godlycow.org.essc.api;

/**
 * Static holder for the {@link EssentialsCAPI} instance.
 *
 * <p>EssentialsC registers its implementation during {@code onEnable}. You should
 * always null-check the result of {@link #getAPI()} in case EssentialsC is not
 * installed or failed to load.</p>
 *
 * <pre>{@code
 * EssentialsCAPI api = APIProvider.getAPI();
 * if (api == null) {
 *     getLogger().warning("EssentialsC not found — AFK integration disabled.");
 *     return;
 * }
 * boolean afk = api.getAFKApi().isAFK(player);
 * }</pre>
 */
public final class APIProvider {

    private static EssentialsCAPI instance;

    private APIProvider() {}

    /**
     * Returns the registered {@link EssentialsCAPI} implementation, or {@code null}
     * if EssentialsC has not yet registered one.
     *
     * @return the API instance, or {@code null}
     */
    public static EssentialsCAPI getAPI() {
        return instance;
    }

    /**
     * Registers the API implementation.
     *
     * <p><strong>Internal use only.</strong> This is called by EssentialsC during
     * {@code onEnable}. Plugins consuming the API should use {@link #getAPI()} instead.</p>
     *
     * @param api the implementation to register; must not be {@code null}
     * @throws IllegalStateException if an implementation is already registered
     */
    public static void register(EssentialsCAPI api) {
        if (instance != null) {
            throw new IllegalStateException("EssentialsCAPI is already registered.");
        }
        instance = api;
    }

    /**
     * Unregisters the current API implementation.
     *
     * <p><strong>Internal use only.</strong> Called by EssentialsC during {@code onDisable}.</p>
     */
    public static void unregister() {
        instance = null;
    }
}