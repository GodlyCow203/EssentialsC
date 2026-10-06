package net.godlycow.org.essc.api.kit.event;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Fired when kit items are about to be given to a player.
 *
 * <p>Cancelling this event blocks the item grant. Use {@link #setItems(List)} to
 * modify which items are handed out.
 */
public class KitGiveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Kit kit;
    private List<ItemStack> items;
    private boolean cancelled;

    /**
     * Creates a new kit give event.
     *
     * @param player the receiving player
     * @param kit the kit being granted
     * @param items the items about to be given
     */
    public KitGiveEvent(Player player, Kit kit, List<ItemStack> items) {
        this.player = player;
        this.kit = kit;
        this.items = new ArrayList<>(items);
        this.cancelled = false;
    }

    /**
     * Returns the receiving player.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the kit being granted.
     *
     * @return the kit
     */
    public Kit getKit() {
        return kit;
    }

    /**
     * Returns a copy of the items about to be given.
     *
     * @return the items
     */
    public List<ItemStack> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Replaces the items that will be given.
     *
     * @param items the new items
     */
    public void setItems(List<ItemStack> items) {
        this.items = new ArrayList<>(items);
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