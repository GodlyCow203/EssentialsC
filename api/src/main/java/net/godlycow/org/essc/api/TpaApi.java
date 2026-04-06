package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.teleport.TPARequestEntry;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * API interface for interacting with EssentialsC's TPA (teleport request) system.
 *
 * <p>Manages teleport requests between players with cooldowns, warmup, economy costs,
 * and safety checks (movement, damage). Supports both TPA and TPAHere request types.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getTpaApi()}.</p>
 *
 * <pre>{@code
 * TpaApi tpa = APIProvider.getAPI().getTpaApi();
 *
 * tpa.requestTeleport(requester, target, TPARequestEntry.Type.TPA);
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface TpaApi {

    /**
     * Sends a teleport request from one player to another.
     *
     * <p>Validates cooldowns, blocked status, ignores, and economy funds.
     * Sends appropriate messages to both players on success or failure.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param requester the player sending the request; must not be {@code null}
     * @param target    the player receiving the request; must not be {@code null}
     * @param type      the type of request (TPA or TPAHERE); must not be {@code null}
     * @return {@code true} if the request was sent successfully
     */
    boolean requestTeleport(Player requester, Player target, TPARequestEntry.Type type);

    /**
     * Accepts a pending teleport request from the requester to the target.
     *
     * <p>Initiates the teleport warmup and deducts economy cost if configured.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param target    the player accepting the request; must not be {@code null}
     * @param requester the player who sent the request; must not be {@code null}
     * @return {@code true} if a valid request was found and accepted
     */
    boolean acceptRequest(Player target, Player requester);

    /**
     * Denies a pending teleport request from the requester to the target.
     *
     * <p>Notifies both players of the denial.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param target    the player denying the request; must not be {@code null}
     * @param requester the player who sent the request; must not be {@code null}
     * @return {@code true} if a valid request was found and denied
     */
    boolean denyRequest(Player target, Player requester);

    /**
     * Cancels an outgoing teleport request from the requester to the target.
     *
     * <p>Notifies the requester of the cancellation.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param requester the player cancelling their request; must not be {@code null}
     * @param target    the target player of the request; must not be {@code null}
     * @return {@code true} if a valid outgoing request was found and cancelled
     */
    boolean cancelRequest(Player requester, Player target);

    /**
     * Toggles TPA requests on/off for the given player.
     *
     * <p>When disabled, other players cannot send TPA requests to this player.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player to toggle; must not be {@code null}
     */
    void toggleTPA(Player player);

    /**
     * Toggles ignore status for a specific player.
     *
     * <p>When ignoring a player, their TPA requests will be silently rejected.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player toggling ignore; must not be {@code null}
     * @param target the player to ignore/unignore; must not be {@code null}
     */
    void toggleIgnore(Player player, Player target);

    /**
     * Returns all incoming TPA requests for the given player.
     *
     * <p>Automatically cleans up expired requests before returning.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return an unmodifiable list of incoming requests; never {@code null}
     */
    List<TPARequestEntry> getIncomingRequests(Player player);

    /**
     * Returns all outgoing TPA requests for the given player.
     *
     * <p>Automatically cleans up expired requests before returning.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return an unmodifiable list of outgoing requests; never {@code null}
     */
    List<TPARequestEntry> getOutgoingRequests(Player player);

    /**
     * Returns whether the player has any pending incoming requests.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if there are pending incoming requests
     */
    boolean hasIncomingRequests(Player player);

    /**
     * Returns whether the player has any pending outgoing requests.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if there are pending outgoing requests
     */
    boolean hasOutgoingRequests(Player player);

    /**
     * Returns whether the player has TPA requests blocked.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player is blocking TPA requests
     */
    boolean isBlocking(Player player);

    /**
     * Cancels an active teleport warmup for the given player.
     *
     * <p>Called automatically on movement, damage, or disconnect.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player whose teleport to cancel; must not be {@code null}
     * @param reason the cancellation reason key (e.g., "move", "damage")
     */
    void cancelTeleport(Player player, String reason);

    /**
     * Returns whether the player is currently in teleport warmup.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if a teleport is in progress
     */
    boolean isInTeleport(Player player);

    /**
     * Reloads the TPA configuration from disk.
     *
     * <p>Re-reads cooldown, warmup, timeout, and cost settings from config.</p>
     *
     * <p>Must be called on the main thread.</p>
     */
    void reload();
}