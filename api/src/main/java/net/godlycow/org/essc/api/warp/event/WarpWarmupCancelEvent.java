package net.godlycow.org.essc.api.warp.event;

import net.godlycow.org.essc.api.warp.Warp;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WarpWarmupCancelEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Warp warp;
    private final CancelReason reason;

    public enum CancelReason {
        PLAYER_OFFLINE,
        PLAYER_MOVED,
        EVENT_CANCELLED
    }

    public WarpWarmupCancelEvent(Player player, Warp warp, CancelReason reason) {
        this.player = player;
        this.warp = warp;
        this.reason = reason;
    }

    public Player getPlayer() {
        return player;
    }

    public Warp getWarp() {
        return warp;
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