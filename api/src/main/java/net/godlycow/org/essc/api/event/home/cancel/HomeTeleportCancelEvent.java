package net.godlycow.org.essc.api.event.home.cancel;

import net.godlycow.org.essc.api.event.home.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a pending teleport gets cancelled (during warmup).
 *
 */
public class HomeTeleportCancelEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Home home;
    private final CancelReason reason;
    private final String details;

    public enum CancelReason {
        PLAYER_MOVE,      // Player moved during warmup
        PLAYER_DAMAGE,    // Player took damage
        PLAYER_QUIT,      // Player disconnected
        COMMAND,          // Player used another command
        ADMIN_CANCEL,     // op cancelled it
        PLUGIN_CANCEL,    // Another plugin cancelled
        OTHER             // Something else
    }

    public HomeTeleportCancelEvent(@NotNull Player player, @Nullable Home home,
                                   @NotNull CancelReason reason, @Nullable String details) {
        super(false);
        this.player = player;
        this.home = home;
        this.reason = reason;
        this.details = details;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * The home youre going to. Can be null if cancelled super early.
     */
    @Nullable
    public Home getHome() {
        return home;
    }

    @NotNull
    public CancelReason getReason() {
        return reason;
    }

    @Nullable
    public String getDetails() {
        return details;
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