package net.godlycow.org.essc.api.kit.event;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired after a kit claim has completed successfully.
 *
 * <p>Observe-only: the claim already happened, so this event cannot be cancelled.
 * Use it for logging, rewards or follow-up actions.
 */
public class KitPostClaimEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Kit kit;
    private final long claimTimestamp;

    /**
     * Creates a new post-claim event.
     *
     * @param player the player that claimed
     * @param kit the claimed kit
     * @param claimTimestamp the claim timestamp in millis
     */
    public KitPostClaimEvent(Player player, Kit kit, long claimTimestamp) {
        this.player = player;
        this.kit = kit;
        this.claimTimestamp = claimTimestamp;
    }

    /**
     * Returns the player that claimed.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the claimed kit.
     *
     * @return the kit
     */
    public Kit getKit() {
        return kit;
    }

    /**
     * Returns the claim timestamp in millis.
     *
     * @return claim timestamp
     */
    public long getClaimTimestamp() {
        return claimTimestamp;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}