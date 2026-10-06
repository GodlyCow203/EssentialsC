package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player's RTP cooldown expires.
 *
 * <p>Observe-only: this event cannot be cancelled.
 */
public class RtpCooldownExpireEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final long previousClaimTime;

    /**
     * Creates a new RTP cooldown expire event.
     *
     * @param player the player whose cooldown expired
     * @param previousClaimTime the timestamp the expired cooldown was measured from
     */
    public RtpCooldownExpireEvent(Player player, long previousClaimTime) {
        this.player = player;
        this.previousClaimTime = previousClaimTime;
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
     * @return previous claim time
     */
    public long getPreviousClaimTime() {
        return previousClaimTime;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}