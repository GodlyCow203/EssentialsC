package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.rtp.RTPManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;


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

    public static void unregisterCommand() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            org.bukkit.command.Command cmd = commandMap.getCommand("rtp");
            if (cmd != null) {
                cmd.unregister(commandMap);

                Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);

                Map<String, org.bukkit.command.Command> knownCommands =
                        (Map<String, org.bukkit.command.Command>) knownCommandsField.get(commandMap);

                knownCommands.remove("rtp");
                knownCommands.remove("essentialsc:rtp");
            }

            EssentialsC.getInstance().getLogger().info("Successfully unregistered /rtp command");
        } catch (Exception e) {
            EssentialsC.getInstance().getLogger().warning("Failed to unregister /rtp command: " + e.getMessage());
            e.printStackTrace();
        }
    }
}