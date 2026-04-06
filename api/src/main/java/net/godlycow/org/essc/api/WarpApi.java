package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.warp.WarpEntry;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API interface for interacting with EssentialsC's warp system.
 *
 * <p>Manages named teleport locations with permissions, economy costs,
 * categories, and usage tracking. All data is persisted to SQLite.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getWarpApi()}.</p>
 *
 * <pre>{@code
 * WarpApi warp = APIProvider.getAPI().getWarpApi();
 *
 * warp.getWarp("spawn").ifPresent(w -> {
 *     player.teleport(w.location());
 * });
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface WarpApi {

    /**
     * Returns whether the warp system is globally enabled.
     *
     * @return {@code true} if warps are enabled in config
     */
    boolean isSystemEnabled();

    /**
     * Returns the warp with the given name, if it exists.
     *
     * @param name the warp name; must not be {@code null}
     * @return an {@link Optional} containing the warp, or empty if not found
     */
    Optional<WarpEntry> getWarp(String name);

    /**
     * Returns all warps including hidden ones.
     *
     * @return an unmodifiable list of all warps; never {@code null}
     */
    List<WarpEntry> getAllWarps();

    /**
     * Returns all non-hidden warps sorted alphabetically.
     *
     * @return an unmodifiable list of visible warps; never {@code null}
     */
    List<WarpEntry> getVisibleWarps();

    /**
     * Returns warps in the specified category.
     *
     * @param category the category name; must not be {@code null}
     * @return an unmodifiable list of warps in the category; never {@code null}
     */
    List<WarpEntry> getWarpsByCategory(String category);

    /**
     * Returns all unique category names.
     *
     * @return an unmodifiable set of category names; never {@code null}
     */
    Set<String> getCategories();

    /**
     * Returns whether a warp with the given name exists.
     *
     * @param name the warp name to check; must not be {@code null}
     * @return {@code true} if the warp exists
     */
    boolean warpExists(String name);

    /**
     * Creates a new warp at the given location.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param name     the unique warp name; must not be {@code null}
     * @param location the location to set; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} if created
     */
    CompletableFuture<Boolean> createWarp(String name, Location location);

    /**
     * Deletes the warp with the given name.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param name the warp name to delete; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} if deleted
     */
    CompletableFuture<Boolean> deleteWarp(String name);

    /**
     * Updates an existing warp's properties.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param warp the warp entry with updated values; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} if updated
     */
    CompletableFuture<Boolean> updateWarp(WarpEntry warp);

    /**
     * Returns the remaining cooldown in seconds for the player.
     *
     * @param uuid the player's UUID; must not be {@code null}
     * @return remaining cooldown in seconds, or {@code 0} if none
     */
    long getRemainingCooldown(UUID uuid);

    /**
     * Returns the number of times the player has used the specified warp.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid     the player's UUID; must not be {@code null}
     * @param warpName the warp name; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to the usage count
     */
    CompletableFuture<Integer> getWarpUsage(UUID uuid, String warpName);

    /**
     * Reloads all warps from the database.
     *
     * <p>Clears caches and re-reads from SQLite.</p>
     *
     * <p>Must be called on the main thread.</p>
     */
    void reload();
}