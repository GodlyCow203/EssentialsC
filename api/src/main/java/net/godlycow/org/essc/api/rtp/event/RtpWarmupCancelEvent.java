package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when an RTP warmup is abandoned.
 *
 * <p>Observe-only: the warmup is already gone, so this event cannot be cancelled.
 * See {@link CancelReason} for why it was abandoned.
 */
public class RtpWarmupCancelEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final World world;
    private final CancelReason reason;

    /**
     * Why a warmup was abandoned.
     */
    public enum CancelReason {
        /** The player went offline mid warmup. */
        PLAYER_OFFLINE,
        /** The player moved while cancel-on-move is enabled. */
        PLAYER_MOVED,
        /** A listener cancelled the warmup start event. */
        EVENT_CANCELLED
    }

    /**
     * Creates a new RTP warmup cancel event.
     *
     * @param player the player whose warmup was abandoned
     * @param world the world
     * @param reason why the warmup was abandoned
     */
    public RtpWarmupCancelEvent(Player player, World world, CancelReason reason) {
        this.player = player;
        this.world = world;
        this.reason = reason;
    }

    /**
     * Returns the player whose warmup was abandoned.
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
     * Returns why the warmup was abandoned.
     *
     * @return the cancel reason
     */
    public CancelReason getReason() {
        return reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}