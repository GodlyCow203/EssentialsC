package net.godlycow.org.essc.api.kit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KitReloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final int kitCount;
    private final long reloadTimestamp;

    public KitReloadEvent(int kitCount, long reloadTimestamp) {
        this.kitCount = kitCount;
        this.reloadTimestamp = reloadTimestamp;
    }

    public int getKitCount() {
        return kitCount;
    }

    public long getReloadTimestamp() {
        return reloadTimestamp;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}