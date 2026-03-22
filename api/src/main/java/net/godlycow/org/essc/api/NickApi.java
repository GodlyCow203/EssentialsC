package net.godlycow.org.essc.api;

import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API interface for interacting with EssentialsC's nickname system.
 *
 * <p>Nicknames are stored in SQLite and cached in memory on player join.
 * All database operations return {@link CompletableFuture} — do not block
 * the main thread waiting on them. Cache reads ({@link #getCachedNickname})
 * are synchronous and safe to call anywhere.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getNickApi()}.</p>
 *
 * <pre>{@code
 * NickApi nick = APIProvider.getAPI().getNickApi();
 *
 * nick.getNickname(player.getUniqueId()).thenAccept(opt -> {
 *     String display = opt.orElse(player.getName());
 *     player.sendMessage("Your name: " + display);
 * });
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface NickApi {

    /**
     * Returns the stored nickname for the given player, if one exists.
     *
     * <p>Checks the in-memory cache first; falls back to the database if the
     * player is not cached. The optional is empty if the player has no nickname set.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the UUID of the player; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to an {@link Optional} containing
     *         the raw MiniMessage nickname string, or {@link Optional#empty()} if none
     */
    CompletableFuture<Optional<String>> getNickname(UUID uuid);

    /**
     * Returns the cached nickname for the given player synchronously, or {@code null}
     * if the player is not in the cache (not yet loaded or has no nickname).
     *
     * <p>Safe to call on the main thread. Does not hit the database.</p>
     *
     * @param uuid the UUID of the player; must not be {@code null}
     * @return the raw MiniMessage nickname string, or {@code null} if not cached
     */
    String getCachedNickname(UUID uuid);

    /**
     * Looks up the UUID of the player who owns the given nickname.
     *
     * <p>Matching is case-insensitive. The optional is empty if no player
     * has that nickname set.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param nickname the nickname to search for; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to an {@link Optional} containing
     *         the owner's UUID, or {@link Optional#empty()} if not found
     */
    CompletableFuture<Optional<UUID>> getUUIDByNickname(String nickname);

    /**
     * Returns whether the given nickname is already in use by another player.
     *
     * <p>The {@code excludeUuid} parameter allows you to exclude the player
     * currently holding the nickname from the check — useful when updating a
     * player's own nickname.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param nickname    the nickname to check; must not be {@code null}
     * @param excludeUuid the UUID of the player to exclude from the check;
     *                    must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} if another
     *         player already has this nickname
     */
    CompletableFuture<Boolean> isNicknameTaken(String nickname, UUID excludeUuid);


    /**
     * Sets or overwrites the nickname for the given player.
     *
     * <p>The nickname string may contain MiniMessage formatting. The value is
     * written to the database and the in-memory cache is updated immediately.
     * This method does <em>not</em> apply the nickname visually — call
     * {@link #applyNickname(Player)} afterwards if the player is online.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid     the UUID of the player; must not be {@code null}
     * @param nickname the nickname to set (may contain MiniMessage tags);
     *                 must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} on success
     */
    CompletableFuture<Boolean> setNickname(UUID uuid, String nickname);

    /**
     * Removes the nickname for the given player.
     *
     * <p>Deletes the record from the database and removes the entry from the
     * cache. The future resolves {@code false} if the player had no nickname set.
     * This method does <em>not</em> reset the player's display name — call
     * {@link #clearNickname(Player)} afterwards if the player is online.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the UUID of the player; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} if a nickname
     *         was found and removed, {@code false} if the player had none
     */
    CompletableFuture<Boolean> removeNickname(UUID uuid);


    /**
     * Applies the player's stored nickname as their Adventure display name and
     * updates the tab list.
     *
     * <p>Fetches the nickname from cache or database asynchronously, then applies
     * it on the main thread. Has no effect if the nick system is disabled in config
     * or if the player has no nickname set.</p>
     *
     * <p>Safe to call from any thread — scheduling is handled internally.</p>
     *
     * @param player the online player to apply the nickname to; must not be {@code null}
     */
    void applyNickname(Player player);

    /**
     * Resets the player's display name to their real username and clears their
     * nickname from the cache.
     *
     * <p>Also triggers a tab list update. Does not remove the nickname from the
     * database — use {@link #removeNickname(UUID)} for permanent removal.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the online player whose nickname to clear; must not be {@code null}
     */
    void clearNickname(Player player);
}