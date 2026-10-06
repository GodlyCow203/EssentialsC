package net.godlycow.org.essc.api.kit.event;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player attempts to claim a kit, before any items are granted.
 *
 * <p>Cancelling this event blocks the claim. Fired for both command claims and
 * {@link net.godlycow.org.essc.api.kit.KitManager#claimKitForPlayer(Player, Kit)}
 * API claims.
 */
public class KitClaimEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Kit kit;
    private boolean cancelled;

    /**
     * Creates a new kit claim event.
     *
     * @param player the claiming player
     * @param kit the kit being claimed
     */
    public KitClaimEvent(Player player, Kit kit) {
        this.player = player;
        this.kit = kit;
        this.cancelled = false;
    }

    /**
     * Returns the claiming player.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the kit being claimed.
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