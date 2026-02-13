package net.godlycow.org.essc.command.admin;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.punishment.PunishmentManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BanCommand extends Command {

    private final PunishmentManager punishmentManager;

    public BanCommand(EssentialsC plugin, PunishmentManager punishmentManager) {
        super(plugin, "ban", "essentialsc.ban", false, 1, "command.usage.ban");
        this.punishmentManager = punishmentManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        String targetName = args[0];
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", targetName);
            sender.sendMessage(lang.get(sender, "error.player_not_found", placeholders));
            return true;
        }

        if (sender instanceof Player playerSender && target.getUniqueId().equals(playerSender.getUniqueId())) {
            sender.sendMessage(lang.get(sender, "ban.cannot_ban_self"));
            return true;
        }

        if (target.isOnline()) {
            Player onlineTarget = target.getPlayer();
            if (onlineTarget != null && onlineTarget.hasPermission("essentialsc.ban.exempt")) {
                if (!(sender instanceof Player playerSender) || !playerSender.hasPermission("essentialsc.ban.exempt.bypass")) {
                    sender.sendMessage(lang.get(sender, "ban.exempt"));
                    plugin.debug("Denied: " + target.getName() + " is exempt from being banned");
                    return true;
                }
            }
        }

        long duration = 0;
        int reasonStart = 1;

        if (args.length > 1 && args[1].startsWith("-t:")) {
            String timeStr = args[1].substring(3);
            duration = parseDuration(timeStr);
            if (duration == -1) {
                sender.sendMessage(lang.get(sender, "ban.invalid_duration"));
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
            reason = lang.get(sender, "ban.default_reason").toString();
            if (reason.startsWith("<")) {
                reason = "Breaking server rules";
            }
        }

        long expires = duration > 0 ? System.currentTimeMillis() + duration : -1;

        plugin.debug("Banning " + target.getName() + " by " + sender.getName() + " for: " + reason + " expires: " + expires);

        punishmentManager.banPlayer(target.getUniqueId(), target.getName(), reason, sender.getName(), expires);

        if (target.isOnline()) {
            Player onlineTarget = target.getPlayer();
            if (onlineTarget != null) {
                Map<String, String> kickPlaceholders = new HashMap<>();
                kickPlaceholders.put("reason", reason);
                kickPlaceholders.put("banner", sender.getName());
                kickPlaceholders.put("duration", formatDuration(duration));

                onlineTarget.kick(lang.get(onlineTarget, "ban.screen_message", kickPlaceholders));
            }
        }

        Map<String, String> broadcastPlaceholders = new HashMap<>();
        broadcastPlaceholders.put("target", target.getName());
        broadcastPlaceholders.put("banner", sender.getName());
        broadcastPlaceholders.put("reason", reason);
        broadcastPlaceholders.put("duration", formatDuration(duration));

        String broadcastKey = duration > 0 ? "ban.broadcast_temp" : "ban.broadcast";
        plugin.getServer().broadcast(lang.get(sender, broadcastKey, broadcastPlaceholders), "essentialsc.ban.notify");

        Map<String, String> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("target", target.getName());
        senderPlaceholders.put("duration", formatDuration(duration));
        sender.sendMessage(lang.get(sender, "ban.success", senderPlaceholders));

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