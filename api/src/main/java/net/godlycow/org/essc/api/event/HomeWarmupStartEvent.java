package net.godlycow.org.essc.api.event;

import net.godlycow.org.essc.api.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HomeWarmupStartEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final Home home;
    private long warmupSeconds;

    public HomeWarmupStartEvent(@NotNull Player player, @NotNull Home home, long warmupSeconds) {
        super(true);
        this.player = player;
        this.home = home;
        this.warmupSeconds = warmupSeconds;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Home getHome() {
        return home;
    }

    public long getWarmupSeconds() {
        return warmupSeconds;
    }

    public void setWarmupSeconds(long seconds) {
        this.warmupSeconds = seconds;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}