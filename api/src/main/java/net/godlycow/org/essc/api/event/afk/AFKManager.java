package net.godlycow.org.essc.api.event.afk;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public interface AFKManager {

    // Set a player to be AFK or active, and choose if it tells the server
    void setAFK(@NotNull Player player, boolean afk, boolean broadcast);

    // Swap the player's status: if they are AFK, make them active (and vice versa)
    void toggleAFK(@NotNull Player player);

    // Check if a player is currently marked as AFK
    boolean isAFK(@NotNull Player player);

    // Check if a player is AFK using their Unique ID
    boolean isAFK(@NotNull UUID uuid);

    // Get the exact time the player first went AFK
    Instant getAFKStartTime(@NotNull Player player);

    // See how many seconds have passed since the player went AFK
    long getAFKDurationSeconds(@NotNull Player player);

    // Get a nice, readable string of the AFK time (like "2m 30s")
    @NotNull
    String getAFKDurationFormatted(@NotNull Player player);

    // Get a list of every online player who is currently AFK
    @NotNull
    Set<Player> getAFKPlayers();

    // Get the total number of players who are AFK
    int getAFKCount();

    // Get the total number of players currently on the server
    int getOnlineCount();

    // Tell the system the player did something; this usually ends their AFK status
    void updateActivity(@NotNull Player player);

    // Get the last time the player did an action
    Instant getLastActivity(@NotNull Player player);

    // See how many seconds have passed since the player's last action
    long getInactiveSeconds(@NotNull Player player);

    // Check if the AFK system is actually turned on in the settings
    boolean isEnabled();

    // Check if the player is currently stuck in place while AFK
    boolean isFrozen(@NotNull Player player);

    // Check if a specific command is banned while a player is AFK
    boolean isCommandBlocked(@NotNull String command);

    // Refresh the player's name in the Tab list (usually to add an [AFK] tag)
    void updatePlayerListName(@NotNull Player player);

    // Remove AFK status from every player on the server at once
    void clearAllAFK();

    // Refresh the plugin's settings and configuration files
    void reload();
}