package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when an RTP warmup begins.
 *
 * <p>Adjust the delay with {@link #setWarmupSeconds(long)}, or cancel the event to
 * skip the warmup entirely.
 */
public class RtpWarmupStartEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final World world;
    private long warmupSeconds;
    private boolean cancelled;
    private String cancelReason;

    /**
     * Creates a new RTP warmup start event.
     *
     * @param player the teleporting player
     * @param world the world
     * @param warmupSeconds the warmup delay in seconds
     */
    public RtpWarmupStartEvent(Player player, World world, long warmupSeconds) {
        this.player = player;
        this.world = world;
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
     * Returns the world.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
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