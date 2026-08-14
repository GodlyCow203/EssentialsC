package net.godlycow.org.essc.api.kit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class KitDataLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID playerId;
    private final String playerName;
    private final int loadedEntryCount;

    public KitDataLoadEvent(UUID playerId, String playerName, int loadedEntryCount) {
        super(true);
        this.playerId = playerId;
        this.playerName = playerName;
        this.loadedEntryCount = loadedEntryCount;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getLoadedEntryCount() {
        return loadedEntryCount;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
