package net.godlycow.org.essc.softwares;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitTask;

public class SchedulerTask {

    private final BukkitTask bukkitTask;
    private final ScheduledTask foliaTask;

    public SchedulerTask(BukkitTask task) {
        this.bukkitTask = task;
        this.foliaTask = null;
    }

    public SchedulerTask(ScheduledTask task) {
        this.foliaTask = task;
        this.bukkitTask = null;
    }

    public void cancel() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
        } else if (foliaTask != null) {
            foliaTask.cancel();
        }
    }

    public boolean isCancelled() {
        if (bukkitTask != null) {
            return bukkitTask.isCancelled();
        }
        if (foliaTask != null) {
            return foliaTask.isCancelled();
        }
        return true;
    }
}
