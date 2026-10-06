package net.godlycow.org.essc.api.kit.event;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a first-join kit is about to be granted to a new player.
 *
 * <p>Cancelling this event skips the automatic grant for that player.
 */
public class KitFirstJoinEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Kit kit;
    private boolean cancelled;

    /**
     * Creates a new first-join kit event.
     *
     * @param player the joining player
     * @param kit the kit about to be granted
     */
    public KitFirstJoinEvent(Player player, Kit kit) {
        this.player = player;
        this.kit = kit;
        this.cancelled = false;
    }

    /**
     * Returns the joining player.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the kit about to be granted.
     *
     * @return the kit
     */
    public Kit getKit() {
        return kit;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}