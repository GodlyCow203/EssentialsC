package net.godlycow.org.essc.api.rtp;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A running RTP request. See {@link RtpManager#getActiveRequest(Player)}.
 */
public interface RtpRequest {
    /**
     * Gets the request id.
     *
     * @return the request id
     */
    UUID getRequestId();

    /**
     * Gets the player.
     *
     * @return the player
     */
    Player getPlayer();

    /**
     * Gets the world being teleported in.
     *
     * @return the target world
     */
    World getTargetWorld();

    /**
     * Gets when the request was made, in millis.
     *
     * @return request time in millis
     */
    long getRequestTimestamp();

    /**
     * Checks if this request has a warmup.
     *
     * @return true if warmup required
     */
    boolean wasWarmupRequired();

    /**
     * Gets the warmup in seconds, 0 if none.
     *
     * @return warmup seconds
     */
    long getWarmupSeconds();
}
