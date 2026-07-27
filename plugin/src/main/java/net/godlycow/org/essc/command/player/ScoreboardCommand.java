package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ScoreboardCommand extends Command {

    public ScoreboardCommand(EssentialsC plugin) {
        super(plugin, "scoreboard", "Toggle or reload the scoreboard", false, 0);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            showUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "toggle" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(lang.get(sender, "error.player_only"));
                    return true;
                }

                if (!sender.hasPermission("essentialsc.scoreboard.toggle")) {
                    sender.sendMessage(lang.get(sender, "error.no_permission"));
                    return true;
                }

                if (plugin.getScoreboardManager() == null) {
                    sender.sendMessage(lang.get(sender, "scoreboard.disabled"));
                    return true;
                }

                plugin.getScoreboardManager().toggle(player);
            }

            case "reload" -> {
                if (!sender.hasPermission("essentialsc.scoreboard.reload")) {
                    sender.sendMessage(lang.get(sender, "error.no_permission"));
                    return true;
                }

                if (plugin.getScoreboardManager() != null) {
                    plugin.getScoreboardManager().reload();
                    plugin.debug("Scoreboard System reloaded");
                }

                sender.sendMessage(lang.get(sender, "scoreboard.reloaded"));
            }

            default -> showUsage(sender);
        }

        return true;
    }

    private void showUsage(CommandSender sender) {
        sender.sendMessage(lang.get(sender, "scoreboard.usage"));
        if (sender.hasPermission("essentialsc.scoreboard.toggle")) {
            sender.sendMessage(lang.get(sender, "scoreboard.help_toggle"));
        }
        if (sender.hasPermission("essentialsc.scoreboard.reload")) {
            sender.sendMessage(lang.get(sender, "scoreboard.help_reload"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> subs = new java.util.ArrayList<>();
            if (sender.hasPermission("essentialsc.scoreboard.toggle")) {
                subs.add("toggle");
            }
            if (sender.hasPermission("essentialsc.scoreboard.reload")) {
                subs.add("reload");
            }
            return subs.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return super.tabComplete(sender, args);
    }
}