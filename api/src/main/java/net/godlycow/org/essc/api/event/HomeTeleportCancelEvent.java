package net.godlycow.org.essc.api.event;

import net.godlycow.org.essc.api.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HomeTeleportCancelEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Home home;
    private final CancelReason reason;
    private final String details;

    public enum CancelReason {
        PLAYER_MOVE,
        PLAYER_DAMAGE,
        PLAYER_QUIT,
        COMMAND,
        ADMIN_CANCEL,
        PLUGIN_CANCEL,
        OTHER
    }

    public HomeTeleportCancelEvent(@NotNull Player player, @Nullable Home home,
                                   @NotNull CancelReason reason, @Nullable String details) {
        super(true);
        this.player = player;
        this.home = home;
        this.reason = reason;
        this.details = details;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @Nullable
    public Home getHome() {
        return home;
    }

    @NotNull
    public CancelReason getReason() {
        return reason;
    }

    @Nullable
    public String getDetails() {
        return details;
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