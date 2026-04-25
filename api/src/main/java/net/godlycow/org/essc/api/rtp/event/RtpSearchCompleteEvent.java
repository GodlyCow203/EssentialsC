package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RtpSearchCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final World world;
    private final Location location;
    private final int attempts;

    public RtpSearchCompleteEvent(Player player, World world, Location location, int attempts) {
        this.player = player;
        this.world = world;
        this.location = location != null ? location.clone() : null;
        this.attempts = attempts;
    }

    public Player getPlayer() {
        return player;
    }

    public World getWorld() {
        return world;
    }

    public Location getLocation() {
        return location != null ? location.clone() : null;
    }

    public int getAttempts() {
        return attempts;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}