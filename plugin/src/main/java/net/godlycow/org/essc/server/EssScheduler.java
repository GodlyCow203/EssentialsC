package net.godlycow.org.essc.server;

import net.godlycow.org.essc.modules.back.BackManager;
import net.godlycow.org.essc.server.software.ServerSoftware;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public class EssScheduler {

    private final Plugin plugin;

    public EssScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Boolean> teleportAsync(Player player, Location location) {
        return teleportAsync(player, location, true);
    }

    public CompletableFuture<Boolean> teleportAsync(Player player, Location location, boolean saveBack) {
        net.godlycow.org.essc.EssentialsC essc = (net.godlycow.org.essc.EssentialsC) plugin;
        BackManager backManager = essc.getBackManager();

        if (saveBack && backManager != null && player.isOnline()) {
            backManager.setBackLocation(player, player.getLocation());
        }

        if (ServerSoftware.isFolia()) {
            return player.teleportAsync(location);
        } else {
            try {
                return player.teleportAsync(location);
            } catch (NoSuchMethodError e) {
                return CompletableFuture.completedFuture(player.teleport(location));
            }
        }
    }
}
