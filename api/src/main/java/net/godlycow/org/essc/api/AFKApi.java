package net.godlycow.org.essc.api;

import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * API interface for interacting with EssentialsC's AFK system.
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getAFKApi()}.</p>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface AFKApi {

    /**
     * Sets the AFK state of a player.
     *
     * <p>If the player is already in the requested state, this method does nothing.
     * When {@code broadcast} is {@code true} and broadcasting is enabled in the
     * plugin config, all online players will receive the appropriate enter/leave message.</p>
     *
     * @param player    the player whose AFK state to change; must not be {@code null}
     * @param afk       {@code true} to mark the player as AFK, {@code false} to return them
     * @param broadcast {@code true} to broadcast the state change to other online players
     */
    void setAFK(Player player, boolean afk, boolean broadcast);

    /**
     * Toggles the AFK state of a player.
     *
     * <p>If the player is currently AFK they will be returned to active. If they
     * are active they will be marked as AFK. The toggle always broadcasts to other
     * online players.</p>
     *
     * <p>When toggling off AFK, the player's last-activity timestamp is reset to now
     * so the auto-AFK timer restarts cleanly.</p>
     *
     * @param player the player to toggle; must not be {@code null}
     */
    void toggleAFK(Player player);

    /**
     * Returns whether the given player is currently AFK.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player is AFK, {@code false} otherwise
     */
    boolean isAFK(Player player);

    /**
     * Returns whether the player identified by the given UUID is currently AFK.
     *
     * <p>This overload is useful when you only have a UUID and the player may or
     * may not be online.</p>
     *
     * @param uuid the UUID of the player to check; must not be {@code null}
     * @return {@code true} if the player is AFK, {@code false} otherwise
     */
    boolean isAFK(UUID uuid);

    /**
     * Returns the {@link Instant} at which the player entered AFK mode.
     *
     * @param player the AFK player; must not be {@code null}
     * @return the instant the player went AFK, or {@code null} if the player is not AFK
     */
    Instant getAFKStartTime(Player player);

    /**
     * Returns how long the player has been AFK, in whole seconds.
     *
     * @param player the AFK player; must not be {@code null}
     * @return seconds since the player went AFK, or {@code 0} if the player is not AFK
     */
    long getAFKDurationSeconds(Player player);

    /**
     * Returns the AFK duration of the given player as a human-readable string.
     *
     * <p>The format depends on the elapsed time:</p>
     * <ul>
     *   <li>Less than 1 minute — {@code "Xs"}</li>
     *   <li>1 minute or more — {@code "Xm Ys"}</li>
     *   <li>1 hour or more — {@code "Xh Ym Zs"}</li>
     * </ul>
     *
     * @param player the AFK player; must not be {@code null}
     * @return a formatted duration string, or {@code "0s"} if the player is not AFK
     */
    String getAFKDurationFormatted(Player player);

    /**
     * Returns a snapshot of all currently online AFK players.
     *
     * <p>The returned set is not backed by the internal AFK state — modifications
     * to it have no effect on the AFK system.</p>
     *
     * @return an unmodifiable set of online players currently marked as AFK;
     *         never {@code null}, may be empty
     */
    Set<Player> getAFKPlayers();

    /**
     * Returns the number of online players currently marked as AFK.
     *
     * <p>Equivalent to {@code getAFKPlayers().size()} but slightly more efficient.</p>
     *
     * @return the current AFK player count, {@code 0} or greater
     */
    int getAFKCount();

    /**
     * Manually records activity for the given player and removes their AFK status if set.
     *
     * <p>Call this if your plugin performs an action on behalf of a player that should
     * count as activity (e.g. a teleport initiated by your code). Has no effect if
     * the player is offline.</p>
     *
     * @param player the player to record activity for; {@code null} is silently ignored
     */
    void updateActivity(Player player);
}