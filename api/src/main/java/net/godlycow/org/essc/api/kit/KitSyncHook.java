package net.godlycow.org.essc.api.kit;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Hook interface for cross-server kit claim synchronization.
 *
 * <p>Implement this interface to enable network-wide kit cooldown tracking
 * across multiple servers (e.g., via Velocity proxy and MySQL).</p>
 *
 * <p>Register your implementation via {@link net.godlycow.org.essc.api.KitApi#setNetworkSyncHook}.</p>
 *
 * @see net.godlycow.org.essc.api.KitApi
 */
public interface KitSyncHook {

    /**
     * Called when a player claims a kit with network-sync enabled.
     *
     * <p>Use this to broadcast the claim to other servers in the network.</p>
     *
     * @param uuid      the player's UUID
     * @param kitName   the kit name (lowercase)
     * @param claimedAt timestamp when the kit was claimed (epoch millis)
     * @param serverId  identifier of the server where the claim occurred
     */
    void onKitClaimed(UUID uuid, String kitName, long claimedAt, String serverId);

    /**
     * Query the network for the most recent claim time of a kit.
     *
     * <p>This is called asynchronously when checking cooldowns for kits with
     * network sync enabled. Return the most recent claim timestamp from any
     * server in the network, or 0 if never claimed.</p>
     *
     * @param uuid    the player's UUID
     * @param kitName the kit name (lowercase)
     * @return future completing with the last claim timestamp (epoch millis), or 0
     */
    CompletableFuture<Long> getNetworkLastClaimed(UUID uuid, String kitName);
}