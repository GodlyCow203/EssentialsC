package net.godlycow.org.essc.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public interface HomeManager {

    /**
     * Gets a specific home by name.
     * @param player The player UUID
     * @param name The home name
     * @return CompletableFuture with Optional containing the home if found
     */
    @NotNull
    CompletableFuture<Optional<Home>> getHome(@NotNull UUID player, @NotNull String name);

    /**
     * Gets all homes for a player.
     * @param player The player UUID
     * @return CompletableFuture with list of homes
     */
    @NotNull
    CompletableFuture<List<Home>> getHomes(@NotNull UUID player);

    /**
     * Gets the count of homes for a player.
     * @param player The player UUID
     * @return CompletableFuture with home count
     */
    @NotNull
    CompletableFuture<Integer> getHomeCount(@NotNull UUID player);

    /**
     * Checks if a home exists.
     * @param player The player UUID
     * @param name The home name
     * @return CompletableFuture with true if exists
     */
    @NotNull
    CompletableFuture<Boolean> hasHome(@NotNull UUID player, @NotNull String name);

    /**
     * Sets (creates or updates) a home.
     * @param player The player
     * @param name The home name
     * @param location The location
     * @return CompletableFuture with true if successful
     */
    @NotNull
    CompletableFuture<Boolean> setHome(@NotNull Player player, @NotNull String name, @NotNull Location location);

    /**
     * Deletes a home.
     * @param player The player UUID
     * @param name The home name
     * @return CompletableFuture with true if deleted
     */
    @NotNull
    CompletableFuture<Boolean> deleteHome(@NotNull UUID player, @NotNull String name);

    /**
     * Deletes all homes for a player.
     * @param player The player UUID
     * @return CompletableFuture with count of deleted homes
     */
    @NotNull
    CompletableFuture<Integer> deleteAllHomes(@NotNull UUID player);

    /**
     * Teleports a player to their home.
     * This respects warmup, cooldown, and cancellation settings.
     * @param player The player to teleport
     * @param home The home to teleport to
     * @return CompletableFuture with true if teleport initiated
     */
    @NotNull
    CompletableFuture<Boolean> teleport(@NotNull Player player, @NotNull Home home);

    /**
     * Teleports a player to a home by name.
     * @param player The player
     * @param homeName The home name
     * @return CompletableFuture with true if teleport initiated
     */
    @NotNull
    CompletableFuture<Boolean> teleport(@NotNull Player player, @NotNull String homeName);

    /**
     * Instantly teleports a player to a home (bypasses warmup).
     * @param player The player
     * @param home The home
     * @return CompletableFuture with true if teleported
     */
    @NotNull
    CompletableFuture<Boolean> teleportInstantly(@NotNull Player player, @NotNull Home home);

    /**
     * Cancels a pending teleport for a player.
     * @param player The player
     * @return true if a teleport was cancelled
     */
    boolean cancelTeleport(@NotNull Player player);

    /**
     * Checks if a player has a pending teleport.
     * @param player The player
     * @return true if waiting to teleport
     */
    boolean hasPendingTeleport(@NotNull Player player);

    /**
     * Gets the maximum homes allowed for a player.
     * @param player The player
     * @return Max homes (Integer.MAX_VALUE for unlimited)
     */
    int getMaxHomes(@NotNull Player player);

    /**
     * Gets remaining home slots for a player.
     * @param player The player
     * @return CompletableFuture with remaining slots
     */
    @NotNull
    CompletableFuture<Integer> getRemainingHomes(@NotNull Player player);

    /**
     * Checks if a player is on teleport cooldown.
     * @param player The player
     * @return true if on cooldown
     */
    boolean isOnCooldown(@NotNull Player player);

    /**
     * Gets remaining cooldown seconds.
     * @param player The player
     * @return Seconds remaining (0 if not on cooldown)
     */
    long getRemainingCooldown(@NotNull Player player);

    /**
     * Gets any player's home (admin only).
     * @param owner The owner UUID
     * @param name The home name
     * @return CompletableFuture with Optional home
     */
    @NotNull
    CompletableFuture<Optional<Home>> getHomeAdmin(@NotNull UUID owner, @NotNull String name);

    /**
     * Gets all homes for any player (admin only).
     * @param owner The owner UUID
     * @return CompletableFuture with list of homes
     */
    @NotNull
    CompletableFuture<List<Home>> getHomesAdmin(@NotNull UUID owner);

    /**
     * Sets a home for any player (admin only).
     * @param owner The owner UUID
     * @param name The home name
     * @param location The location
     * @return CompletableFuture with true if successful
     */
    @NotNull
    CompletableFuture<Boolean> setHomeAdmin(@NotNull UUID owner, @NotNull String name, @NotNull Location location);

    /**
     * Deletes any player's home (admin only).
     * @param owner The owner UUID
     * @param name The home name
     * @return CompletableFuture with true if deleted
     */
    @NotNull
    CompletableFuture<Boolean> deleteHomeAdmin(@NotNull UUID owner, @NotNull String name);
}