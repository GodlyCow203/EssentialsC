package net.godlycow.org.essc.plugin.listener;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class WarpListener implements Listener {

    private final EssentialsC plugin;

    public WarpListener(EssentialsC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().isWarpEnabled()) return;
        if (!plugin.getConfigManager().isWarpCancelOnMovement()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!plugin.getWarpManager().hasPendingWarp(uuid)) return;

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        plugin.getWarpManager().cancelWarmupTask(uuid);
        plugin.getWarpManager().removePendingWarp(uuid);
        plugin.getWarpManager().clearMovementTrack(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getWarpManager() != null) {
            UUID uuid = event.getPlayer().getUniqueId();
            plugin.getWarpManager().cancelWarmupTask(uuid);
            plugin.getWarpManager().removePendingWarp(uuid);
            plugin.getWarpManager().clearMovementTrack(uuid);
        }
    }
}