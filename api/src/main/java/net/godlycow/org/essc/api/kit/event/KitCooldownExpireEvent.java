package net.godlycow.org.essc.api.kit.event;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player's kit cooldown expires.
 *
 * <p>Observe-only: this event cannot be cancelled.
 */
public class KitCooldownExpireEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Kit kit;
    private final long previousClaimTime;

    /**
     * Creates a new kit cooldown expire event.
     *
     * @param player the player whose cooldown expired
     * @param kit the kit
     * @param previousClaimTime the timestamp the expired cooldown was measured from
     */
    public KitCooldownExpireEvent(Player player, Kit kit, long previousClaimTime) {
        this.player = player;
        this.kit = kit;
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
     * Returns the kit.
     *
     * @return the kit
     */
    public Kit getKit() {
        return kit;
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