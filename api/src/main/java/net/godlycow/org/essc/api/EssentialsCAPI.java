package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.home.HomeManager;
import net.godlycow.org.essc.api.kit.KitManager;
import net.godlycow.org.essc.api.rtp.RtpManager;
import net.godlycow.org.essc.api.warp.WarpManager;

public interface EssentialsCAPI {
    KitManager getKitManager();
    RtpManager getRtpManager();
    HomeManager getHomeManager();
    WarpManager getWarpManager();
    boolean isKitSystemEnabled();
    boolean isRtpSystemEnabled();
    boolean isHomeSystemEnabled();
    boolean isWarpSystemEnabled();
    String getApiVersion();
}