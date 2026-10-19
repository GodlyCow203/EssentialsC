package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.modules.rtp.RTPManager;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class RTPCommand extends Command {

    public RTPCommand(EssentialsC plugin) {
        super(plugin, "rtp", "essentialsc.rtp", true, 0, "command.usage.rtp");
    }

    @Override
    protected boolean chargesOnSuccess() {
        return true;
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

        if (args.length == 0) {
            plugin.getRtpGuiManager().openGUI(player, () -> chargeCost(player));
            return true;
        }

        String worldName = manager.findConfiguredWorld(args[0]);
        World world = worldName == null ? null : manager.resolveWorld(worldName);
        if (world == null) {
            player.sendMessage(lang.get(player, "rtp.error.world_not_found"));
            return true;
        }

        manager.startRTP(player, world, () -> chargeCost(player));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length != 1 || !(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        RTPManager manager = plugin.getRtpManager();
        String input = args[0].toLowerCase();
        return manager.getConfiguredWorldNames().stream()
                .filter(name -> manager.isWorldEnabled(name) && manager.hasWorldPermission(player, name))
                .map(manager::getWorldAlias)
                .filter(alias -> alias.startsWith(input))
                .collect(Collectors.toList());
    }

}