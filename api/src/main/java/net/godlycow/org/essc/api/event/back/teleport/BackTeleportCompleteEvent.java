package net.godlycow.org.essc.api.event.back.teleport;

import net.godlycow.org.essc.api.event.back.Back;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class BackTeleportCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Back back;
    private final long teleportDuration;

    public BackTeleportCompleteEvent(@NotNull Player player, @NotNull Back back, long teleportDuration) {
        super(true); // This runs asynchronously (off the main thread)
        this.player = player;
        this.back = back;
        this.teleportDuration = teleportDuration;
    }

    // Get the player who has arrived at their back location
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // Get the back destination where the player teleported
    @NotNull
    public Back getBack() {
        return back;
    }

    // Get the total time (in milliseconds) the process took from start to finish
    public long getTeleportDuration() {
        return teleportDuration;
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