package net.godlycow.org.essc.api.event.home;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public interface HomeManager {

    // Look up a specific home by name for a player
    @NotNull
    CompletableFuture<Optional<Home>> getHome(@NotNull UUID player, @NotNull String name);

    // Get a list of every home a player has created
    @NotNull
    CompletableFuture<List<Home>> getHomes(@NotNull UUID player);

    // Count how many homes a player currently owns
    @NotNull
    CompletableFuture<Integer> getHomeCount(@NotNull UUID player);

    // Check if a player already has a home with a specific name
    @NotNull
    CompletableFuture<Boolean> hasHome(@NotNull UUID player, @NotNull String name);

    // Create a new home or update an existing one at the player's current spot
    @NotNull
    CompletableFuture<Boolean> setHome(@NotNull Player player, @NotNull String name, @NotNull Location location);

    // Remove a specific home from a player's list
    @NotNull
    CompletableFuture<Boolean> deleteHome(@NotNull UUID player, @NotNull String name);

    // Wipe all homes for a player (cannot be undone)
    @NotNull
    CompletableFuture<Integer> deleteAllHomes(@NotNull UUID player);

    // Start the teleport process, including any wait times or movement checks
    @NotNull
    CompletableFuture<Boolean> teleport(@NotNull Player player, @NotNull Home home);

    // Find a home by name and then start the teleport process
    @NotNull
    CompletableFuture<Boolean> teleport(@NotNull Player player, @NotNull String homeName);

    // Move the player to a home immediately, skipping all waits and checks
    @NotNull
    CompletableFuture<Boolean> teleportInstantly(@NotNull Player player, @NotNull Home home);

    // Stop a teleport while the player is still in the waiting (warmup) period
    boolean cancelTeleport(@NotNull Player player);

    // Check if a player is currently in the middle of a teleport warmup
    boolean hasPendingTeleport(@NotNull Player player);

    // See the maximum number of homes a player is allowed to have
    int getMaxHomes(@NotNull Player player);

    // Calculate how many more homes a player can set before hitting their limit
    @NotNull
    CompletableFuture<Integer> getRemainingHomes(@NotNull Player player);

    // Check if the player has to wait before they can teleport again
    boolean isOnCooldown(@NotNull Player player);

    // See how many seconds are left until the player can teleport again
    long getRemainingCooldown(@NotNull Player player);

    // (Admin) Look up a home belonging to any player
    @NotNull
    CompletableFuture<Optional<Home>> getHomeAdmin(@NotNull UUID owner, @NotNull String name);

    // (Admin) Get a list of all homes belonging to any player
    @NotNull
    CompletableFuture<List<Home>> getHomesAdmin(@NotNull UUID owner);

    // (Admin) Forcefully set a home for another player
    @NotNull
    CompletableFuture<Boolean> setHomeAdmin(@NotNull UUID owner, @NotNull String name, @NotNull Location location);

    // (Admin) Forcefully remove a home belonging to another player
    @NotNull
    CompletableFuture<Boolean> deleteHomeAdmin(@NotNull UUID owner, @NotNull String name);
}