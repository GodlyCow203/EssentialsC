package net.godlycow.org.essc.api.warp;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The warp system. Get it from {@link net.godlycow.org.essc.api.EssentialsCAPI#getWarpManager()}.
 * If warps are disabled lookups come back empty and writes return false.
 */
public interface WarpManager {
    /**
     * Checks if warps are enabled.
     *
     * @return true if enabled
     */
    boolean isWarpSystemEnabled();

    /**
     * Gets a warp by name.
     *
     * @param name the warp name
     * @return the warp, or null if missing
     */
    Warp getWarp(String name);

    /**
     * Checks if a warp exists.
     *
     * @param name the warp name
     * @return true if it exists
     */
    boolean warpExists(String name);

    /**
     * Gets all warps, including hidden ones.
     *
     * @return all warps, never null
     */
    Collection<Warp> getAllWarps();

    /**
     * Gets only public warps.
     *
     * @return visible warps, never null
     */
    Collection<Warp> getVisibleWarps();

    /**
     * Gets warps in one category.
     *
     * @param category the category name
     * @return warps in the category, empty if none
     */
    Collection<Warp> getWarpsByCategory(String category);

    /**
     * Gets all category names.
     *
     * @return category names, never null
     */
    Set<String> getCategories();

    /**
     * Creates a warp.
     *
     * @param name the warp name
     * @param location the warp location
     * @return future with true on success
     */
    CompletableFuture<Boolean> createWarp(String name, Location location);

    /**
     * Deletes a warp.
     *
     * @param name the warp name
     * @return future with true if something was deleted
     */
    CompletableFuture<Boolean> deleteWarp(String name);

    /**
     * Saves changes made to a warp.
     *
     * @param warp the changed warp
     * @return future with true on success
     */
    CompletableFuture<Boolean> updateWarp(Warp warp);

    /**
     * Counts how often a player used a warp.
     *
     * @param playerId the player UUID
     * @param warpName the warp name
     * @return future with the usage count
     */
    CompletableFuture<Integer> getWarpUsage(UUID playerId, String warpName);

    /**
     * Checks if the player is on warp cooldown.
     *
     * @param player the player
     * @return true if on cooldown
     */
    boolean isOnCooldown(Player player);

    /**
     * Gets remaining warp cooldown in seconds, 0 if none.
     *
     * @param player the player
     * @return remaining seconds
     */
    long getRemainingCooldownSeconds(Player player);

    /**
     * Checks if the player has a warp warming up.
     *
     * @param player the player
     * @return true if pending
     */
    boolean hasPendingWarp(Player player);

    /**
     * Cancels the player's pending warp, if any.
     *
     * @param player the player
     */
    void cancelWarp(Player player);

    /**
     * Reloads warps from disk.
     */
    void reload();
}
