package net.godlycow.org.essc.command.admin;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.punishment.PunishmentManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class MuteCommand extends Command {

    private final PunishmentManager punishmentManager;

    public MuteCommand(EssentialsC plugin, PunishmentManager punishmentManager) {
        super(plugin, "mute", "essentialsc.mute", false, 1, "command.usage.mute");
        this.punishmentManager = punishmentManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player target = plugin.getBedrockUtil().resolvePlayer(args[0]);

        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", args[0]);
            sender.sendMessage(lang.get(sender, "error.player_not_found", placeholders));
            return true;
        }

        if (target.equals(sender)) {
            sender.sendMessage(lang.get(sender, "mute.cannot_mute_self"));
            return true;
        }

        if (target.hasPermission("essentialsc.mute.exempt")) {
            if (!(sender instanceof Player playerSender) || !playerSender.hasPermission("essentialsc.mute.exempt.bypass")) {
                sender.sendMessage(lang.get(sender, "mute.exempt"));
                plugin.debug("Denied: " + target.getName() + " is exempt from being muted");
                return true;
            }
        }

        long duration = 0;
        int reasonStart = 1;

        if (args.length > 1 && args[1].startsWith("-t:")) {
            String timeStr = args[1].substring(3);
            duration = parseDuration(timeStr);
            if (duration == -1) {
                sender.sendMessage(lang.get(sender, "mute.invalid_duration"));
                return true;
            }
            reasonStart = 2;
        }

        String reason;
        if (args.length > reasonStart) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = reasonStart; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        } else {
            reason = "Breaking chat rules";
        }

        long expires = duration > 0 ? System.currentTimeMillis() + duration : -1;

        plugin.debug("Muting " + target.getName() + " by " + sender.getName() + " for: " + reason);

        punishmentManager.mutePlayer(target.getUniqueId(), target.getName(), reason, sender.getName(), expires);

        Map<String, String> targetPlaceholders = new HashMap<>();
        targetPlaceholders.put("reason", reason);
        targetPlaceholders.put("muter", sender.getName());
        targetPlaceholders.put("duration", formatDuration(duration));
        target.sendMessage(lang.get(target, "mute.target_message", targetPlaceholders));

        Map<String, String> broadcastPlaceholders = new HashMap<>();
        broadcastPlaceholders.put("target", target.getName());
        broadcastPlaceholders.put("muter", sender.getName());
        broadcastPlaceholders.put("reason", reason);
        broadcastPlaceholders.put("duration", formatDuration(duration));

        String broadcastKey = duration > 0 ? "mute.broadcast_temp" : "mute.broadcast";
        plugin.getServer().broadcast(lang.get(sender, broadcastKey, broadcastPlaceholders), "essentialsc.mute.notify");

        Map<String, String> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("target", target.getName());
        senderPlaceholders.put("duration", formatDuration(duration));
        sender.sendMessage(lang.get(sender, "mute.success", senderPlaceholders));

        if (plugin.getDiscordSRVHook() != null) {
            plugin.getDiscordSRVHook().sendMuteEmbed(
                    target.getUniqueId(),
                    target.getName(),
                    reason,
                    sender.getName(),
                    expires > 0 ? expires : -1
            );
        }

        return true;
    }

    private long parseDuration(String input) {
        if (input.isEmpty()) return 0;

        try {
            if (input.endsWith("s")) return TimeUnit.SECONDS.toMillis(Long.parseLong(input.substring(0, input.length() - 1)));
            if (input.endsWith("m")) return TimeUnit.MINUTES.toMillis(Long.parseLong(input.substring(0, input.length() - 1)));
            if (input.endsWith("h")) return TimeUnit.HOURS.toMillis(Long.parseLong(input.substring(0, input.length() - 1)));
            if (input.endsWith("d")) return TimeUnit.DAYS.toMillis(Long.parseLong(input.substring(0, input.length() - 1)));
            if (input.endsWith("w")) return TimeUnit.DAYS.toMillis(Long.parseLong(input.substring(0, input.length() - 1)) * 7);
            if (input.endsWith("mo")) return TimeUnit.DAYS.toMillis(Long.parseLong(input.substring(0, input.length() - 2)) * 30);
            if (input.endsWith("y")) return TimeUnit.DAYS.toMillis(Long.parseLong(input.substring(0, input.length() - 1)) * 365);
            return Long.parseLong(input) * 1000;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String formatDuration(long millis) {
        if (millis <= 0) return "Permanent";

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;

        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .filter(p -> !p.equals(sender))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[1].isEmpty()) {
            return Arrays.asList("-t:1h", "-t:1d", "-t:7d", "-t:30d");
        }
        return Collections.emptyList();
    }
}
