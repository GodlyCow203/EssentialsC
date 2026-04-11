package net.godlycow.org.essc.command.admin;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.punishment.PunishmentManager;
import net.godlycow.org.essc.util.DurationParser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

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
            sender.sendMessage(lang.get(sender, "error.player_not_found", Map.of("player", args[0])));
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
            duration = DurationParser.parse(args[1].substring(3));
            if (duration == DurationParser.INVALID) {
                sender.sendMessage(lang.get(sender, "mute.invalid_duration"));
                return true;
            }
            if (duration == DurationParser.PERMANENT) duration = 0;
            reasonStart = 2;
        }

        String reason = buildReason(args, reasonStart, "Breaking chat rules");
        long expires = duration > 0 ? System.currentTimeMillis() + duration : -1;

        plugin.debug("Muting " + target.getName() + " by " + sender.getName() + " for: " + reason);
        punishmentManager.mutePlayer(target.getUniqueId(), target.getName(), reason, sender.getName(), expires);

        String durationStr = DurationParser.format(duration);

        target.sendMessage(lang.get(target, "mute.target_message", Map.of(
                "reason",   reason,
                "muter",    sender.getName(),
                "duration", durationStr
        )));

        plugin.getServer().broadcast(lang.get(sender, duration > 0 ? "mute.broadcast_temp" : "mute.broadcast", Map.of(
                "target",   target.getName(),
                "muter",    sender.getName(),
                "reason",   reason,
                "duration", durationStr
        )), "essentialsc.mute.notify");

        sender.sendMessage(lang.get(sender, "mute.success", Map.of(
                "target",   target.getName(),
                "duration", durationStr
        )));

        if (plugin.getDiscordSRVHook() != null) {
            plugin.getDiscordSRVHook().sendMuteEmbed(
                    target.getUniqueId(), target.getName(), reason, sender.getName(),
                    expires > 0 ? expires : -1);
        }

        return true;
    }

    private String buildReason(String[] args, int start, String defaultReason) {
        if (args.length <= start) return defaultReason;
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) sb.append(" ");
            sb.append(args[i]);
        }
        String reason = sb.toString().trim();
        return reason.isEmpty() ? defaultReason : reason;
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