package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.home.HomeManager;
import net.godlycow.org.essc.api.kit.KitManager;
import net.godlycow.org.essc.api.rtp.RtpManager;
import net.godlycow.org.essc.api.warp.WarpManager;

/**
 * The main API. Grab it from {@link APIProvider#get()} and use the managers
 * below. Managers are always returned, even if their system is turned off,
 * in which case they just do nothing. Check the is*Enabled methods first
 * if that matters to you.
 */
public interface EssentialsCAPI {
    /**
     * Gets the kit manager.
     *
     * @return the kit manager, never null
     */
    KitManager getKitManager();

    /**
     * Gets the RTP manager.
     *
     * @return the RTP manager, never null
     */
    RtpManager getRtpManager();

    /**
     * Gets the homes manager.
     *
     * @return the home manager, never null
     */
    HomeManager getHomeManager();

    /**
     * Gets the warp manager.
     *
     * @return the warp manager, never null
     */
    WarpManager getWarpManager();

    /**
     * Checks if kits are enabled.
     *
     * @return true if enabled
     */
    boolean isKitSystemEnabled();

    /**
     * Checks if RTP is enabled.
     *
     * @return true if enabled
     */
    boolean isRtpSystemEnabled();

    /**
     * Checks if homes are enabled.
     *
     * @return true if enabled
     */
    boolean isHomeSystemEnabled();

    /**
     * Checks if warps are enabled.
     *
     * @return true if enabled
     */
    boolean isWarpSystemEnabled();

    /**
     * Gets the API version, for example "1.3.0". Separate from the plugin version.
     *
     * @return the API version, never null
     */
    String getApiVersion();
}
