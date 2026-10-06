package net.godlycow.org.essc.api.rtp;

import java.util.UUID;

/**
 * A player's RTP state. See {@link RtpManager#getPlayerState(org.bukkit.entity.Player)}.
 */
public interface RtpPlayerState {
    /**
     * Gets the player.
     *
     * @return the player UUID
     */
    UUID getPlayerId();

    /**
     * Checks if an RTP is running for the player.
     *
     * @return true if in progress
     */
    boolean isRtpInProgress();

    /**
     * Checks if the player is on cooldown.
     *
     * @return true if on cooldown
     */
    boolean isOnCooldown();

    /**
     * Gets remaining cooldown in seconds, 0 if none.
     *
     * @return remaining seconds
     */
    long getRemainingCooldownSeconds();

    /**
     * Gets the last completed RTP, in millis.
     *
     * @return last RTP time in millis
     */
    long getLastRtpTimestamp();

    /**
     * Gets the lifetime RTP count.
     *
     * @return total RTPs
     */
    int getTotalRtpCount();

    /**
     * Checks if the player has a warmup pending.
     *
     * @return true if warming up
     */
    boolean hasPendingWarmup();
}
