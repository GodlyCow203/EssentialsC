package net.godlycow.org.essc.modules.schedule;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Schedule {


    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private final String name;
    private final boolean enabled;
    private final int interval;
    private final LocalTime triggerTime;
    private final List<String> worlds;
    private final String permission;
    private final List<ScheduleAction> actions;
    private boolean paused = false;

    public Schedule(String name, ConfigurationSection section) {
        this.name       = name;
        this.enabled    = section.getBoolean("enabled", true);
        this.worlds     = section.getStringList("worlds");
        this.permission = section.getString("permission", null);

        if (section.contains("time")) {
            LocalTime parsed;
            try {
                parsed = LocalTime.parse(section.getString("time", "00:00"), TIME_FORMAT);
            } catch (DateTimeParseException e) {
                parsed = null;
            }
            this.triggerTime = parsed;
            this.interval    = -1;
        } else {
            this.interval    = section.getInt("interval", 600);
            this.triggerTime = null;
        }

        this.actions = new ArrayList<>();
        var actionList = section.getMapList("actions");
        for (var map : actionList) {
            ConfigurationSection actionSection = section.createSection("_tmp", map);
            actions.add(ScheduleAction.from(actionSection));
        }
    }

    public Collection<Player> resolveTargets() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> worlds.isEmpty() || worlds.contains(p.getWorld().getName()))
                .filter(p -> permission == null || p.hasPermission(permission))
                .collect(Collectors.toList());
    }


    public String getName(){
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean p){
        this.paused = p;
    }

    public int getInterval() {
        return interval;
    }

    public LocalTime getTriggerTime(){
        return triggerTime;
    }

    public boolean isIntervalBased() {
        return triggerTime == null;
    }

    public List<ScheduleAction> getActions(){
        return actions;
    }

    public String getSummary(EssentialsC plugin) {
        String triggerKey = isIntervalBased() ? "schedules.trigger.interval" : "schedules.trigger.time";
        String stateKey = !enabled ? "schedules.state.disabled" : paused ? "schedules.state.paused" : "schedules.state.running";

        String triggerValue = isIntervalBased()
                ? String.valueOf(interval)
                : triggerTime.format(TIME_FORMAT);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", name);
        placeholders.put("trigger", triggerValue);
        placeholders.put("state", plugin.getLanguageManager().get(null, stateKey).toString());
        placeholders.put("actions", String.valueOf(actions.size()));

        return plugin.getLanguageManager().get(null, "schedules.summary.format", placeholders).toString();
    }

    public String getStateKey() {
        return !enabled ? "schedules.state.disabled" : paused ? "schedules.state.paused" : "schedules.state.running";
    }

    public String getTriggerTypeKey() {
        return isIntervalBased() ? "schedules.trigger.interval" : "schedules.trigger.time";
    }

    public String getTriggerValue() {
        return isIntervalBased() ? String.valueOf(interval) : triggerTime.format(TIME_FORMAT);
    }
}