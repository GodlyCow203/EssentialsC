package net.godlycow.org.essc.api.rtp;

import java.util.UUID;

public interface RtpPlayerState {
    UUID getPlayerId();
    boolean isRtpInProgress();
    boolean isOnCooldown();
    long getRemainingCooldownSeconds();
    long getLastRtpTimestamp();
    int getTotalRtpCount();
    boolean hasPendingWarmup();
}