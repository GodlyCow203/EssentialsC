package net.godlycow.org.essc.api.warp;

import org.bukkit.Location;

public interface Warp {

    String getName();
    Location getLocation();

    void setLocation(Location location);

    String getPermission();

    void setPermission(String permission);

    double getCost();

    void setCost(double cost);

    boolean isHidden();

    void setHidden(boolean hidden);

    String getDescription();

    void setDescription(String description);

    String getCategory();

    void setCategory(String category);
}