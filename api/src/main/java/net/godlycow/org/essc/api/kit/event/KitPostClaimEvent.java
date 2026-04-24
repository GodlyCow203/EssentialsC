package net.godlycow.org.essc.api.kit.event;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KitPostClaimEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Kit kit;
    private final long claimTimestamp;

    public KitPostClaimEvent(Player player, Kit kit, long claimTimestamp) {
        this.player = player;
        this.kit = kit;
        this.claimTimestamp = claimTimestamp;
    }

    public Player getPlayer() {
        return player;
    }

    public Kit getKit() {
        return kit;
    }

    public long getClaimTimestamp() {
        return claimTimestamp;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}