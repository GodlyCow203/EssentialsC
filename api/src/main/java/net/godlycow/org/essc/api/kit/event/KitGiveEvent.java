package net.godlycow.org.essc.api.kit.event;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KitGiveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Kit kit;
    private List<ItemStack> items;
    private boolean cancelled;

    public KitGiveEvent(Player player, Kit kit, List<ItemStack> items) {
        this.player = player;
        this.kit = kit;
        this.items = new ArrayList<>(items);
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Kit getKit() {
        return kit;
    }

    public List<ItemStack> getItems() {
        return new ArrayList<>(items);
    }

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