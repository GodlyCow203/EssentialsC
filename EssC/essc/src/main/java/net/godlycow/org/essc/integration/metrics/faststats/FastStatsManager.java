package net.godlycow.org.essc.integration.metrics.faststats;

import dev.faststats.Metrics;
import dev.faststats.bukkit.BukkitContext;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class FastStatsManager {

    private BukkitContext context;

    public void init(JavaPlugin plugin) {

        context = new BukkitContext.Factory(plugin, "753b5c694c676a97c8966eee8a159012").metrics(Metrics.Factory::create).create();
        context.ready();

        Bukkit.getLogger().info("Enabled FastStats Metrics");
    }

    public void shutdown() {
        if (context != null) {
            context.shutdown();
        }
    }
}