package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.home.HomeManager;
import net.godlycow.org.essc.api.kit.KitManager;
import net.godlycow.org.essc.api.rtp.RtpManager;

public interface EssentialsCAPI {
    KitManager getKitManager();
    RtpManager getRtpManager();
    HomeManager getHomeManager();
    boolean isKitSystemEnabled();
    boolean isRtpSystemEnabled();
    boolean isHomeSystemEnabled();
    String getApiVersion();
}