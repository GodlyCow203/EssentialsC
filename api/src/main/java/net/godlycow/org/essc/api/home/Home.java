package net.godlycow.org.essc.api.home;

import org.bukkit.Location;
import org.bukkit.Server;

import java.util.UUID;

public interface Home {
    UUID getOwner();
    String getName();
    String getWorldName();
    double getX();
    double getY();
    double getZ();
    float getYaw();
    float getPitch();
    long getCreatedAt();
    Location toLocation(Server server);
}
