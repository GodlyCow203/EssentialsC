package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class DBackCommand extends Command {

    public DBackCommand(EssentialsC plugin) {
        super(plugin, "dback", "essentialsc.dback", true, 0);
    }

    @Override
    protected boolean chargesOnSuccess() {
        return true;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        boolean confirm = args.length > 0 && args[0].equalsIgnoreCase("confirm");

        plugin.debug("DBack command executed by " + player.getName() + (confirm ? " (confirm)" : ""));
        plugin.getBackManager().teleportDeathBack(player, confirm, () -> chargeCost(player));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("confirm");
        }
        return Collections.emptyList();
    }
}