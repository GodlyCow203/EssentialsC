package net.godlycow.org.essc.api.warp.event;

import net.godlycow.org.essc.api.warp.Warp;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a warp teleport warmup is abandoned.
 *
 * <p>Observe-only: the warmup is already gone, so this event cannot be cancelled.
 * See {@link CancelReason} for why it was abandoned.
 */
public class WarpWarmupCancelEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Warp warp;
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
     * Creates a new warp warmup cancel event.
     *
     * @param player the player whose warmup was abandoned
     * @param warp the destination warp
     * @param reason why the warmup was abandoned
     */
    public WarpWarmupCancelEvent(Player player, Warp warp, CancelReason reason) {
        this.player = player;
        this.warp = warp;
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
     * Returns the destination warp.
     *
     * @return the warp
     */
    public Warp getWarp() {
        return warp;
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