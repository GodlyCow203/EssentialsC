package net.godlycow.org.essc.api.event;

import net.godlycow.org.essc.api.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HomeTeleportCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Home home;
    private final long teleportDuration;

    public HomeTeleportCompleteEvent(@NotNull Player player, @NotNull Home home, long teleportDuration) {
        super(true);
        this.player = player;
        this.home = home;
        this.teleportDuration = teleportDuration;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Home getHome() {
        return home;
    }

    public long getTeleportDuration() { // duration
        return teleportDuration;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}