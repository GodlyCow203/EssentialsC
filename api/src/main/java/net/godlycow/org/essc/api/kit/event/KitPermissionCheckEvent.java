package net.godlycow.org.essc.api.kit.event;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when EssentialsC checks whether a player may claim a kit.
 *
 * <p>Use {@link #setHasPermission(boolean)} to override the outcome with your own
 * permission logic, or cancel the event to force-deny the claim.
 */
public class KitPermissionCheckEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Kit kit;
    private boolean hasPermission;
    private boolean cancelled;

    /**
     * Creates a new permission check event.
     *
     * @param player the player being checked
     * @param kit the kit being checked
     * @param hasPermission the current outcome
     */
    public KitPermissionCheckEvent(Player player, Kit kit, boolean hasPermission) {
        this.player = player;
        this.kit = kit;
        this.hasPermission = hasPermission;
        this.cancelled = false;
    }

    /**
     * Returns the player being checked.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the kit being checked.
     *
     * @return the kit
     */
    public Kit getKit() {
        return kit;
    }

    /**
     * Returns whether the player currently passes the permission check.
     *
     * @return {@code true} if permitted
     */
    public boolean hasPermission() {
        return hasPermission;
    }

    /**
     * Overrides the permission check outcome.
     *
     * @param hasPermission the new outcome
     */
    public void setHasPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
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