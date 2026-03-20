package net.godlycow.org.essc.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * API interface for interacting with EssentialsC's Back teleport system.
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getBackApi()}.</p>
 *
 * <pre>{@code
 * BackApi back = APIProvider.getAPI().getBackApi();
 *
 * // check and retrieve a player's stored back location
 * if (back.hasBackLocation(player.getUniqueId())) {
 *     back.getBackLocation(player.getUniqueId())
 *         .ifPresent(loc -> player.sendMessage("Your back: " + loc.getBlockX() + ", " + loc.getBlockY()));
 * }
 *
 * // programmatically teleport a player back
 * back.teleportBack(player);
 * }</pre>
 *
 * <p>Back locations are stored in memory only. They are populated automatically
 * on death and on teleport events, and cleared when a player disconnects.</p>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface BackApi {


    /**
     * Returns whether the given player has a stored back location.
     *
     * <p>A back location is saved automatically on player death and on any
     * teleport event. It is cleared when the player disconnects.</p>
     *
     * @param uuid the UUID of the player; must not be {@code null}
     * @return {@code true} if a back location exists for this player;
     *         {@code false} otherwise
     */
    boolean hasBackLocation(UUID uuid);

    /**
     * Returns the stored back location for the given player, if present.
     *
     * <p>The returned {@link Location} is a defensive copy — modifying it has no
     * effect on the stored location. The optional is empty if no back location
     * has been saved yet or if the player is offline.</p>
     *
     * @param uuid the UUID of the player; must not be {@code null}
     * @return an {@link Optional} containing the back {@link Location},
     *         or {@link Optional#empty()} if none is stored
     */
    Optional<Location> getBackLocation(UUID uuid);

    /**
     * Returns whether the given player currently has a pending back teleport
     * in its warmup phase.
     *
     * <p>A teleport enters the warmup phase when {@link #teleportBack(Player)}
     * is called and the configured warmup time is greater than zero. The pending
     * state ends when the teleport completes or is cancelled.</p>
     *
     * @param uuid the UUID of the player; must not be {@code null}
     * @return {@code true} if a warmup teleport is in progress for this player;
     *         {@code false} otherwise
     */
    boolean hasPendingTeleport(UUID uuid);

    /**
     * Returns whether the given player is currently on the back-teleport cooldown.
     *
     * <p>Players with the {@code essentialsc.back.admin} permission are never
     * considered on cooldown. Returns {@code false} if the configured cooldown
     * duration is zero or negative.</p>
     *
     * @param uuid the UUID of the player; must not be {@code null}
     * @return {@code true} if the player must wait before using {@code /back}
     *         again; {@code false} otherwise
     */
    boolean isOnCooldown(UUID uuid);

    /**
     * Returns the number of seconds remaining on the given player's back-teleport
     * cooldown.
     *
     * <p>Returns {@code 0} if the player is not on cooldown, if the cooldown is
     * disabled, or if the player holds the {@code essentialsc.back.admin}
     * permission.</p>
     *
     * @param uuid the UUID of the player; must not be {@code null}
     * @return seconds remaining on the cooldown, {@code 0} or greater
     */
    long getRemainingCooldown(UUID uuid);


    /**
     * Returns the configured warmup duration in seconds before a back teleport
     * is executed.
     *
     * <p>A value of {@code 0} or less means teleports are instant. Players with
     * the {@code essentialsc.back.admin} permission always teleport instantly
     * regardless of this value.</p>
     *
     * @return warmup duration in seconds; {@code 0} or greater
     */
    long getWarmupSeconds();

    /**
     * Returns the configured cooldown duration in seconds between back teleports.
     *
     * <p>A value of {@code 0} or less means there is no cooldown.</p>
     *
     * @return cooldown duration in seconds; {@code 0} or greater
     */
    long getCooldownSeconds();

    /**
     * Returns whether the particle effect is played at the destination when a
     * back teleport completes.
     *
     * @return {@code true} if particles are enabled; {@code false} otherwise
     */
    boolean isParticlesEnabled();

    /**
     * Returns whether the teleport sound is played when a back teleport completes.
     *
     * @return {@code true} if sounds are enabled; {@code false} otherwise
     */
    boolean isSoundsEnabled();

    /**
     * Returns whether a pending back teleport is cancelled if the player moves
     * during the warmup phase.
     *
     * @return {@code true} if movement cancels the warmup; {@code false} otherwise
     */
    boolean isCancelOnMovementEnabled();


    /**
     * Sets (or overwrites) the stored back location for the given player.
     *
     * <p>Passing a {@link Location} with a {@code null} world or a {@code null}
     * location itself is a no-op. The location is cloned internally — you may
     * safely modify or discard the original after this call.</p>
     *
     * @param player   the player whose back location to set; must not be {@code null}
     * @param location the location to store; ignored if {@code null} or worldless
     */
    void setBackLocation(Player player, Location location);

    /**
     * Removes the stored back location for the given player.
     *
     * <p>Has no effect if no location is currently stored.</p>
     *
     * @param player the player whose back location to clear; must not be {@code null}
     */
    void removeBackLocation(Player player);

    /**
     * Initiates a back teleport for the given player.
     *
     * <p>Internally this method handles the full teleport flow:</p>
     * <ul>
     *   <li>No-op (with player message) if a teleport is already pending.</li>
     *   <li>No-op (with player message) if no back location is stored.</li>
     *   <li>No-op (with player message) if the player is on cooldown.</li>
     *   <li>Instant teleport if warmup is zero or the player has
     *       {@code essentialsc.back.admin}.</li>
     *   <li>Otherwise starts the warmup timer; the teleport executes after the
     *       configured warmup period unless cancelled.</li>
     * </ul>
     *
     * <p>This method is synchronous and safe to call on the main thread.</p>
     *
     * @param player the player to teleport; must not be {@code null}
     */
    void teleportBack(Player player);

    /**
     * Cancels a pending warmup back teleport for the given player.
     *
     * <p>Has no effect if no warmup teleport is currently in progress for this
     * player. The player will receive a cancellation message whose text may vary
     * based on the reason string.</p>
     *
     * <p>Known reason values recognised by the plugin:</p>
     * <ul>
     *   <li>{@code "move"} — player moved during warmup (uses the movement-cancel
     *       message key)</li>
     *   <li>Any other value — uses the generic cancellation message key</li>
     * </ul>
     *
     * @param player the player whose pending teleport to cancel; must not be {@code null}
     * @param reason a short reason string; use {@code "move"} for movement cancels,
     *               any other value for generic cancellation
     */
    void cancelTeleport(Player player, String reason);
}
