package net.godlycow.org.essc.api.rtp;

import java.util.List;

public interface RtpWorldSettings {
    String getWorldName();
    String getDisplayName();
    int getMinRadius();
    int getMaxRadius();
    List<String> getBlockedBiomes();
    boolean isEnabled();
}