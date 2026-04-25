package net.godlycow.org.essc.api.rtp;

import org.bukkit.Location;
import org.bukkit.World;

public interface RtpResult {
    boolean wasSuccessful();
    Location getDestination();
    World getWorld();
    String getFailureReason();
    long getRequestTimestamp();
    long getCompletionTimestamp();
    int getSearchAttempts();
}