package net.godlycow.org.essc.api.kit.event;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KitLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Kit kit;
    private final String sourceFileName;

    public KitLoadEvent(Kit kit, String sourceFileName) {
        this.kit = kit;
        this.sourceFileName = sourceFileName;
    }

    public Kit getKit() {
        return kit;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}