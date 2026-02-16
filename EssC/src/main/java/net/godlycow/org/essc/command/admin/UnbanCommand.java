package net.godlycow.org.essc.command.admin;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.punishment.PunishmentManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnbanCommand extends Command {

    private final PunishmentManager punishmentManager;

    public UnbanCommand(EssentialsC plugin, PunishmentManager punishmentManager) {
        super(plugin, "unban", "essentialsc.unban", false, 1, "command.usage.unban");
        this.punishmentManager = punishmentManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        String targetName = args[0];
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(targetName);

        if (!punishmentManager.isBanned(target.getUniqueId())) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", targetName);
            sender.sendMessage(lang.get(sender, "unban.not_banned", placeholders));
            return true;
        }

        punishmentManager.unbanPlayer(target.getUniqueId());

        Map<String, String> broadcastPlaceholders = new HashMap<>();
        broadcastPlaceholders.put("target", targetName);
        broadcastPlaceholders.put("unbanner", sender.getName());

        plugin.getServer().broadcast(lang.get(sender, "unban.broadcast", broadcastPlaceholders), "essentialsc.ban.notify");

        Map<String, String> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("target", targetName);
        sender.sendMessage(lang.get(sender, "unban.success", senderPlaceholders));

        plugin.debug("Unbanned " + targetName + " by " + sender.getName());
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return punishmentManager.getAllBans().stream()
                    .map(PunishmentManager.BanEntry::name)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}