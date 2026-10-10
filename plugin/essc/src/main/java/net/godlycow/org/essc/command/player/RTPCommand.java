package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.modules.rtp.RTPManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;


public class RTPCommand extends Command {

    public RTPCommand(EssentialsC plugin) {
        super(plugin, "rtp", "essentialsc.rtp", true, 0, "command.usage.rtp");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (!player.hasPermission("essentialsc.rtp")) {
            player.sendMessage(lang.get(player, "rtp.error.no_permission"));
            return true;
        }

        RTPManager manager = plugin.getRtpManager();

        if (!manager.isEnabled()) {
            player.sendMessage(lang.get(player, "rtp.error.disabled"));
            return true;
        }

        plugin.getRtpGuiManager().openGUI(player);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}