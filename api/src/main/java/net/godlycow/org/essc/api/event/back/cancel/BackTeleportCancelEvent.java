package net.godlycow.org.essc.api.event.back.cancel;

import net.godlycow.org.essc.api.event.back.Back;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class BackTeleportCancelEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Back back;
    private final CancelReason reason;
    private final String details;

    public enum CancelReason {
        PLAYER_MOVE,      // Player moved during warmup
        PLAYER_DAMAGE,    // Player took damage
        PLAYER_QUIT,      // Player disconnected
        COMMAND,          // Player used another command
        ADMIN_CANCEL,     // Admin cancelled it
        PLUGIN_CANCEL,    // Another plugin cancelled
        OTHER             // Something else
    }

    public BackTeleportCancelEvent(@NotNull Player player, @Nullable Back back,
                                   @NotNull CancelReason reason, @Nullable String details) {
        super(false);
        this.player = player;
        this.back = back;
        this.reason = reason;
        this.details = details;
    }

    // Get the player whose teleport was cancelled
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // Get the back location they were going to (can be null if cancelled early)
    @Nullable
    public Back getBack() {
        return back;
    }

    // Get why the teleport was cancelled
    @NotNull
    public CancelReason getReason() {
        return reason;
    }

    // Get extra details about the cancellation
    @Nullable
    public String getDetails() {
        return details;
    }

    // Required Bukkit method for event handling
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    // Required Bukkit method for event handling
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}