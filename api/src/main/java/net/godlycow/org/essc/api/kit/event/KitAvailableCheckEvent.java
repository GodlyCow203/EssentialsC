package net.godlycow.org.essc.api.kit.event;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when EssentialsC checks whether a kit is available to a player.
 *
 * <p>Use {@link #setAvailable(boolean)} and {@link #setDenialReason(String)} to
 * override the outcome, or cancel the event to force-deny the claim.
 */
public class KitAvailableCheckEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Kit kit;
    private boolean available;
    private String denialReason;
    private boolean cancelled;

    /**
     * Creates a new availability check event.
     *
     * @param player the player being checked
     * @param kit the kit being checked
     * @param available the current outcome
     * @param denialReason the current denial reason, or {@code null} if available
     */
    public KitAvailableCheckEvent(Player player, Kit kit, boolean available, String denialReason) {
        this.player = player;
        this.kit = kit;
        this.available = available;
        this.denialReason = denialReason;
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
     * Returns whether the kit is currently available to the player.
     *
     * @return {@code true} if available
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Overrides whether the kit is available.
     *
     * @param available the new outcome
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Returns the denial reason shown when the kit is unavailable.
     *
     * @return the denial reason, or {@code null}
     */
    public String getDenialReason() {
        return denialReason;
    }

    /**
     * Sets the denial reason shown when the kit is unavailable.
     *
     * @param denialReason the new denial reason
     */
    public void setDenialReason(String denialReason) {
        this.denialReason = denialReason;
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