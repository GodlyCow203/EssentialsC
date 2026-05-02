package net.godlycow.org.essc.bootstrap;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.listener.*;
import net.godlycow.org.essc.version.VersionChecker;


public class ListenerRegistrar {
    public ListenerRegistrar(EssentialsC plugin) {
        plugin.getServer().getPluginManager().registerEvents(new JoinLeaveListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InvseeListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EnderSeeListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MuteListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new VersionChecker(plugin), plugin);
    }

}
