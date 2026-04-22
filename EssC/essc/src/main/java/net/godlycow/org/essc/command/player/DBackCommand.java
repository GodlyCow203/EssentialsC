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
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        plugin.debug("DBack command executed by " + player.getName());
        plugin.getBackManager().teleportDeathBack(player);

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}