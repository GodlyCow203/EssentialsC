package net.godlycow.org.essc.command.admin;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.punishment.PunishmentManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnmuteCommand extends Command {

    private final PunishmentManager punishmentManager;

    public UnmuteCommand(EssentialsC plugin, PunishmentManager punishmentManager) {
        super(plugin, "unmute", "essentialsc.unmute", false, 1, "command.usage.unmute");
        this.punishmentManager = punishmentManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player target = plugin.getServer().getPlayer(args[0]);

        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", args[0]);
            sender.sendMessage(lang.get(sender, "error.player_not_found", placeholders));
            return true;
        }

        if (!punishmentManager.isMuted(target.getUniqueId())) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            sender.sendMessage(lang.get(sender, "unmute.not_muted", placeholders));
            return true;
        }

        punishmentManager.unmutePlayer(target.getUniqueId());

        target.sendMessage(lang.get(target, "unmute.target_message"));

        Map<String, String> broadcastPlaceholders = new HashMap<>();
        broadcastPlaceholders.put("target", target.getName());
        broadcastPlaceholders.put("unmuter", sender.getName());

        plugin.getServer().broadcast(lang.get(sender, "unmute.broadcast", broadcastPlaceholders), "essentialsc.mute.notify");

        Map<String, String> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("target", target.getName());
        sender.sendMessage(lang.get(sender, "unmute.success", senderPlaceholders));

        plugin.debug("Unmuted " + target.getName() + " by " + sender.getName());
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .filter(p -> punishmentManager.isMuted(p.getUniqueId()))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}