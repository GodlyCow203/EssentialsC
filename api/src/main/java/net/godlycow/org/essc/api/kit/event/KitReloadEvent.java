package net.godlycow.org.essc.api.kit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when kit definitions are reloaded.
 *
 * <p>Observe-only: this event cannot be cancelled.
 */
public class KitReloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final int kitCount;
    private final long reloadTimestamp;

    /**
     * Creates a new kit reload event.
     *
     * @param kitCount how many kits were loaded by the reload
     * @param reloadTimestamp the reload timestamp in millis
     */
    public KitReloadEvent(int kitCount, long reloadTimestamp) {
        this.kitCount = kitCount;
        this.reloadTimestamp = reloadTimestamp;
    }

    /**
     * Returns how many kits were loaded by the reload.
     *
     * @return kit count
     */
    public int getKitCount() {
        return kitCount;
    }

    /**
     * Returns the reload timestamp in millis.
     *
     * @return reload timestamp
     */
    public long getReloadTimestamp() {
        return reloadTimestamp;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}