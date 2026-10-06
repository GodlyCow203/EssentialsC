package net.godlycow.org.essc.api.warp;

import org.bukkit.Location;

/**
 * A server warp. Get them from {@link WarpManager}. Setters only change
 * the object, call {@link WarpManager#updateWarp(Warp)} to save.
 */
public interface Warp {

    /**
     * Gets the warp name.
     *
     * @return the warp name
     */
    String getName();

    /**
     * Gets the warp location.
     *
     * @return the location
     */
    Location getLocation();

    /**
     * Sets the warp location.
     *
     * @param location the new location
     */
    void setLocation(Location location);

    /**
     * Gets the permission needed to use it, null if open to all.
     *
     * @return the permission, or null
     */
    String getPermission();

    /**
     * Sets the permission, null to clear.
     *
     * @param permission the permission, or null
     */
    void setPermission(String permission);

    /**
     * Gets the cost per use, 0 means free.
     *
     * @return the cost
     */
    double getCost();

    /**
     * Sets the cost per use.
     *
     * @param cost the new cost
     */
    void setCost(double cost);

    /**
     * Checks if the warp is hidden from public lists.
     *
     * @return true if hidden
     */
    boolean isHidden();

    /**
     * Hides or unhides the warp.
     *
     * @param hidden true to hide
     */
    void setHidden(boolean hidden);

    /**
     * Gets the description shown in lists and GUIs.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    void setDescription(String description);

    /**
     * Gets the category, null if none.
     *
     * @return the category, or null
     */
    String getCategory();

    /**
     * Sets the category, null to clear.
     *
     * @param category the new category, or null
     */
    void setCategory(String category);
}
