package net.godlycow.org.essc.api.rtp;

import java.util.List;

/**
 * RTP settings for one world. Get them from {@link RtpManager}.
 */
public interface RtpWorldSettings {
    /**
     * Gets the world name.
     *
     * @return the world name
     */
    String getWorldName();

    /**
     * Gets the display name shown to players.
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Gets the min teleport radius in blocks.
     *
     * @return min radius
     */
    int getMinRadius();

    /**
     * Gets the max teleport radius in blocks.
     *
     * @return max radius
     */
    int getMaxRadius();

    /**
     * Gets biomes RTP will not land in.
     *
     * @return blocked biomes, never null
     */
    List<String> getBlockedBiomes();

    /**
     * Checks if RTP is on for this world.
     *
     * @return true if enabled
     */
    boolean isEnabled();
}
