package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.home.HomeEntry;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API interface for interacting with EssentialsC's home system.
 *
 * <p>Manages player homes with SQLite persistence, GUI management, teleportation
 * with warmup/cooldown, and admin controls for managing other players' homes.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getHomeApi()}.</p>
 *
 * <pre>{@code
 * HomeApi home = APIProvider.getAPI().getHomeApi();
 *
 * home.getHomes(player.getUniqueId()).thenAccept(homes -> {
 *     homes.forEach(h -> player.sendMessage(h.name()));
 * });
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface HomeApi {

    /**
     * Returns the maximum number of homes the player can have.
     *
     * <p>Checks permissions (essentialsc.sethome.&lt;n&gt;) and config default.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return the maximum home count, or {@link Integer#MAX_VALUE} for unlimited
     */
    int getMaxHomes(Player player);

    /**
     * Returns the current number of homes for the player.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the player's UUID; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to the home count
     */
    CompletableFuture<Integer> getHomeCount(UUID uuid);

    /**
     * Returns whether a home with the given name exists for the player.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the player's UUID; must not be {@code null}
     * @param name the home name; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} if exists
     */
    CompletableFuture<Boolean> homeExists(UUID uuid, String name);

    /**
     * Creates or updates a home for the player at the given location.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param player   the player setting the home; must not be {@code null}
     * @param name     the home name; must not be {@code null}
     * @param location the location to set; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} on success
     */
    CompletableFuture<Boolean> setHome(Player player, String name, Location location);

    /**
     * Creates or updates a home for the specified UUID at the given location.
     *
     * <p>Admin method for setting homes for offline players.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid     the target player's UUID; must not be {@code null}
     * @param name     the home name; must not be {@code null}
     * @param location the location to set; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} on success
     */
    CompletableFuture<Boolean> setHome(UUID uuid, String name, Location location);

    /**
     * Deletes a home for the player.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the player's UUID; must not be {@code null}
     * @param name the home name to delete; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} if deleted
     */
    CompletableFuture<Boolean> deleteHome(UUID uuid, String name);

    /**
     * Returns a specific home for the player.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the player's UUID; must not be {@code null}
     * @param name the home name; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to the home, or {@code null} if not found
     */
    CompletableFuture<HomeEntry> getHome(UUID uuid, String name);

    /**
     * Returns all homes for the player.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the player's UUID; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to list of homes; never {@code null}
     */
    CompletableFuture<List<HomeEntry>> getHomes(UUID uuid);

    /**
     * Returns all UUIDs that have at least one home set.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @return a {@link CompletableFuture} resolving to set of UUIDs; never {@code null}
     */
    CompletableFuture<Set<UUID>> getAllHomeOwners();

    /**
     * Returns whether the player is currently on home teleport cooldown.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if on cooldown
     */
    boolean isOnCooldown(Player player);

    /**
     * Returns the remaining cooldown in seconds for the player.
     *
     * @param player the player to check; must not be {@code null}
     * @return remaining seconds, or {@code 0} if not on cooldown
     */
    long getRemainingCooldown(Player player);

    /**
     * Returns whether the player has a pending home teleport warmup.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if warmup is in progress
     */
    boolean hasPendingTeleport(Player player);

    /**
     * Cancels any pending home teleport for the player.
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player whose teleport to cancel; must not be {@code null}
     */
    void cancelTeleport(Player player);

    /**
     * Initiates teleportation to the specified home.
     *
     * <p>Applies warmup, cooldown, and blocked world checks. Sends appropriate
     * messages to the player.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player to teleport; must not be {@code null}
     * @param home   the home to teleport to; must not be {@code null}
     */
    void startTeleport(Player player, HomeEntry home);

    /**
     * Opens the home GUI for the player.
     *
     * <p>Only opens if GUI mode is enabled in config.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player to open GUI for; must not be {@code null}
     */
    void openGui(Player player);

    /**
     * Returns whether GUI mode is enabled for homes.
     *
     * @return {@code true} if homes use GUI interface
     */
    boolean isGuiMode();

    /**
     * Reloads the home configuration from disk.
     *
     * <p>Must be called on the main thread.</p>
     */
    void reload();
}