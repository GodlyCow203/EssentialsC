package net.godlycow.org.essc.api.kit;

import java.util.UUID;

public interface KitClaimProfile {
    UUID getPlayerId();
    String getKitName();
    long getLastClaimedTimestamp();
    int getTotalClaimCount();
    boolean hasEverClaimed();
}