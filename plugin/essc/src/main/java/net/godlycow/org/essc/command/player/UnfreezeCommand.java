package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.modules.FreezeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UnfreezeCommand extends Command {

    public UnfreezeCommand(EssentialsC plugin) {
        super(plugin, "unfreeze", "essentialsc.unfreeze", false, 1, "command.usage.unfreeze");
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

        if (!freeze.isFrozen(target)) {
            sender.sendMessage(lang.get(sender, "freeze.not_frozen", Map.of("player", target.getName())));
            return true;
        }

        freeze.unfreeze(target);
        plugin.debug("Unfroze " + target.getName() + " by " + sender.getName());

        sender.sendMessage(lang.get(sender, "freeze.disabled", Map.of("player", target.getName())));
        target.sendMessage(lang.get(target, "freeze.unfrozen",
                Map.of("player", sender.getName())));
        return true;
    }


    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("essentialsc.unfreeze")) {
            FreezeManager freeze = plugin.getFreezeManager();
            return plugin.getServer().getOnlinePlayers().stream()
                    .filter(p -> freeze == null || freeze.isFrozen(p))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}
