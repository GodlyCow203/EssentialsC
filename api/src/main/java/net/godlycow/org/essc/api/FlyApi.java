package net.godlycow.org.essc.api;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * API interface for interacting with EssentialsC's fly system.
 *
 * <p>EssentialsC persists fly state across sessions — players who were flying
 * when they disconnected will have flight restored on their next join (unless
 * they hold the {@code essentialsc.fly.disable-on-join} permission).</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getFlyApi()}.</p>
 *
 * <pre>{@code
 * FlyApi fly = APIProvider.getAPI().getFlyApi();
 *
 * if (!fly.isFlying(player)) {
 *     fly.setFlying(player, true);
 * }
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface FlyApi {

    /**
     * Returns whether the given player currently has flight enabled.
     *
     * <p>This checks both {@code allowFlight} and the active flying state on the
     * player, so it returns {@code true} only when the player can and is flying.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player is currently flying
     */
    boolean isFlying(Player player);

    /**
     * Returns whether the given player is flagged to have their fly state restored
     * on their next join.
     *
     * <p>Players are added to this set when they disconnect while flying. The flag
     * is cleared once they reconnect and flight is restored.</p>
     *
     * @param uuid the UUID of the player; must not be {@code null}
     * @return {@code true} if flight will be restored for this player on next join
     */
    boolean hasPersistentFly(UUID uuid);

    /**
     * Enables or disables flight for the given player.
     *
     * <p>When enabling, both {@code allowFlight} and the active fly state are set.
     * When disabling, both are cleared. This does not affect the persistent fly
     * flag — use {@link #setPersistentFly(UUID, boolean)} if you also want to
     * control session restoration.</p>
     *
     * @param player  the player to modify; must not be {@code null} and must be online
     * @param flying  {@code true} to enable flight, {@code false} to disable it
     */
    void setFlying(Player player, boolean flying);

    /**
     * Toggles flight for the given player.
     *
     * <p>Equivalent to {@code setFlying(player, !isFlying(player))}.</p>
     *
     * @param player the player to toggle; must not be {@code null} and must be online
     */
    void toggleFlying(Player player);

    /**
     * Sets whether the given player's fly state will be restored on their next join.
     *
     * <p>Set to {@code true} to add the player to the persistence set, {@code false}
     * to remove them. Changes are written to disk immediately.</p>
     *
     * @param uuid       the UUID of the player; must not be {@code null}
     * @param persistent {@code true} to persist fly across the next session,
     *                   {@code false} to clear the flag
     */
    void setPersistentFly(UUID uuid, boolean persistent);

    /**
     * Returns a snapshot of the UUIDs of all players whose fly state is queued
     * for restoration on next join.
     *
     * <p>The returned set is not backed by the internal state — modifications have
     * no effect.</p>
     *
     * @return an unmodifiable set of UUIDs; never {@code null}, may be empty
     */
    Set<UUID> getPersistentFlyPlayers();
}