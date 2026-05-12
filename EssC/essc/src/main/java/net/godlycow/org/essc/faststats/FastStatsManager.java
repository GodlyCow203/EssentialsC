package net.godlycow.org.essc.faststats;

import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class FastStatsManager {

    private Metrics metrics;


    public void init(JavaPlugin plugin) {
        metrics = BukkitMetrics.factory()
                .token("753b5c694c676a97c8966eee8a159012")
                .create(plugin);
        Bukkit.getLogger().info("Enabled Faststats Metrics");
    }

}