package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.kit.KitManager;

public interface EssentialsCAPI {
    KitManager getKitManager();
    boolean isKitSystemEnabled();
    String getApiVersion();
}