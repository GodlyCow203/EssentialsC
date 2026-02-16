package net.godlycow.org.essc.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main interface for managing homes. All the database stuff is async so you
 * dont lag the server. Every method returns CompletableFuture.
 */
public interface HomeManager {

    /**
     * Gets a specific home by name. Returns empty if not found.
     */
    @NotNull
    CompletableFuture<Optional<Home>> getHome(@NotNull UUID player, @NotNull String name);

    /**
     * Gets all homes for a player. Returns empty list if none.
     */
    @NotNull
    CompletableFuture<List<Home>> getHomes(@NotNull UUID player);

    /**
     * How many homes does this player have?
     */
    @NotNull
    CompletableFuture<Integer> getHomeCount(@NotNull UUID player);

    /**
     * Checks if a player has a home with this name.
     */
    @NotNull
    CompletableFuture<Boolean> hasHome(@NotNull UUID player, @NotNull String name);

    /**
     * Creates or updates a home. Fires HomeCreateEvent.
     * Returns true if successful.
     */
    @NotNull
    CompletableFuture<Boolean> setHome(@NotNull Player player, @NotNull String name, @NotNull Location location);

    /**
     * Deletes a home. Fires HomeDeleteEvent.
     * Returns true if it was actually deleted (false if it didnt exist).
     */
    @NotNull
    CompletableFuture<Boolean> deleteHome(@NotNull UUID player, @NotNull String name);

    /**
     * Deletes ALL homes for a player. Use with caution!
     * Returns how many were deleted.
     */
    @NotNull
    CompletableFuture<Integer> deleteAllHomes(@NotNull UUID player);

    /**
     * Teleports a player to a home. This respects warmup, cooldown, and movement checks.
     * Fires HomeTeleportEvent and maybe HomeWarmupStartEvent.
     * Returns true if teleport was started (not necessarily completed yet).
     */
    @NotNull
    CompletableFuture<Boolean> teleport(@NotNull Player player, @NotNull Home home);

    /**
     * Teleports player to home by name. Same as above but looks up the name first.
     */
    @NotNull
    CompletableFuture<Boolean> teleport(@NotNull Player player, @NotNull String homeName);

    /**
     * Instantly teleports without warmup. Bypasses all checks.
     * Use this if you need immediate teleport.
     */
    @NotNull
    CompletableFuture<Boolean> teleportInstantly(@NotNull Player player, @NotNull Home home);

    /**
     * Cancels a pending teleport (during warmup).
     * Returns true if there was something to cancel.
     */
    boolean cancelTeleport(@NotNull Player player);

    /**
     * Is this player currently waiting to teleport?
     */
    boolean hasPendingTeleport(@NotNull Player player);


    /**
     * Max homes this player can have. Checks permissions and config.
     * Fires HomeLimitCheckEvent so other plugins can modify it.
     */
    int getMaxHomes(@NotNull Player player);

    /**
     * How many more homes can this player set?
     */
    @NotNull
    CompletableFuture<Integer> getRemainingHomes(@NotNull Player player);

    /**
     * Is this player on teleport cooldown?
     */
    boolean isOnCooldown(@NotNull Player player);

    /**
     * How many seconds left on cooldown? Returns 0 if not on cooldown.
     */
    long getRemainingCooldown(@NotNull Player player);

    /**
     * Gets any players home (admin only, bypasses normal checks).
     */
    @NotNull
    CompletableFuture<Optional<Home>> getHomeAdmin(@NotNull UUID owner, @NotNull String name);

    /**
     * Gets all homes for any player (admin only).
     */
    @NotNull
    CompletableFuture<List<Home>> getHomesAdmin(@NotNull UUID owner);

    /**
     * Sets a home for any player (admin only).
     */
    @NotNull
    CompletableFuture<Boolean> setHomeAdmin(@NotNull UUID owner, @NotNull String name, @NotNull Location location);

    /**
     * Deletes any players home (admin only).
     */
    @NotNull
    CompletableFuture<Boolean> deleteHomeAdmin(@NotNull UUID owner, @NotNull String name);
}