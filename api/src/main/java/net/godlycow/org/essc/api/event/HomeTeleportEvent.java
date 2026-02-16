package net.godlycow.org.essc.api.event;

import net.godlycow.org.essc.api.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HomeTeleportEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final Home home;
    private final TeleportCause cause;
    private String cancelReason;

    public enum TeleportCause {
        COMMAND,
        ADMIN_COMMAND,
        API,
        WARMUP_COMPLETE
    }

    public HomeTeleportEvent(@NotNull Player player, @NotNull Home home, @NotNull TeleportCause cause) {
        super(true);
        this.player = player;
        this.home = home;
        this.cause = cause;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Home getHome() {
        return home;
    }

    @NotNull
    public TeleportCause getCause() {
        return cause;
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