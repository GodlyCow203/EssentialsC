package net.godlycow.org.essc.command.admin;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.punishment.PunishmentManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CheckpunishCommand extends Command {

    private final PunishmentManager punishmentManager;

    public CheckpunishCommand(EssentialsC plugin, PunishmentManager punishmentManager) {
        super(plugin, "checkpunish", "essentialsc.checkpunish", false, 1, "command.usage.checkpunish");
        this.punishmentManager = punishmentManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);

        boolean isBanned = punishmentManager.isBanned(target.getUniqueId());
        boolean isMuted = punishmentManager.isMuted(target.getUniqueId());

        if (!isBanned && !isMuted) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            sender.sendMessage(lang.get(sender, "checkpunish.clean", placeholders));
            return true;
        }

        sender.sendMessage(lang.get(sender, "checkpunish.header"));

        if (isBanned) {
            PunishmentManager.BanEntry ban = punishmentManager.getBanEntry(target.getUniqueId());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("reason", ban.reason());
            placeholders.put("banner", ban.banner());
            placeholders.put("time", formatTime(ban.time()));
            placeholders.put("expires", ban.expires() > 0 ? formatTime(ban.expires()) : "Never");
            placeholders.put("remaining", ban.expires() > 0 ? formatDuration(ban.expires() - System.currentTimeMillis()) : "Permanent");
            sender.sendMessage(lang.get(sender, "checkpunish.ban_info", placeholders));
        }

        if (isMuted) {
            PunishmentManager.MuteEntry mute = punishmentManager.getMuteEntry(target.getUniqueId());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("reason", mute.reason());
            placeholders.put("muter", mute.muter());
            placeholders.put("time", formatTime(mute.time()));
            placeholders.put("expires", mute.expires() > 0 ? formatTime(mute.expires()) : "Never");
            placeholders.put("remaining", mute.expires() > 0 ? formatDuration(mute.expires() - System.currentTimeMillis()) : "Permanent");
            sender.sendMessage(lang.get(sender, "checkpunish.mute_info", placeholders));
        }

        return true;
    }

    private String formatTime(long millis) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date(millis));
    }

    private String formatDuration(long millis) {
        if (millis <= 0) return "Expired";
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        if (days > 0) return days + "d " + hours + "h";
        return hours + "h";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(org.bukkit.entity.Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}