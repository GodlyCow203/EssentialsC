package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.SchedulesApi;
import net.godlycow.org.essc.api.schedule.ScheduleEntry;
import net.godlycow.org.essc.schedule.Schedule;
import net.godlycow.org.essc.schedule.ScheduleManager;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SchedulesApiImpl implements SchedulesApi {

    private final ScheduleManager manager;

    public SchedulesApiImpl(ScheduleManager manager) {
        this.manager = manager;
    }

    @Override
    public List<String> getScheduleNames() {
        return List.copyOf(manager.getSchedules().keySet());
    }

    @Override
    public Optional<ScheduleEntry> getSchedule(String name) {
        return manager.getSchedule(name).map(this::mapToEntry);
    }

    @Override
    public List<ScheduleEntry> getAllSchedules() {
        return manager.getSchedules().values().stream()
                .map(this::mapToEntry)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isMasterEnabled() {
        return manager.isMasterEnabled();
    }

    @Override
    public void setMasterEnabled(boolean enabled) {
        manager.setMasterEnabled(enabled);
    }

    @Override
    public boolean runNow(String name) {
        return manager.runNow(name);
    }

    @Override
    public boolean pause(String name) {
        return manager.pause(name);
    }

    @Override
    public boolean resume(String name) {
        return manager.resume(name);
    }

    @Override
    public void reload() {
        manager.reload();
    }

    private ScheduleEntry mapToEntry(Schedule s) {
        return new ScheduleEntry(
                s.getName(),
                s.isEnabled(),
                s.isPaused(),
                s.isIntervalBased(),
                s.getInterval(),
                s.getTriggerTime() != null ? s.getTriggerTime().toString() : null,
                s.getActions().size(),
                List.copyOf(s.resolveTargets().isEmpty() ? List.of() : s.resolveTargets().stream()
                        .findFirst().map(p -> p.getWorld().getName()).map(List::of).orElse(List.of())),
                null
        );
    }
}