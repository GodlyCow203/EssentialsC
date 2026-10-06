package net.godlycow.org.essc.api.home.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player's home teleport cooldown expires.
 *
 * <p>Observe-only: this event cannot be cancelled.
 */
public class HomeCooldownExpireEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final long previousTeleportTime;

    /**
     * Creates a new home cooldown expire event.
     *
     * @param player the player whose cooldown expired
     * @param previousTeleportTime the timestamp the expired cooldown was measured from
     */
    public HomeCooldownExpireEvent(Player player, long previousTeleportTime) {
        this.player = player;
        this.previousTeleportTime = previousTeleportTime;
    }

    /**
     * Returns the player whose cooldown expired.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the timestamp the expired cooldown was measured from.
     *
     * @return previous teleport time
     */
    public long getPreviousTeleportTime() {
        return previousTeleportTime;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
