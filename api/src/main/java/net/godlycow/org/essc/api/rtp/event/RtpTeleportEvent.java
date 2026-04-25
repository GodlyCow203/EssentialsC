package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RtpTeleportEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final World world;
    private Location destination;
    private boolean cancelled;
    private String cancelReason;

    public RtpTeleportEvent(Player player, World world, Location destination) {
        this.player = player;
        this.world = world;
        this.destination = destination.clone();
        this.cancelled = false;
        this.cancelReason = "";
    }

    public Player getPlayer() {
        return player;
    }

    public World getWorld() {
        return world;
    }

    public Location getDestination() {
        return destination.clone();
    }

    public void setDestination(Location destination) {
        this.destination = destination.clone();
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}