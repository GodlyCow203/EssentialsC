package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.schedule.ScheduleEntry;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * API interface for interacting with EssentialsC's schedule system.
 *
 * <p>Schedules are loaded from schedules.yml and support both time-based
 * (cron-like) and interval-based triggering. All schedule state changes
 * are synchronous and must be called on the main thread.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getSchedulesApi()}.</p>
 *
 * <pre>{@code
 * SchedulesApi schedules = APIProvider.getAPI().getSchedulesApi();
 *
 * schedules.getSchedule("announcement").ifPresent(s -> {
 *     if (s.isActive()) {
 *         schedules.runNow("announcement");
 *     }
 * });
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface SchedulesApi {

    /**
     * Returns a list of all configured schedule names.
     *
     * @return an unmodifiable list of schedule names; never {@code null}
     */
    List<String> getScheduleNames();

    /**
     * Returns the schedule entry with the given name, if it exists.
     *
     * @param name the schedule name to look up; must not be {@code null}
     * @return an {@link Optional} containing the schedule, or empty if not found
     */
    Optional<ScheduleEntry> getSchedule(String name);

    /**
     * Returns all configured schedules as a list.
     *
     * @return an unmodifiable list of all schedules; never {@code null}
     */
    List<ScheduleEntry> getAllSchedules();

    /**
     * Returns whether the schedule system master switch is enabled.
     *
     * @return {@code true} if schedules are globally enabled
     */
    boolean isMasterEnabled();

    /**
     * Sets the master enable state for the schedule system.
     *
     * <p>When disabled, all scheduled tasks are stopped. When enabled,
     * tasks are restarted according to their individual configurations.
     * This change is persisted to schedules.yml.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param enabled {@code true} to enable schedules globally
     */
    void setMasterEnabled(boolean enabled);

    /**
     * Immediately executes the schedule with the given name.
     *
     * <p>Runs all actions for the schedule's current target players,
     * regardless of trigger conditions or cooldowns.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param name the schedule to run; must not be {@code null}
     * @return {@code true} if the schedule was found and executed
     */
    boolean runNow(String name);

    /**
     * Pauses the schedule with the given name.
     *
     * <p>Paused schedules will not trigger automatically, but can still
     * be executed manually via {@link #runNow(String)}.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param name the schedule to pause; must not be {@code null}
     * @return {@code true} if the schedule was found and is now paused
     */
    boolean pause(String name);

    /**
     * Resumes the schedule with the given name.
     *
     * <p>Unpauses a previously paused schedule, allowing it to trigger
     * automatically again.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param name the schedule to resume; must not be {@code null}
     * @return {@code true} if the schedule was found and is now resumed
     */
    boolean resume(String name);

    /**
     * Reloads all schedules from disk.
     *
     * <p>Re-reads schedules.yml, stops all current tasks, and restarts
     * them with the new configuration. This is equivalent to
     * {@code /schedules reload}.</p>
     *
     * <p>Must be called on the main thread.</p>
     */
    void reload();
}