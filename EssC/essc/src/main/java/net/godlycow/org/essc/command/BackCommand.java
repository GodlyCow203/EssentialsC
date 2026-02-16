package net.godlycow.org.essc.command;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class BackCommand extends Command {

    public BackCommand(EssentialsC plugin) {
        super(plugin, "back", "essentialsc.back", true, 0);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        plugin.debug("Back command executed by " + player.getName());
        plugin.getBackManager().teleportBack(player);

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}