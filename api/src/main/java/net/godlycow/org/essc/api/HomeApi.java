package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.home.Home;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API interface for interacting with EssentialsC's home system.
 *
 * <p>All database operations return {@link CompletableFuture} — do not block the
 * main thread waiting on them. Cooldown and warmup checks are synchronous and
 * safe to call on the main thread.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getHomeApi()}.</p>
 *
 * <pre>{@code
 * HomeApi homes = APIProvider.getAPI().getHomeApi();
 *
 * homes.getHome(player.getUniqueId(), "base").thenAccept(home -> {
 *     if (home != null) {
 *         homes.startTeleport(player, home);
 *     }
 * });
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 * @see Home
 */
public interface HomeApi {

    /**
     * Looks up a single home by owner UUID and name.
     *
     * <p>Name comparison is case-insensitive. The future resolves {@code null}
     * if no home with the given name exists for this player.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the UUID of the home owner; must not be {@code null}
     * @param name the name of the home; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to the {@link Home}, or {@code null}
     *         if not found
     */
    CompletableFuture<Home> getHome(UUID uuid, String name);

    /**
     * Returns all homes belonging to the given player, ordered alphabetically by name.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the UUID of the home owner; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to a list of {@link Home}s;
     *         never {@code null}, may be empty
     */
    CompletableFuture<List<Home>> getHomes(UUID uuid);

    /**
     * Returns the total number of homes set by the given player.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the UUID of the home owner; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to the home count; {@code 0} if none
     */
    CompletableFuture<Integer> getHomeCount(UUID uuid);

    /**
     * Returns whether a home with the given name exists for the given player.
     *
     * <p>Name comparison is case-insensitive.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the UUID of the home owner; must not be {@code null}
     * @param name the name of the home; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} if the home exists
     */
    CompletableFuture<Boolean> homeExists(UUID uuid, String name);

    /**
     * Returns the maximum number of homes the given player is allowed to set.
     *
     * <p>Checks for permission nodes of the form {@code essentialsc.sethome.N} (where N
     * is 1–100), returning the highest matching value. Falls back to the configured
     * default if none match. Players with {@code essentialsc.sethome.unlimited} or
     * {@code essentialsc.sethome.admin} have no limit.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return the maximum home count for this player, or {@link Integer#MAX_VALUE}
     *         if unlimited
     */
    int getMaxHomes(Player player);

    /**
     * Creates or updates a home for the given player at the specified location.
     *
     * <p>If a home with the same name already exists for this player, its location
     * is overwritten. The name is stored in lowercase.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param player   the player setting the home; must not be {@code null}
     * @param name     the name of the home; must not be {@code null}
     * @param location the location to save; must not be {@code null} and must have a loaded world
     * @return a {@link CompletableFuture} resolving to {@code true} on success
     */
    CompletableFuture<Boolean> setHome(Player player, String name, Location location);

    /**
     * Deletes the home with the given name for the given player.
     *
     * <p>Name comparison is case-insensitive. The future resolves {@code false} if
     * no matching home was found.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the UUID of the home owner; must not be {@code null}
     * @param name the name of the home to delete; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} if a home was deleted
     */
    CompletableFuture<Boolean> deleteHome(UUID uuid, String name);

    /**
     * Begins the teleport process for the given player to the given home.
     *
     * <p>This method handles the full warmup and cooldown pipeline:</p>
     * <ul>
     *   <li>If the player is on cooldown, a message is sent and the teleport is aborted.</li>
     *   <li>If the configured warmup is greater than zero and the player does not hold
     *       {@code essentialsc.home.admin}, the teleport is scheduled after the warmup
     *       period. Moving during the warmup cancels it (if configured).</li>
     *   <li>If no warmup applies, the player is teleported immediately.</li>
     * </ul>
     *
     * <p>Particles and sounds are played on arrival according to the plugin config.
     * This method must be called on the main thread.</p>
     *
     * @param player the player to teleport; must not be {@code null} and must be online
     * @param home   the destination home; must not be {@code null}
     */
    void startTeleport(Player player, Home home);

    /**
     * Cancels any pending warmup teleport for the given player.
     *
     * <p>Has no effect if the player has no pending teleport.</p>
     *
     * @param player the player whose teleport to cancel; must not be {@code null}
     */
    void cancelTeleport(Player player);

    /**
     * Returns whether the given player currently has a warmup teleport pending.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if a teleport is scheduled but not yet completed
     */
    boolean hasPendingTeleport(Player player);

    /**
     * Returns whether the given player is currently on home teleport cooldown.
     *
     * <p>Players with the {@code essentialsc.home.admin} permission always return
     * {@code false}.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player must wait before using {@code /home} again
     */
    boolean isOnCooldown(Player player);

    /**
     * Returns the number of seconds remaining on the given player's home teleport cooldown.
     *
     * @param player the player to check; must not be {@code null}
     * @return seconds remaining; {@code 0} if the player is not on cooldown
     */
    long getRemainingCooldown(Player player);
}