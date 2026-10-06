package net.godlycow.org.essc.api.kit.event;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Fired when a player's kit claim data is persisted.
 *
 * <p>Observe-only: this event cannot be cancelled.
 */
public class KitDataSaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID playerId;
    private final Kit kit;
    private final long claimTimestamp;
    private final int newClaimCount;

    /**
     * Creates a new kit data save event.
     *
     * @param playerId the player whose data was saved
     * @param kit the claimed kit
     * @param claimTimestamp the claim timestamp in millis
     * @param newClaimCount the updated lifetime claim count
     */
    public KitDataSaveEvent(UUID playerId, Kit kit, long claimTimestamp, int newClaimCount) {
        this.playerId = playerId;
        this.kit = kit;
        this.claimTimestamp = claimTimestamp;
        this.newClaimCount = newClaimCount;
    }

    /**
     * Returns the player whose data was saved.
     *
     * @return the player UUID
     */
    public UUID getPlayerId() {
        return playerId;
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

    /**
     * Returns the updated lifetime claim count.
     *
     * @return new claim count
     */
    public int getNewClaimCount() {
        return newClaimCount;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}