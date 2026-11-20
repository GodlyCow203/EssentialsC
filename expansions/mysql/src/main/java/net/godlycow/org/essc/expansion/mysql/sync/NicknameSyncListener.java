package net.godlycow.org.essc.expansion.mysql.sync;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NicknameSyncListener implements Listener {

    private final NicknameSyncService service;

    public NicknameSyncListener(NicknameSyncService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        service.onJoin(event.getPlayer());
    }
}
