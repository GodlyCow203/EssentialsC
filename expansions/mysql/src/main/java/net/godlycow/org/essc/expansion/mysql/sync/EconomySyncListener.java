package net.godlycow.org.essc.expansion.mysql.sync;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class EconomySyncListener implements Listener {

    private final EconomySyncService service;

    public EconomySyncListener(EconomySyncService service) {

        this.service = service;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        service.onJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        service.onQuit(event.getPlayer());
    }
}
