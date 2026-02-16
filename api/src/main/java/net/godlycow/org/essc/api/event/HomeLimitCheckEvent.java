package net.godlycow.org.essc.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HomeLimitCheckEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private int maxHomes;
    private String cancelReason;

    public HomeLimitCheckEvent(@NotNull Player player, int defaultMaxHomes) {
        super(true);
        this.player = player;
        this.maxHomes = defaultMaxHomes;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    public int getMaxHomes() {
        return maxHomes;
    }

    public void setMaxHomes(int max) {
        this.maxHomes = max;
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
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}