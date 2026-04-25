package net.godlycow.org.essc.api.rtp;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface RtpRequest {
    UUID getRequestId();
    Player getPlayer();
    World getTargetWorld();
    long getRequestTimestamp();
    boolean wasWarmupRequired();
    long getWarmupSeconds();
}