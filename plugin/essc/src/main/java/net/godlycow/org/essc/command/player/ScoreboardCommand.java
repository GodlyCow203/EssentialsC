package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ScoreboardCommand extends Command {

    public ScoreboardCommand(EssentialsC plugin) {
        super(plugin, "scoreboard", null, false, 0);
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

                if (!canToggle(sender)) {
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

            case "status" -> {
                if (plugin.getScoreboardManager() == null) {
                    sender.sendMessage(lang.get(sender, "scoreboard.disabled"));
                    return true;
                }

                if (plugin.getScoreboardManager().isPlaceholderApiEnabled()) {
                    sender.sendMessage(lang.get(sender, "scoreboard.placeholderapi.enabled"));
                } else {
                    sender.sendMessage(lang.get(sender, "scoreboard.placeholderapi.disabled"));
                }
            }

            default -> showUsage(sender);
        }

        return true;
    }

    private void showUsage(CommandSender sender) {
        sender.sendMessage(lang.get(sender, "scoreboard.usage"));
        if (canToggle(sender)) {
            sender.sendMessage(lang.get(sender, "scoreboard.help_toggle"));
        }
        if (sender.hasPermission("essentialsc.scoreboard.reload")) {
            sender.sendMessage(lang.get(sender, "scoreboard.help_reload"));
        }
        sender.sendMessage(lang.get(sender, "scoreboard.help_status"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> subs = new java.util.ArrayList<>();
            if (canToggle(sender)) {
                subs.add("toggle");
            }
            if (sender.hasPermission("essentialsc.scoreboard.reload")) {
                subs.add("reload");
            }
            subs.add("status");
            return subs.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return super.tabComplete(sender, args);
    }

    private boolean canToggle(CommandSender sender) {
        return sender.hasPermission("essentialsc.scoreboard.toggle");
    }
}