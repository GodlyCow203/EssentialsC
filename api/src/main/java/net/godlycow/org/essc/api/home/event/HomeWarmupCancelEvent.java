package net.godlycow.org.essc.api.home.event;

import net.godlycow.org.essc.api.home.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a home teleport warmup is abandoned.
 *
 * <p>Observe-only: the warmup is already gone, so this event cannot be cancelled.
 * See {@link CancelReason} for why it was abandoned.
 */
public class HomeWarmupCancelEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Home home;
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
     * Creates a new home warmup cancel event.
     *
     * @param player the player whose warmup was abandoned
     * @param home the destination home
     * @param reason why the warmup was abandoned
     */
    public HomeWarmupCancelEvent(Player player, Home home, CancelReason reason) {
        this.player = player;
        this.home = home;
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
     * Returns the destination home.
     *
     * @return the home
     */
    public Home getHome() {
        return home;
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
