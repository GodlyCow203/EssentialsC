package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RtpWarmupCancelEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final World world;
    private final CancelReason reason;

    public enum CancelReason {
        PLAYER_OFFLINE,
        PLAYER_MOVED,
        EVENT_CANCELLED
    }

    public RtpWarmupCancelEvent(Player player, World world, CancelReason reason) {
        this.player = player;
        this.world = world;
        this.reason = reason;
    }

    public Player getPlayer() {
        return player;
    }

    public World getWorld() {
        return world;
    }

    public CancelReason getReason() {
        return reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}