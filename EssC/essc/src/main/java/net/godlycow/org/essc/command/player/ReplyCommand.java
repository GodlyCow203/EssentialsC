package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ReplyCommand extends Command {

    public ReplyCommand(EssentialsC plugin) {
        super(plugin, "reply", "essentialsc.reply", false, 1, "command.usage.reply");
        this.aliases = new String[]{"r"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        UUID replyTarget = null;

        if (sender instanceof Player player) {
            replyTarget = plugin.getReplyManager().getReplyTarget(player.getUniqueId());
        } else {
            sender.sendMessage(lang.get(sender, "reply.console_no_reply"));
            return true;
        }

        if (replyTarget == null) {
            sender.sendMessage(lang.get(sender, "reply.no_reply_target"));
            return true;
        }

        Player target = plugin.getServer().getPlayer(replyTarget);

        if (target == null) {
            sender.sendMessage(lang.get(sender, "reply.target_offline"));
            plugin.getReplyManager().removeReplyTarget(((Player) sender).getUniqueId());
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (String arg : args) {
            messageBuilder.append(arg).append(" ");
        }
        String message = messageBuilder.toString().trim();

        if (message.isEmpty()) {
            sendUsage(sender);
            return true;
        }

        Player playerSender = (Player) sender;

        if (plugin.getIgnoreManager().isIgnoring(target.getUniqueId(), playerSender.getUniqueId())) {
            sender.sendMessage(lang.get(sender, "msg.ignored_by_target"));
            return true;
        }

        if (plugin.getIgnoreManager().isIgnoring(playerSender.getUniqueId(), target.getUniqueId())) {
            sender.sendMessage(lang.get(sender, "msg.ignoring_target"));
            return true;
        }

        plugin.getReplyManager().setReplyTarget(target.getUniqueId(), playerSender.getUniqueId());
        plugin.getReplyManager().setReplyTarget(playerSender.getUniqueId(), target.getUniqueId());

        plugin.debug("Reply from " + sender.getName() + " to " + target.getName() + ": " + message);

        Map<String, String> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("target", target.getName());
        senderPlaceholders.put("message", message);
        sender.sendMessage(lang.get(sender, "msg.outgoing", senderPlaceholders));

        Map<String, String> targetPlaceholders = new HashMap<>();
        targetPlaceholders.put("sender", sender.getName());
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
        return Collections.emptyList();
    }
}