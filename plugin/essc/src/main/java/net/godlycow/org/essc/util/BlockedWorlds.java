package net.godlycow.org.essc.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public final class BlockedWorlds {

    private BlockedWorlds() {
    }

    public static boolean isBlocked(Player player, List<String> blockedWorlds, String bypassPermission, String worldName) {
        return worldName != null
                && blockedWorlds.contains(worldName)
                && !player.hasPermission(bypassPermission);
    }

    public static boolean isBlocked(Player player, List<String> blockedWorlds, String bypassPermission, Location location) {
        World world = location.getWorld();
        return world != null && isBlocked(player, blockedWorlds, bypassPermission, world.getName());
    }
}
