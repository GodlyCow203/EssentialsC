package net.godlycow.org.essc.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * API interface for interacting with EssentialsC's spawn system.
 *
 * <p>Manages the server spawn location, teleportation with warmup and cooldown,
 * and first-join spawning. All location data is persisted to spawn.yml.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getSpawnApi()}.</p>
 *
 * <pre>{@code
 * SpawnApi spawn = APIProvider.getAPI().getSpawnApi();
 *
 * if (spawn.isSpawnSet()) {
 *     spawn.teleportToSpawn(player);
 * }
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface SpawnApi {

    /**
     * Returns whether a spawn location has been set.
     *
     * @return {@code true} if spawn is configured and world is loaded
     */
    boolean isSpawnSet();

    /**
     * Returns the current spawn location.
     *
     * @return a copy of the spawn location, or {@code null} if not set
     */
    Location getSpawn();

    /**
     * Sets the spawn location to the given location.
     *
     * <p>Persists immediately to spawn.yml. Overwrites any existing spawn.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param location the location to set as spawn; must not be {@code null}
     */
    void setSpawn(Location location);

    /**
     * Returns whether the given player is currently on spawn cooldown.
     *
     * <p>Players with the {@code essentialsc.spawn.admin} permission bypass cooldowns.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player must wait before using spawn again
     */
    boolean isOnCooldown(Player player);

    /**
     * Returns the remaining cooldown time in seconds for the given player.
     *
     * <p>Returns {@code 0} if the player is not on cooldown or has bypass permission.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return remaining cooldown in seconds, or {@code 0}
     */
    long getRemainingCooldown(Player player);

    /**
     * Returns whether the given player has a pending spawn teleport warmup.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if a warmup is in progress
     */
    boolean hasPendingTeleport(Player player);

    /**
     * Initiates teleportation to spawn for the given player.
     *
     * <p>Applies warmup and cooldown checks. Sends appropriate messages to the player.
     * If warmup is configured, the teleport will occur after the delay unless cancelled.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player to teleport; must not be {@code null}
     */
    void teleportToSpawn(Player player);

    /**
     * Immediately teleports the given player to spawn, bypassing warmup.
     *
     * <p>Cooldown is still applied unless the player has bypass permission.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player to teleport; must not be {@code null}
     */
    void teleportToSpawnImmediate(Player player);

    /**
     * Cancels any pending spawn teleport for the given player.
     *
     * <p>Has no effect if the player has no pending teleport.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player whose teleport to cancel; must not be {@code null}
     */
    void cancelTeleport(Player player);

    /**
     * Reloads the spawn configuration from disk.
     *
     * <p>Re-reads spawn.yml and updates the cached spawn location.</p>
     *
     * <p>Must be called on the main thread.</p>
     */
    void reload();
}