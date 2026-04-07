package net.godlycow.org.essc.command.admin;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.punishment.PunishmentManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BanIpCommand extends Command {

    private final PunishmentManager punishmentManager;

    public BanIpCommand(EssentialsC plugin, PunishmentManager punishmentManager) {
        super(plugin, "ban-ip", "essentialsc.banip", false, 1, "command.usage.banip");
        this.punishmentManager = punishmentManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        String target = args[0];
        String ip;
        String targetName = target;

        if (isValidIp(target)) {
            ip = target;
        } else {
            Player onlineTarget = plugin.getBedrockUtil().resolvePlayer(target);
            if (onlineTarget == null) {
                onlineTarget = plugin.getServer().getPlayerExact(target);
            }

            if (onlineTarget != null && onlineTarget.getAddress() != null) {
                ip = onlineTarget.getAddress().getAddress().getHostAddress();
                targetName = onlineTarget.getName();
            } else {
                OfflinePlayer offlineTarget = plugin.getServer().getOfflinePlayer(target);
                if (offlineTarget.hasPlayedBefore()) {
                    sender.sendMessage(lang.get(sender, "banip.player_offline_no_ip", Map.of("player", target)));
                    return true;
                } else {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", target);
                    sender.sendMessage(lang.get(sender, "error.player_not_found", placeholders));
                    return true;
                }
            }
        }

        Player onlinePlayerWithIp = getOnlinePlayerByIp(ip);
        if (onlinePlayerWithIp != null && onlinePlayerWithIp.hasPermission("essentialsc.ban.exempt")) {
            if (!(sender instanceof Player playerSender) || !playerSender.hasPermission("essentialsc.ban.exempt.bypass")) {
                sender.sendMessage(lang.get(sender, "banip.exempt"));
                plugin.debug("Denied: IP " + ip + " belongs to exempt player " + onlinePlayerWithIp.getName());
                return true;
            }
        }

        if (sender instanceof Player playerSender) {
            if (playerSender.getAddress() != null &&
                    playerSender.getAddress().getAddress().getHostAddress().equals(ip)) {
                sender.sendMessage(lang.get(sender, "banip.cannot_ban_self"));
                return true;
            }
        }

        long duration = 0;
        int reasonStart = 1;

        if (args.length > 1 && args[1].startsWith("-t:")) {
            String timeStr = args[1].substring(3);
            duration = parseDuration(timeStr);
            if (duration == -1) {
                sender.sendMessage(lang.get(sender, "banip.invalid_duration"));
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
            reason = lang.get(sender, "banip.default_reason").toString();
            if (reason.startsWith("<")) {
                reason = "Breaking server rules";
            }
        }

        long expires = duration > 0 ? System.currentTimeMillis() + duration : -1;

        plugin.debug("IP Banning " + ip + " (resolved from: " + targetName + ") by " + sender.getName() +
                " for: " + reason + " expires: " + expires);

        punishmentManager.banIp(ip, reason, sender.getName(), expires);

        int kickedCount = kickPlayersWithIp(ip, sender, reason, duration);

        Map<String, String> broadcastPlaceholders = new HashMap<>();
        broadcastPlaceholders.put("ip", ip);
        broadcastPlaceholders.put("target", targetName);
        broadcastPlaceholders.put("banner", sender.getName());
        broadcastPlaceholders.put("reason", reason);
        broadcastPlaceholders.put("duration", formatDuration(duration));
        broadcastPlaceholders.put("count", String.valueOf(kickedCount));

        String broadcastKey = duration > 0 ? "banip.broadcast_temp" : "banip.broadcast";
        plugin.getServer().broadcast(lang.get(sender, broadcastKey, broadcastPlaceholders), "essentialsc.banip.notify");

        Map<String, String> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("ip", ip);
        senderPlaceholders.put("target", targetName);
        senderPlaceholders.put("duration", formatDuration(duration));
        senderPlaceholders.put("count", String.valueOf(kickedCount));
        sender.sendMessage(lang.get(sender, "banip.success", senderPlaceholders));

        if (plugin.getDiscordSRVHook() != null) {
            plugin.getDiscordSRVHook().sendBanIpEmbed(
                    ip,
                    targetName,
                    reason,
                    sender.getName(),
                    expires > 0 ? expires : -1
            );
        }

        return true;
    }

    private boolean isValidIp(String input) {
        try {
            InetAddress.getByName(input);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private Player getOnlinePlayerByIp(String ip) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getAddress() != null && player.getAddress().getAddress().getHostAddress().equals(ip)) {
                return player;
            }
        }
        return null;
    }

    private int kickPlayersWithIp(String ip, CommandSender banner, String reason, long duration) {
        int count = 0;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getAddress() != null && player.getAddress().getAddress().getHostAddress().equals(ip)) {
                Map<String, String> kickPlaceholders = new HashMap<>();
                kickPlaceholders.put("reason", reason);
                kickPlaceholders.put("banner", banner.getName());
                kickPlaceholders.put("duration", formatDuration(duration));
                kickPlaceholders.put("ip", ip);

                player.kick(lang.get(player, "banip.screen_message", kickPlaceholders));
                count++;
            }
        }
        return count;
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
            List<String> completions = plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());

            if (args[0].matches("\\d+\\.?\\d*")) {
                completions.add("192.168.1.1");
            }
            return completions;
        }
        if (args.length == 2 && args[1].isEmpty()) {
            return Arrays.asList("-t:1h", "-t:1d", "-t:7d", "-t:30d", "-t:perm");
        }
        return Collections.emptyList();
    }
}