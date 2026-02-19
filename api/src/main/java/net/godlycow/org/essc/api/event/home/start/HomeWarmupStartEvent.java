package net.godlycow.org.essc.api.event.home.start;

import net.godlycow.org.essc.api.event.home.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class HomeWarmupStartEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final Home home;
    private long warmupSeconds;

    public HomeWarmupStartEvent(@NotNull Player player, @NotNull Home home, long warmupSeconds) {
        super(false); // Runs on the main server thread
        this.player = player;
        this.home = home;
        this.warmupSeconds = warmupSeconds;
    }

    // Get the player who is about to teleport
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // Get the home destination the player is teleporting to
    @NotNull
    public Home getHome() {
        return home;
    }

    // Get the current wait time in seconds before the teleport happens
    public long getWarmupSeconds() {
        return warmupSeconds;
    }

    // Change the amount of time the player has to wait
    public void setWarmupSeconds(long seconds) {
        this.warmupSeconds = seconds;
    }

    // Check if the warmup was skipped or stopped
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Cancel the warmup (this usually makes the teleport happen instantly)
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