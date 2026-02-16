package net.godlycow.org.essc.api.event;

import net.godlycow.org.essc.api.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class HomeDeleteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final Home home;
    private final boolean adminDelete;
    private String cancelReason;

    public HomeDeleteEvent(@Nullable Player player, @NotNull Home home, boolean adminDelete) {
        super(true);
        this.player = player;
        this.home = home;
        this.adminDelete = adminDelete;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Home getHome() {
        return home;
    }

    public boolean isAdminDelete() {
        return adminDelete;
    }

    @Nullable
    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(@Nullable String reason) {
        this.cancelReason = reason;
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