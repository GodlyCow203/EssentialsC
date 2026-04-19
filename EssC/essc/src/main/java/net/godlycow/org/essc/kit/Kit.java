package net.godlycow.org.essc.kit;

import org.bukkit.inventory.ItemStack;
import java.util.List;

public class Kit {
    private final String name;
    private final String displayName;
    private final String permission;
    private final long cooldown;
    private final boolean oneTime;
    private final boolean firstJoin;
    private final int maxClaims;
    private final List<ItemStack> items;
    private final String description;
    private final boolean networkSync;

    public Kit(String name, String displayName, String permission, long cooldown,
               boolean oneTime, boolean firstJoin, int maxClaims,
               List<ItemStack> items, String description, boolean networkSync) {
        this.name = name;
        this.displayName = displayName;
        this.permission = permission;
        this.cooldown = cooldown;
        this.oneTime = oneTime;
        this.firstJoin = firstJoin;
        this.maxClaims = maxClaims;
        this.items = items;
        this.description = description;
        this.networkSync = networkSync;
    }

    public String getName() {
        return name;
    }
    public String getDisplayName() {
        return displayName;
    }
    public String getPermission() {
        return permission;
    }
    public long getCooldown() {
        return cooldown;
    }
    public boolean isOneTime() {
        return oneTime;
    }
    public boolean isFirstJoin() {
        return firstJoin;
    }
    public int getMaxClaims() {
        return maxClaims;
    }
    public List<ItemStack> getItems() {
        return items;
    }
    public String getDescription() {
        return description;
    }
    public boolean isNetworkSync() {
        return networkSync;
    }
}