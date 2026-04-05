package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class MsgCommand extends Command {

    public MsgCommand(EssentialsC plugin) {
        super(plugin, "msg", "essentialsc.msg", false, 2, "command.usage.msg");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        String targetName = args[0];
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", targetName);
            sender.sendMessage(lang.get(sender, "error.player_not_found", placeholders));
            return true;
        }

        if (target.equals(sender)) {
            sender.sendMessage(lang.get(sender, "msg.cannot_message_self"));
            return true;
        }

        if (sender instanceof Player playerSender) {
            if (plugin.getIgnoreManager().isIgnoring(target.getUniqueId(), playerSender.getUniqueId())) {
                sender.sendMessage(lang.get(sender, "msg.ignored_by_target"));
                return true;
            }
        }

        if (sender instanceof Player playerSender) {
            if (plugin.getIgnoreManager().isIgnoring(playerSender.getUniqueId(), target.getUniqueId())) {
                sender.sendMessage(lang.get(sender, "msg.ignoring_target"));
                return true;
            }
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String message = messageBuilder.toString().trim();

        if (message.isEmpty()) {
            sendUsage(sender);
            return true;
        }

        if (sender instanceof Player playerSender) {
            plugin.getReplyManager().setReplyTarget(target.getUniqueId(), playerSender.getUniqueId());
        }
        plugin.getReplyManager().setReplyTarget(target.getUniqueId(), sender instanceof Player ? ((Player) sender).getUniqueId() : null);

        plugin.debug("Message from " + sender.getName() + " to " + target.getName() + ": " + message);

        Map<String, String> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("target", target.getName());
        senderPlaceholders.put("message", message);
        sender.sendMessage(lang.get(sender, "msg.outgoing", senderPlaceholders));

        Map<String, String> targetPlaceholders = new HashMap<>();
        targetPlaceholders.put("sender", sender instanceof Player ? sender.getName() : "Console");
        targetPlaceholders.put("message", message);
        target.sendMessage(lang.get(target, "msg.incoming", targetPlaceholders));

        Map<String, String> spyPlaceholders = new HashMap<>();
        spyPlaceholders.put("sender", sender.getName());
        spyPlaceholders.put("target", target.getName());
        spyPlaceholders.put("message", message);

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.hasPermission("essentialsc.msg.spy") && !online.equals(sender) && !online.equals(target)) {
                online.sendMessage(lang.get(online, "msg.spy", spyPlaceholders));
            }
        }

        return true;
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
        return Collections.emptyList();
    }
}