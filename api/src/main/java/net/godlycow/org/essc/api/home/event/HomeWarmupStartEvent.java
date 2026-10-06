package net.godlycow.org.essc.api.home.event;

import net.godlycow.org.essc.api.home.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a home teleport warmup begins.
 *
 * <p>Adjust the delay with {@link #setWarmupSeconds(long)}, or cancel the event to
 * skip the warmup entirely.
 */
public class HomeWarmupStartEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Home home;
    private long warmupSeconds;
    private boolean cancelled;
    private String cancelReason;

    /**
     * Creates a new home warmup start event.
     *
     * @param player the teleporting player
     * @param home the destination home
     * @param warmupSeconds the warmup delay in seconds
     */
    public HomeWarmupStartEvent(Player player, Home home, long warmupSeconds) {
        this.player = player;
        this.home = home;
        this.warmupSeconds = warmupSeconds;
        this.cancelled = false;
        this.cancelReason = "";
    }

    /**
     * Returns the teleporting player.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the destination home.
     *
     * @return the home
     */
    public Home getHome() {
        return home;
    }

    /**
     * Returns the warmup delay in seconds.
     *
     * @return warmup seconds
     */
    public long getWarmupSeconds() {
        return warmupSeconds;
    }

    /**
     * Overrides the warmup delay.
     *
     * @param warmupSeconds the new warmup in seconds
     */
    public void setWarmupSeconds(long warmupSeconds) {
        this.warmupSeconds = warmupSeconds;
    }

    /**
     * Returns why the event was cancelled, or an empty string if not cancelled.
     *
     * @return the cancel reason
     */
    public String getCancelReason() {
        return cancelReason;
    }

    /**
     * Sets why the event is being cancelled.
     *
     * @param cancelReason the cancel reason
     */
    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
