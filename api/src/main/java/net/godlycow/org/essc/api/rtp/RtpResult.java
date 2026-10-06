package net.godlycow.org.essc.api.rtp;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * What came out of an RTP request. Check {@link #wasSuccessful()} first,
 * on failure only the reason and timestamps mean anything.
 */
public interface RtpResult {
    /**
     * Checks if the teleport worked.
     *
     * @return true on success
     */
    boolean wasSuccessful();

    /**
     * Gets where the player landed, null on failure.
     *
     * @return the destination, or null
     */
    Location getDestination();

    /**
     * Gets the world the request ran in.
     *
     * @return the world
     */
    World getWorld();

    /**
     * Gets why it failed, null on success.
     *
     * @return failure reason, or null
     */
    String getFailureReason();

    /**
     * Gets when the request was made, in millis.
     *
     * @return request time in millis
     */
    long getRequestTimestamp();

    /**
     * Gets when the request finished, in millis.
     *
     * @return completion time in millis
     */
    long getCompletionTimestamp();

    /**
     * Gets how many spots were tried.
     *
     * @return search attempts
     */
    int getSearchAttempts();
}
