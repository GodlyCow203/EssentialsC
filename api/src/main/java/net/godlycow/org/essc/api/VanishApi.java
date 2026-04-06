package net.godlycow.org.essc.api;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * API interface for interacting with EssentialsC's vanish system.
 *
 * <p>Manages player invisibility, hiding from other players, tab list,
 * mob targeting, and collisions. Supports permission-based visibility
 * for staff members.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getVanishApi()}.</p>
 *
 * <pre>{@code
 * VanishApi vanish = APIProvider.getAPI().getVanishApi();
 *
 * if (!vanish.isVanished(player)) {
 *     vanish.vanish(player);
 * }
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface VanishApi {

    /**
     * Hides the player from other players without the {@code essentialsc.vanish.see}
     * permission.
     *
     * <p>Applies configured effects (night vision, collision disable, etc.)
     * and updates tab list visibility.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player to vanish; must not be {@code null}
     */
    void vanish(Player player);

    /**
     * Reveals the player to all other players.
     *
     * <p>Removes vanish metadata and effects, shows player to all,
     * and updates tab list.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player to unvanish; must not be {@code null}
     */
    void unvanish(Player player);

    /**
     * Returns whether the player is currently vanished.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player is vanished
     */
    boolean isVanished(Player player);

    /**
     * Returns an unmodifiable set of all currently vanished player UUIDs.
     *
     * @return set of vanished player UUIDs; never {@code null}
     */
    Set<UUID> getVanishedPlayers();

    /**
     * Toggles vanish state for the given player.
     *
     * <p>Vanishes if currently visible, unvanishes if currently vanished.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player to toggle; must not be {@code null}
     */
    default void toggle(Player player) {
        if (isVanished(player)) {
            unvanish(player);
        } else {
            vanish(player);
        }
    }

    /**
     * Returns whether the player can see vanished players.
     *
     * <p>Convenience method checking the {@code essentialsc.vanish.see}
     * permission.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player has vanish see permission
     */
    boolean canSeeVanished(Player player);

    /**
     * Reloads the vanish configuration from disk.
     *
     * <p>Updates hide-from-tab, night-vision, mob-targeting, and
     * collision settings from config.</p>
     *
     * <p>Must be called on the main thread.</p>
     */
    void reload();
}