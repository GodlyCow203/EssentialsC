package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.EssentialsCAPI;
import net.godlycow.org.essc.api.kit.KitManager;
import net.godlycow.org.essc.api.impl.kit.KitManagerImpl;

public class EssentialsCAPIImpl implements EssentialsCAPI {
    private final EssentialsC plugin;
    private final KitManagerImpl kitManagerImpl;
    private static final String API_VERSION = "1.0.0";

    public EssentialsCAPIImpl(EssentialsC plugin) {
        this.plugin = plugin;
        this.kitManagerImpl = new KitManagerImpl(plugin);
    }

    @Override
    public KitManager getKitManager() {
        return kitManagerImpl;
    }

    @Override
    public boolean isKitSystemEnabled() {
        if (plugin.getKitManager() == null) {
            return false;
        }
        return true;
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }
}