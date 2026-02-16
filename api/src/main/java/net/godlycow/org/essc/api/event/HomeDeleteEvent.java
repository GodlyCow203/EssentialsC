package net.godlycow.org.essc.api.event;

import net.godlycow.org.essc.api.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a home is being deleted.
 * Cancel this to prevent deletion.
 *
 */
public class HomeDeleteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final Home home;
    private final boolean adminDelete;
    private String cancelReason;

    public HomeDeleteEvent(@Nullable Player player, @NotNull Home home, boolean adminDelete) {
        super(false);
        this.player = player;
        this.home = home;
        this.adminDelete = adminDelete;
    }

    /** Who deleted it? Null if console or system */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /** The home being deleted */
    @NotNull
    public Home getHome() {
        return home;
    }

    /** Was this an admin deletion? */
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