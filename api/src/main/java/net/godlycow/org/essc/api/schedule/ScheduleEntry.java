package net.godlycow.org.essc.api.schedule;

import java.util.List;

/**
 * An immutable snapshot of a configured schedule.
 *
 * <p>Instances are returned by {@link net.godlycow.org.essc.api.SchedulesApi}.
 * Schedules are loaded from schedules.yml and can be time-based or interval-based.</p>
 *
 * @see net.godlycow.org.essc.api.SchedulesApi#getSchedule(String)
 */
public record ScheduleEntry(

        /**
         * The unique name of this schedule.
         */
        String name,

        /**
         * Whether this schedule is enabled in configuration.
         */
        boolean enabled,

        /**
         * Whether this schedule is currently paused.
         */
        boolean paused,

        /**
         * Whether this schedule is interval-based (true) or time-based (false).
         */
        boolean intervalBased,

        /**
         * The interval in seconds between executions, or {@code -1} if time-based.
         */
        int interval,

        /**
         * The time string in HH:mm format, or {@code null} if interval-based.
         */
        String time,

        /**
         * The number of actions configured for this schedule.
         */
        int actionCount,

        /**
         * The list of world names this schedule targets, or empty for all worlds.
         */
        List<String> worlds,

        /**
         * The required permission to be targeted, or {@code null} if none.
         */
        String permission
) {

    /**
     * Returns the current state key for this schedule.
     *
     * @return "disabled", "paused", or "running"
     */
    public String getState() {
        if (!enabled) return "disabled";
        if (paused) return "paused";
        return "running";
    }

    /**
     * Returns whether this schedule would execute if triggered now.
     *
     * @return {@code true} if enabled and not paused
     */
    public boolean isActive() {
        return enabled && !paused;
    }
}