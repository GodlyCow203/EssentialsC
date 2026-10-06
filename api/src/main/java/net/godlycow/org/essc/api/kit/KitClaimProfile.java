package net.godlycow.org.essc.api.kit;

import java.util.UUID;

/**
 * A player's claim history for one kit. Get it from
 * {@link KitManager#fetchClaimProfile(org.bukkit.entity.Player, Kit)}.
 */
public interface KitClaimProfile {
    /**
     * Gets the player.
     *
     * @return the player UUID
     */
    UUID getPlayerId();

    /**
     * Gets the kit name.
     *
     * @return the kit name
     */
    String getKitName();

    /**
     * Gets when the kit was last claimed, in millis. 0 if never.
     *
     * @return last claim time in millis
     */
    long getLastClaimedTimestamp();

    /**
     * Gets the total claim count.
     *
     * @return total claims
     */
    int getTotalClaimCount();

    /**
     * Checks if the player ever claimed this kit.
     *
     * @return true if claimed at least once
     */
    boolean hasEverClaimed();
}
