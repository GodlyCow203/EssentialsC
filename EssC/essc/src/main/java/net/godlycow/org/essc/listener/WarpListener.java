package net.godlycow.org.essc.listener;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WarpListener implements Listener {

    private final EssentialsC plugin;

    public WarpListener(EssentialsC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().isWarpEnabled()) return;
        if (!plugin.getConfigManager().isWarpCancelOnMovement()) return;

        if (!plugin.getWarpManager().hasPendingWarp(event.getPlayer().getUniqueId())) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getWarpManager() != null) {
            plugin.getWarpManager().removePendingWarp(event.getPlayer().getUniqueId());
            plugin.getWarpManager().clearMovementTrack(event.getPlayer().getUniqueId());
        }
    }
}