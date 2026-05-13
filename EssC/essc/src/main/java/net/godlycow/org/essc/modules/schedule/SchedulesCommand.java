package net.godlycow.org.essc.modules.schedule;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class SchedulesCommand extends Command {

    private final ScheduleManager scheduleManager;

    public SchedulesCommand(EssentialsC plugin) {
        super(plugin, "schedules", "essentialsc.schedules", false, 1, "command.usage.schedules");
        this.scheduleManager = plugin.getScheduleManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        switch (args[0].toLowerCase()) {
            case "list"   -> executeList(sender);
            case "info"   -> executeInfo(sender, args);
            case "run"    -> executeRun(sender, args);
            case "pause"  -> executePause(sender, args);
            case "resume" -> executeResume(sender, args);
            case "on"     -> executeOn(sender);
            case "off"    -> executeOff(sender);
            default       -> sendUsage(sender);
        }
        return true;
    }

    private void executeList(CommandSender sender) {
        Map<String, Schedule> schedules = scheduleManager.getSchedules();

        if (schedules.isEmpty()) {
            sender.sendMessage(lang.get(sender, "schedules.list.empty"));
            return;
        }

        sender.sendMessage(lang.get(sender, "schedules.list.header"));

        for (Schedule schedule : schedules.values()) {
            NamedTextColor stateColor = !schedule.isEnabled() ? NamedTextColor.DARK_GRAY
                    : schedule.isPaused() ? NamedTextColor.YELLOW
                    : NamedTextColor.GREEN;

            Component stateLabel = lang.get(sender, schedule.getStateKey());

            String triggerKey = schedule.getTriggerTypeKey();
            Map<String, String> triggerPlaceholders = new HashMap<>();
            triggerPlaceholders.put("value", schedule.getTriggerValue());
            Component trigger = lang.get(sender, triggerKey, triggerPlaceholders);

            Map<String, String> suffixPlaceholders = new HashMap<>();
            suffixPlaceholders.put("trigger", schedule.getTriggerValue());
            suffixPlaceholders.put("actions", String.valueOf(schedule.getActions().size()));

            Component line = Component.text("  » ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(schedule.getName(), NamedTextColor.WHITE)
                            .clickEvent(ClickEvent.runCommand("/schedules info " + schedule.getName()))
                            .hoverEvent(HoverEvent.showText(lang.get(sender, "schedules.list.click_for_details"))))
                    .append(Component.text(" ["))
                    .append(stateLabel.color(stateColor))
                    .append(Component.text("]"))
                    .append(lang.get(sender, "schedules.list.entry.suffix", suffixPlaceholders));

            sender.sendMessage(line);
        }
    }

    private void executeInfo(CommandSender sender, String[] args) {
        if (args.length < 2) { sendUsage(sender); return; }

        scheduleManager.getSchedule(args[1]).ifPresentOrElse(schedule -> {
            String triggerKey = schedule.getTriggerTypeKey();
            Map<String, String> triggerPlaceholders = new HashMap<>();
            triggerPlaceholders.put("value", schedule.getTriggerValue());
            Component trigger = lang.get(sender, triggerKey + ".full", triggerPlaceholders);

            Component state = lang.get(sender, schedule.getStateKey());

            sender.sendMessage(lang.get(sender, "schedules.info.header"));

            Map<String, String> namePlaceholders = new HashMap<>();
            namePlaceholders.put("name", schedule.getName());
            sender.sendMessage(lang.get(sender, "schedules.info.name", namePlaceholders));

            sender.sendMessage(
                    Component.empty()
                            .append(lang.get(sender, "schedules.info.trigger.prefix"))
                            .append(trigger)
            );

            sender.sendMessage(
                    Component.empty()
                            .append(lang.get(sender, "schedules.info.state.prefix"))
                            .append(state)
            );

            Map<String, String> actionsPlaceholders = new HashMap<>();
            actionsPlaceholders.put("count", String.valueOf(schedule.getActions().size()));
            sender.sendMessage(lang.get(sender, "schedules.info.actions", actionsPlaceholders));

        }, () -> sender.sendMessage(lang.get(sender, "schedules.not_found")));
    }

    private void executeRun(CommandSender sender, String[] args) {
        if (args.length < 2) { sendUsage(sender); return; }

        if (scheduleManager.runNow(args[1])) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", args[1]);
            sender.sendMessage(lang.get(sender, "schedules.run.success", placeholders));
        } else {
            sender.sendMessage(lang.get(sender, "schedules.not_found"));
        }
    }

    private void executePause(CommandSender sender, String[] args) {
        if (args.length < 2) { sendUsage(sender); return; }

        if (scheduleManager.pause(args[1])) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", args[1]);
            sender.sendMessage(lang.get(sender, "schedules.pause.success", placeholders));
        } else {
            scheduleManager.getSchedule(args[1]).ifPresentOrElse(
                    s -> {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("name", args[1]);
                        sender.sendMessage(lang.get(sender, "schedules.already_paused", placeholders));
                    },
                    () -> sender.sendMessage(lang.get(sender, "schedules.not_found"))
            );
        }
    }

    private void executeResume(CommandSender sender, String[] args) {
        if (args.length < 2) { sendUsage(sender); return; }

        if (scheduleManager.resume(args[1])) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", args[1]);
            sender.sendMessage(lang.get(sender, "schedules.resume.success", placeholders));
        } else {
            scheduleManager.getSchedule(args[1]).ifPresentOrElse(
                    s -> {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("name", args[1]);
                        sender.sendMessage(lang.get(sender, "schedules.not_paused", placeholders));
                    },
                    () -> sender.sendMessage(lang.get(sender, "schedules.not_found"))
            );
        }
    }

    private void executeOn(CommandSender sender) {
        if (scheduleManager.isMasterEnabled()) {
            sender.sendMessage(lang.get(sender, "schedules.master.already_on"));
        } else {
            scheduleManager.setMasterEnabled(true);
            sender.sendMessage(lang.get(sender, "schedules.master.on"));
        }
    }

    private void executeOff(CommandSender sender) {
        if (!scheduleManager.isMasterEnabled()) {
            sender.sendMessage(lang.get(sender, "schedules.master.already_off"));
        } else {
            scheduleManager.setMasterEnabled(false);
            sender.sendMessage(lang.get(sender, "schedules.master.off"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filter(List.of("list", "info", "run", "pause", "resume", "on", "off"), args[0]);
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("list") && !args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off")) {
            return filter(new ArrayList<>(scheduleManager.getSchedules().keySet()), args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String input) {
        return options.stream()
                .filter(o -> o.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }
}