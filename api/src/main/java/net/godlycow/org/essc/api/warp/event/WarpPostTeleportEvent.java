package net.godlycow.org.essc.api.warp.event;

import net.godlycow.org.essc.api.warp.Warp;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WarpPostTeleportEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Warp warp;
    private final Location destination;

    public WarpPostTeleportEvent(Player player, Warp warp, Location destination) {
        this.player = player;
        this.warp = warp;
        this.destination = destination.clone();
    }

    public Player getPlayer() {
        return player;
    }

    public Warp getWarp() {
        return warp;
    }

    public Location getDestination() {
        return destination.clone();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}