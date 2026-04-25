package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.kit.KitManager;
import net.godlycow.org.essc.api.rtp.RtpManager;

public interface EssentialsCAPI {
    KitManager getKitManager();
    RtpManager getRtpManager();
    boolean isKitSystemEnabled();
    boolean isRtpSystemEnabled();
    String getApiVersion();
}