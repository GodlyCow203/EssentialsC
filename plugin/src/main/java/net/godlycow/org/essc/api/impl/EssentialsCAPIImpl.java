package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.EssentialsCAPI;
import net.godlycow.org.essc.api.home.HomeManager;
import net.godlycow.org.essc.api.impl.home.HomeManagerImpl;
import net.godlycow.org.essc.api.kit.KitManager;
import net.godlycow.org.essc.api.rtp.RtpManager;
import net.godlycow.org.essc.api.impl.kit.KitManagerImpl;
import net.godlycow.org.essc.api.impl.rtp.RtpManagerImpl;

public class EssentialsCAPIImpl implements EssentialsCAPI {
    private final EssentialsC plugin;
    private final KitManagerImpl kitManagerImpl;
    private final RtpManagerImpl rtpManagerImpl;
    private final HomeManagerImpl homeManagerImpl;
    private static final String API_VERSION = "1.1.0";

    public EssentialsCAPIImpl(EssentialsC plugin) {
        this.plugin = plugin;
        this.kitManagerImpl = new KitManagerImpl(plugin);
        this.rtpManagerImpl = new RtpManagerImpl(plugin);
        this.homeManagerImpl = new HomeManagerImpl(plugin);
    }

    @Override
    public KitManager getKitManager() {
        return kitManagerImpl;
    }

    @Override
    public RtpManager getRtpManager() {
        return rtpManagerImpl;
    }

    @Override
    public HomeManager getHomeManager() {
        return homeManagerImpl;
    }

    @Override
    public boolean isKitSystemEnabled() {
        if (plugin.getKitManager() == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isRtpSystemEnabled() {
        if (plugin.getRtpManager() == null) {
            return false;
        }
        return plugin.getConfigManager().isRTPEnabled();
    }

    @Override
    public boolean isHomeSystemEnabled() {
        if (plugin.getHomeManager() == null) {
            return false;
        }
        return true;
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }
}