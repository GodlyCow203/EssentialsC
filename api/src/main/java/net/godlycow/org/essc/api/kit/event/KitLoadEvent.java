package net.godlycow.org.essc.api.kit.event;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a kit definition is loaded from disk.
 *
 * <p>Observe-only: this event cannot be cancelled.
 */
public class KitLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Kit kit;
    private final String sourceFileName;

    /**
     * Creates a new kit load event.
     *
     * @param kit the loaded kit
     * @param sourceFileName the file the kit was loaded from
     */
    public KitLoadEvent(Kit kit, String sourceFileName) {
        this.kit = kit;
        this.sourceFileName = sourceFileName;
    }

    /**
     * Returns the loaded kit.
     *
     * @return the kit
     */
    public Kit getKit() {
        return kit;
    }

    /**
     * Returns the file the kit was loaded from.
     *
     * @return the source file name
     */
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