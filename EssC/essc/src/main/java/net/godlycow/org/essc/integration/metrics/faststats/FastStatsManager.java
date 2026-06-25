package net.godlycow.org.essc.integration.metrics.faststats;

import dev.faststats.ErrorTracker;
import dev.faststats.Metrics;
import dev.faststats.bukkit.BukkitContext;
import org.bukkit.plugin.java.JavaPlugin;

public class FastStatsManager {


    private BukkitContext context;

    public void init(JavaPlugin plugin) {
        ErrorTracker.contextAware().ignoreError(java.lang.reflect.InvocationTargetException.class).ignoreError(java.io.IOException.class, "Broken pipe").getAttributes()  //ignore IT + IO Exceptions
                .put("plugin_version", plugin.getDescription().getVersion())
                .put("server_version", plugin.getServer().getVersion());

        context = new BukkitContext.Factory(plugin, "753b5c694c676a97c8966eee8a159012")
                .errorTrackerService(ErrorTracker.contextAware().ignoreError(java.lang.reflect.InvocationTargetException.class).ignoreError(java.io.IOException.class, "Broken pipe"))
                .metrics(Metrics.Factory::create)
                .create();

        context.ready();

        plugin.getLogger().info("Enabled FastStats Metrics and Error Tracking");
    }

    public void shutdown() {
        if (context != null) {
            context.shutdown();
        }
    }

    public static ErrorTracker getErrorTracker() {
        return ErrorTracker.contextAware().ignoreError(java.lang.reflect.InvocationTargetException.class).ignoreError(java.io.IOException.class, "Broken pipe");
    }
}