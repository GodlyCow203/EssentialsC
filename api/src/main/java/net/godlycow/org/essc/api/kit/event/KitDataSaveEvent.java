package net.godlycow.org.essc.api.kit.event;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class KitDataSaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID playerId;
    private final Kit kit;
    private final long claimTimestamp;
    private final int newClaimCount;

    public KitDataSaveEvent(UUID playerId, Kit kit, long claimTimestamp, int newClaimCount) {
        this.playerId = playerId;
        this.kit = kit;
        this.claimTimestamp = claimTimestamp;
        this.newClaimCount = newClaimCount;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Kit getKit() {
        return kit;
    }

    public long getClaimTimestamp() {
        return claimTimestamp;
    }

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