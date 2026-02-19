package net.godlycow.org.essc.api.event.home.teleport;

import net.godlycow.org.essc.api.event.home.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class HomeTeleportCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Home home;
    private final long teleportDuration;

    public HomeTeleportCompleteEvent(@NotNull Player player, @NotNull Home home, long teleportDuration) {
        super(true); // This runs asynchronously (off the main thread)
        this.player = player;
        this.home = home;
        this.teleportDuration = teleportDuration;
    }

    // Get the player who has arrived at their home
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // Get the home destination where the player teleported
    @NotNull
    public Home getHome() {
        return home;
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