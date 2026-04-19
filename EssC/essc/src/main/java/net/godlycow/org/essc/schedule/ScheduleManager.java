package net.godlycow.org.essc.schedule;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.softwares.SchedulerTask;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class ScheduleManager {

    private final EssentialsC plugin;
    private final File file;

    private final Map<String, Schedule> schedules = new LinkedHashMap<>();
    private final Map<String, SchedulerTask> tasks   = new HashMap<>();
    private SchedulerTask clockTask;
    private boolean masterEnabled = true;

    public ScheduleManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.file   = new File(plugin.getDataFolder(), "schedules.yml");
    }


    public void load() {
        plugin.saveResource("schedules.yml", false);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        migrate(config);

        schedules.clear();
        masterEnabled = config.getBoolean("master-enabled", true);

        var section = config.getConfigurationSection("schedules");
        if (section == null) {
            plugin.debug("[Schedules] No schedules defined in schedules.yml");
            return;
        }

        for (String key : section.getKeys(false)) {
            var sub = section.getConfigurationSection(key);
            if (sub == null) continue;
            schedules.put(key, new Schedule(key, sub));
            plugin.debug("[Schedules] Loaded schedule: " + key);
        }

        startTasks();
        plugin.getLogger().info("[Schedules] Loaded " + schedules.size() + " schedule(s).");
    }

    public void reload() {
        stopTasks();
        load();
    }

    public void shutdown() {
        stopTasks();
        schedules.clear();
    }


    private void startTasks() {
        stopTasks();

        if (!masterEnabled) return;

        for (Schedule schedule : schedules.values()) {
            if (!schedule.isEnabled()) continue;

            if (schedule.isIntervalBased()) {
                long ticks = schedule.getInterval() * 20L;
                SchedulerTask task = plugin.getEssScheduler().runGlobalTimer(
                        () -> run(schedule), ticks, ticks);
                tasks.put(schedule.getName(), task);
            }
        }

        boolean hasTimeBased = schedules.values().stream()
                .anyMatch(s -> s.isEnabled() && !s.isIntervalBased());

        if (hasTimeBased) {
            long secondsUntilNextMinute = 60 - LocalTime.now().getSecond();
            clockTask = plugin.getEssScheduler().runGlobalTimer(
                    this::tickClock,
                    secondsUntilNextMinute * 20L,
                    60 * 20L
            );
        }
    }

    private void stopTasks() {
        tasks.values().forEach(SchedulerTask::cancel);
        tasks.clear();
        if (clockTask != null) {
            clockTask.cancel();
            clockTask = null;
        }
    }

    private void tickClock() {
        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        for (Schedule schedule : schedules.values()) {
            if (!schedule.isEnabled() || schedule.isPaused()) continue;
            if (schedule.isIntervalBased()) continue;
            if (now.equals(schedule.getTriggerTime())) {
                run(schedule);
            }
        }
    }

    private void run(Schedule schedule) {
        if (schedule.isPaused()) return;
        var targets = schedule.resolveTargets();
        if (targets.isEmpty()) return;

        plugin.debug("[Schedules] Running schedule '" + schedule.getName() + "' for " + targets.size() + " player(s).");

        for (ScheduleAction action : schedule.getActions()) {
            action.execute(plugin, targets);
        }
    }

    public boolean runNow(String name) {
        Schedule schedule = schedules.get(name);
        if (schedule == null) return false;
        var targets = schedule.resolveTargets();
        for (ScheduleAction action : schedule.getActions()) {
            action.execute(plugin, targets);
        }
        return true;
    }

    public boolean pause(String name) {
        Schedule schedule = schedules.get(name);
        if (schedule == null || schedule.isPaused()) return false;
        schedule.setPaused(true);
        return true;
    }

    public boolean resume(String name) {
        Schedule schedule = schedules.get(name);
        if (schedule == null || !schedule.isPaused()) return false;
        schedule.setPaused(false);
        return true;
    }

    public boolean isMasterEnabled() {
        return masterEnabled;
    }

    public void setMasterEnabled(boolean enabled) {
        this.masterEnabled = enabled;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("master-enabled", enabled);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("[Schedules] Could not save schedules.yml: " + e.getMessage());
        }
        if (enabled) {
            startTasks();
        } else {
            stopTasks();
        }
    }

    private void migrate(FileConfiguration config) {
        InputStream defaultStream = plugin.getResource("schedules.yml");
        if (defaultStream == null) return;

        FileConfiguration defaults = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
        );

        boolean dirty = false;
        for (String key : defaults.getKeys(true)) {
            if (!config.contains(key)) {
                config.set(key, defaults.get(key));
                dirty = true;
            }
        }

        if (dirty) {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("[Schedules] Could not save schedules.yml: " + e.getMessage());
            }
        }
    }

    public Map<String, Schedule> getSchedules() { return Collections.unmodifiableMap(schedules); }

    public Optional<Schedule> getSchedule(String name) {
        return Optional.ofNullable(schedules.get(name));
    }
}