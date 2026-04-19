package net.godlycow.org.essc.softwares;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class EssScheduler {

    private final Plugin plugin;

    public EssScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    public void runAsync(Runnable task) {
        if (ServerSoftware.isFolia()) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, t -> task.run());
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public SchedulerTask runGlobal(Runnable task) {
        if (ServerSoftware.isFolia()) {
            return new SchedulerTask(
                    plugin.getServer().getGlobalRegionScheduler().run(plugin, t -> task.run())
            );
        } else {
            return new SchedulerTask(
                    plugin.getServer().getScheduler().runTask(plugin, task)
            );
        }
    }

    public SchedulerTask runGlobalLater(Runnable task, long delayTicks) {
        if (ServerSoftware.isFolia()) {
            return new SchedulerTask(
                    plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), delayTicks)
            );
        } else {
            return new SchedulerTask(
                    plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks)
            );
        }
    }

    public SchedulerTask runGlobalTimer(Runnable task, long delayTicks, long periodTicks) {
        if (ServerSoftware.isFolia()) {
            return new SchedulerTask(
                    plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), delayTicks, periodTicks)
            );
        } else {
            return new SchedulerTask(
                    plugin.getServer().getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks)
            );
        }
    }

    public SchedulerTask runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
        if (ServerSoftware.isFolia()) {
            long delayMs = delayTicks * 50L;
            long periodMs = periodTicks * 50L;
            return new SchedulerTask(
                    plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, t -> task.run(), delayMs, periodMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            );
        } else {
            return new SchedulerTask(
                    plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks)
            );
        }
    }

    public SchedulerTask runForEntity(Entity entity, Runnable task) {
        if (ServerSoftware.isFolia()) {
            return new SchedulerTask(
                    entity.getScheduler().run(plugin, t -> task.run(), null)
            );
        } else {
            return new SchedulerTask(
                    plugin.getServer().getScheduler().runTask(plugin, task)
            );
        }
    }

    public SchedulerTask runForEntityLater(Entity entity, Runnable task, long delayTicks) {
        if (ServerSoftware.isFolia()) {
            return new SchedulerTask(
                    entity.getScheduler().runDelayed(plugin, t -> task.run(), null, delayTicks)
            );
        } else {
            return new SchedulerTask(
                    plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks)
            );
        }
    }

    public SchedulerTask runForEntityTimer(Entity entity, Runnable task, long delayTicks, long periodTicks) {
        if (ServerSoftware.isFolia()) {
            return new SchedulerTask(
                    entity.getScheduler().runAtFixedRate(plugin, t -> task.run(), null, delayTicks, periodTicks)
            );
        } else {
            return new SchedulerTask(
                    plugin.getServer().getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks)
            );
        }
    }

    public SchedulerTask runForLocation(Location location, Runnable task) {
        if (ServerSoftware.isFolia()) {
            return new SchedulerTask(
                    plugin.getServer().getRegionScheduler().run(plugin, location, t -> task.run())
            );
        } else {
            return new SchedulerTask(
                    plugin.getServer().getScheduler().runTask(plugin, task)
            );
        }
    }

    public SchedulerTask runForLocationLater(Location location, Runnable task, long delayTicks) {
        if (ServerSoftware.isFolia()) {
            return new SchedulerTask(
                    plugin.getServer().getRegionScheduler().runDelayed(plugin, location, t -> task.run(), delayTicks)
            );
        } else {
            return new SchedulerTask(
                    plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks)
            );
        }
    }

    public SchedulerTask runForLocationTimer(Location location, Runnable task, long delayTicks, long periodTicks) {
        if (ServerSoftware.isFolia()) {
            return new SchedulerTask(
                    plugin.getServer().getRegionScheduler().runAtFixedRate(plugin, location, t -> task.run(), delayTicks, periodTicks)
            );
        } else {
            return new SchedulerTask(
                    plugin.getServer().getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks)
            );
        }
    }

    public CompletableFuture<Boolean> teleportAsync(Player player, Location location) {
        return teleportAsync(player, location, true);
    }

    public CompletableFuture<Boolean> teleportAsync(Player player, Location location, boolean saveBack) {
        net.godlycow.org.essc.EssentialsC essc = (net.godlycow.org.essc.EssentialsC) plugin;
        net.godlycow.org.essc.back.BackManager backManager = essc.getBackManager();

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

    public void runOnRegion(Location location, Runnable task) {
        if (ServerSoftware.isFolia()) {
            plugin.getServer().getRegionScheduler().run(plugin, location, t -> task.run());
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    public Executor asyncExecutor() {
        if (ServerSoftware.isFolia()) {
            return runnable -> plugin.getServer().getAsyncScheduler().runNow(plugin, t -> runnable.run());
        } else {
            return runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }
}