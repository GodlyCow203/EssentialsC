package net.godlycow.org.essc.api.event;

import net.godlycow.org.essc.api.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a player tries to create or update a home.
 * Cancel this to prevent the home from being saved.
 */
public class HomeCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final Home home;
    private String cancelReason;

    public HomeCreateEvent(@NotNull Player player, @NotNull Home home) {
        super(false);
        this.player = player;
        this.home = home;
    }

    /** The player creating the home */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /** The home being created (contains name, location, etc) */
    @NotNull
    public Home getHome() {
        return home;
    }

    /** Why was this cancelled? Shown to player if set. */
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