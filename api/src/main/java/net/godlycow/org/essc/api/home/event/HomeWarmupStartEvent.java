package net.godlycow.org.essc.api.home.event;

import net.godlycow.org.essc.api.home.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HomeWarmupStartEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Home home;
    private long warmupSeconds;
    private boolean cancelled;
    private String cancelReason;

    public HomeWarmupStartEvent(Player player, Home home, long warmupSeconds) {
        this.player = player;
        this.home = home;
        this.warmupSeconds = warmupSeconds;
        this.cancelled = false;
        this.cancelReason = "";
    }

    public Player getPlayer() {
        return player;
    }

    public Home getHome() {
        return home;
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
