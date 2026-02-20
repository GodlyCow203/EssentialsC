package net.godlycow.org.essc.api.event.back;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public interface BackManager {

    // Save a location that the player can return to later
    void setBackLocation(@NotNull Player player, @NotNull Location location);

    // Look up the saved back location for a player
    @NotNull
    Optional<Back> getBackLocation(@NotNull Player player);

    // Look up the saved back location using a UUID (works even if player is offline)
    @NotNull
    Optional<Back> getBackLocation(@NotNull UUID uuid);

    // Check if a player has a back location stored
    boolean hasBackLocation(@NotNull Player player);

    // Check if a player has a back location stored using their UUID
    boolean hasBackLocation(@NotNull UUID uuid);

    // Remove a player's back location
    void removeBackLocation(@NotNull Player player);

    // Remove a back location using a UUID
    void removeBackLocation(@NotNull UUID uuid);

    // Start the teleport process to go back, including any wait times
    @NotNull
    CompletableFuture<Boolean> teleport(@NotNull Player player);

    // Teleport the player back immediately, skipping all waits
    @NotNull
    CompletableFuture<Boolean> teleportInstantly(@NotNull Player player);

    // Stop a back teleport while the player is still in the waiting period
    boolean cancelTeleport(@NotNull Player player);

    // Check if a player is currently waiting to teleport back
    boolean hasPendingTeleport(@NotNull Player player);

    // Check if the player has to wait before using /back again
    boolean isOnCooldown(@NotNull Player player);

    // See how many seconds are left until the player can use /back again
    long getRemainingCooldown(@NotNull Player player);

    // Get how long players must wait before teleporting (in seconds)
    long getWarmupSeconds();

    // Get how long players must wait between uses (in seconds)
    long getCooldownSeconds();

    // Check if the back system is turned on
    boolean isEnabled();

    // Reload the back configuration
    void reload();
}