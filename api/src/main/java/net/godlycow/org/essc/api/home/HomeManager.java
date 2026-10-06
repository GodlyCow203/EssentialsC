package net.godlycow.org.essc.api.home;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The homes system. Get it from {@link net.godlycow.org.essc.api.EssentialsCAPI#getHomeManager()}.
 * Reads are cached and async, writes go through the same checks as the commands.
 */
public interface HomeManager {
    /**
     * Checks if homes are enabled.
     *
     * @return true if enabled
     */
    boolean isHomeSystemEnabled();

    /**
     * Gets one home.
     *
     * @param owner the owner UUID
     * @param name the home name
     * @return future with the home, or null if missing
     */
    CompletableFuture<Home> fetchHome(UUID owner, String name);

    /**
     * Gets all homes of a player.
     *
     * @param owner the owner UUID
     * @return future with the homes, empty if none
     */
    CompletableFuture<List<Home>> fetchHomes(UUID owner);

    /**
     * Checks if a home exists.
     *
     * @param owner the owner UUID
     * @param name the home name
     * @return future with true if it exists
     */
    CompletableFuture<Boolean> homeExists(UUID owner, String name);

    /**
     * Counts a player's homes.
     *
     * @param owner the owner UUID
     * @return future with the count
     */
    CompletableFuture<Integer> getHomeCount(UUID owner);

    /**
     * Sets a home for an online player.
     *
     * @param player the owner, must be online
     * @param name the home name
     * @param location the home location
     * @return future with true on success
     */
    CompletableFuture<Boolean> setHome(Player player, String name, Location location);

    /**
     * Sets a home by UUID, works offline too.
     *
     * @param owner the owner UUID
     * @param name the home name
     * @param location the home location
     * @return future with true on success
     */
    CompletableFuture<Boolean> setHome(UUID owner, String name, Location location);

    /**
     * Deletes a home.
     *
     * @param owner the owner UUID
     * @param name the home name
     * @return future with true if something was deleted
     */
    CompletableFuture<Boolean> deleteHome(UUID owner, String name);

    /**
     * Gets how many homes a player may have.
     *
     * @param player the player
     * @return max homes
     */
    int getMaxHomes(Player player);

    /**
     * Gets cached home names without hitting storage.
     *
     * @param owner the owner UUID
     * @return cached home names
     */
    Collection<String> getCachedHomeNames(UUID owner);

    /**
     * Clears a player's home cache.
     *
     * @param owner the owner UUID
     */
    void clearCache(UUID owner);

    /**
     * Checks if the player is on home cooldown.
     *
     * @param player the player
     * @return true if on cooldown
     */
    boolean isOnCooldown(Player player);

    /**
     * Gets remaining home cooldown in seconds, 0 if none.
     *
     * @param player the player
     * @return remaining seconds
     */
    long getRemainingCooldownSeconds(Player player);

    /**
     * Checks if the player has a teleport warming up.
     *
     * @param player the player
     * @return true if a teleport is pending
     */
    boolean hasPendingTeleport(Player player);

    /**
     * Cancels the player's pending teleport, if any.
     *
     * @param player the player
     */
    void cancelTeleport(Player player);

    /**
     * Starts a home teleport with warmup and cooldown.
     *
     * @param player the player
     * @param home the destination
     */
    void startTeleport(Player player, Home home);

    /**
     * Reloads home config from disk.
     */
    void reload();
}
