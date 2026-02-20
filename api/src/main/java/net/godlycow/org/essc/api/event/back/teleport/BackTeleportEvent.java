package net.godlycow.org.essc.api.event.back.teleport;

import net.godlycow.org.essc.api.event.back.Back;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class BackTeleportEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final Back back;
    private final TeleportCause cause;
    private String cancelReason;

    // The reason why the teleport was started
    public enum TeleportCause {
        COMMAND,        // Player typed the /back command
        DEATH,          // Player died and is returning to death point
        ADMIN_COMMAND,  // An admin is teleporting a player
        API             // Another plugin triggered this teleport
    }

    public BackTeleportEvent(@NotNull Player player, @NotNull Back back, @NotNull TeleportCause cause) {
        super(false); // Runs on the main server thread
        this.player = player;
        this.back = back;
        this.cause = cause;
    }

    // Get the player who is trying to teleport back
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // Get the back location the player is heading toward
    @NotNull
    public Back getBack() {
        return back;
    }

    // Get what triggered this teleport request
    @NotNull
    public TeleportCause getCause() {
        return cause;
    }

    // Get the message explaining why the teleport was stopped
    @Nullable
    public String getCancelReason() {
        return cancelReason;
    }

    // Set a reason to show the player if you block the teleport
    public void setCancelReason(@Nullable String reason) {
        this.cancelReason = reason;
    }

    // Check if the teleport process was stopped
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Stop the player from teleporting
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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