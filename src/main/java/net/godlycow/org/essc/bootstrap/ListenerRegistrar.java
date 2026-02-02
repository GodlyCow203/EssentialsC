package net.godlycow.org.essc.bootstrap;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.config.ConfigManager;
import net.godlycow.org.essc.listener.*;
import net.godlycow.org.essc.shop.ShopListener;
import net.godlycow.org.essc.shop.ShopManager;


public class ListenerRegistrar {


    public ListenerRegistrar(EssentialsC plugin) {
        plugin.getServer().getPluginManager().registerEvents(new JoinLeaveListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InvseeListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EnderSeeListener(plugin), plugin);
    }
}
