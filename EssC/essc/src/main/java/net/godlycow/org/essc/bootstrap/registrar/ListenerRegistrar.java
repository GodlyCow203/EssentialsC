package net.godlycow.org.essc.bootstrap.registrar;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.listener.*;
import net.godlycow.org.essc.plugin.listener.EnderSeeListener;
import net.godlycow.org.essc.plugin.listener.InvseeListener;
import net.godlycow.org.essc.plugin.listener.JoinLeaveListener;
import net.godlycow.org.essc.plugin.listener.MuteListener;
import net.godlycow.org.essc.util.VersionCheckUtil;


public class ListenerRegistrar {
    public ListenerRegistrar(EssentialsC plugin) {
        plugin.getServer().getPluginManager().registerEvents(new JoinLeaveListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InvseeListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EnderSeeListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MuteListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new VersionCheckUtil(plugin), plugin);
    }

}
