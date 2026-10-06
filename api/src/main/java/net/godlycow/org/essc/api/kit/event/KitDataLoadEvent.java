package net.godlycow.org.essc.api.kit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Fired when a player's kit claim data is loaded.
 *
 * <p>Observe-only: this event cannot be cancelled. Note this event is fired
 * asynchronously.
 */
public class KitDataLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID playerId;
    private final String playerName;
    private final int loadedEntryCount;

    /**
     * Creates a new kit data load event.
     *
     * @param playerId the player whose data was loaded
     * @param playerName the player name at load time
     * @param loadedEntryCount how many claim entries were loaded
     */
    public KitDataLoadEvent(UUID playerId, String playerName, int loadedEntryCount) {
        super(true);
        this.playerId = playerId;
        this.playerName = playerName;
        this.loadedEntryCount = loadedEntryCount;
    }

    /**
     * Returns the player whose data was loaded.
     *
     * @return the player UUID
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Returns the player name at load time.
     *
     * @return the player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Returns how many claim entries were loaded.
     *
     * @return loaded entry count
     */
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
