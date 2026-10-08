package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.modules.FreezeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FreezeCommand extends Command {

    public FreezeCommand(EssentialsC plugin) {
        super(plugin, "freeze", "essentialsc.freeze", false, 1, "command.usage.freeze");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        FreezeManager freeze = plugin.getFreezeManager();
        if (freeze == null) {
            sender.sendMessage(lang.get(sender, "error.internal"));

            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(lang.get(sender, "error.player_not_found", Map.of("player", args[0])));
            return true;
        }


        if (freeze.isExempt(target) && !sender.hasPermission("essentialsc.freeze.bypass.override")) {
            sender.sendMessage(lang.get(sender, "freeze.exempt", Map.of("player", target.getName())));
            return true;
        }

        if (freeze.isFrozen(target)) {
            sender.sendMessage(lang.get(sender, "freeze.already_frozen", Map.of("player", target.getName())));
            return true;
        }

        freeze.freeze(target);
        plugin.debug("Froze " + target.getName() + " by " + sender.getName());

        sender.sendMessage(lang.get(sender, "freeze.enabled", Map.of("player", target.getName())));
        target.sendMessage(lang.get(target, "freeze.frozen",
                Map.of("player", sender.getName())));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("essentialsc.freeze")) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}
