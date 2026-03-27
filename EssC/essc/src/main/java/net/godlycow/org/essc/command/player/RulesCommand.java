package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RulesCommand extends Command {

    public RulesCommand(EssentialsC plugin) {
        super(plugin, "rules", "essentialsc.rules", false, 0, "command.usage.rules");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("essentialsc.rules.reload")) {
                sender.sendMessage(lang.get(sender, "error.no_permission"));
                return true;
            }

            plugin.getRulesManager().reload();
            sender.sendMessage(lang.get(sender, "rules.reloaded"));
            plugin.debug("Rules reloaded by " + sender.getName());
            return true;
        }

        List<Component> rules = plugin.getRulesManager().getRules();

        if (rules.isEmpty()) {
            sender.sendMessage(lang.get(sender, "rules.empty"));
            return true;
        }

        for (Component rule : rules) {
            sender.sendMessage(rule);
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("essentialsc.rules.reload")) {
            return List.of("reload").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}