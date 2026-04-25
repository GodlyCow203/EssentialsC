package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RtpWarmupStartEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final World world;
    private long warmupSeconds;
    private boolean cancelled;
    private String cancelReason;

    public RtpWarmupStartEvent(Player player, World world, long warmupSeconds) {
        this.player = player;
        this.world = world;
        this.warmupSeconds = warmupSeconds;
        this.cancelled = false;
        this.cancelReason = "";
    }

    public Player getPlayer() {
        return player;
    }

    public World getWorld() {
        return world;
    }

    public long getWarmupSeconds() {
        return warmupSeconds;
    }

    public void setWarmupSeconds(long warmupSeconds) {
        this.warmupSeconds = warmupSeconds;
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