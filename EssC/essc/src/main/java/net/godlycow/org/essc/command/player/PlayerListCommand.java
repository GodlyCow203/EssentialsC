package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.util.LegacyColorConverter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;

public class PlayerListCommand extends Command {

    public PlayerListCommand(EssentialsC plugin) {
        super(plugin, "playerlist", "essentialsc.playerlist", false, 0, "command.usage.playerlist");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int total = onlinePlayers.size();
        int max = Bukkit.getMaxPlayers();

        Map<String, String> headerPlaceholders = new HashMap<>();
        headerPlaceholders.put("online", String.valueOf(total));
        headerPlaceholders.put("max", String.valueOf(max));
        sender.sendMessage(lang.get(sender, "playerlist.header", headerPlaceholders));

        if (total == 0) {
            sender.sendMessage(lang.get(sender, "playerlist.empty"));
            sender.sendMessage(lang.get(sender, "playerlist.footer", headerPlaceholders));
            return true;
        }

        boolean useLuckPerms = plugin.getConfigManager().isPlayerListLuckPermsEnabled()
                && plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null;

        LuckPerms luckPerms = null;
        if (useLuckPerms) {
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) luckPerms = provider.getProvider();
        }

        List<String> playerDisplays = new ArrayList<>();
        for (Player player : onlinePlayers) {
            String display;
            if (useLuckPerms && luckPerms != null) {
                User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                if (user != null) {
                    String prefix = user.getCachedData().getMetaData().getPrefix();
                    String suffix = user.getCachedData().getMetaData().getSuffix();

                    StringBuilder sb = new StringBuilder();
                    if (prefix != null) sb.append(LegacyColorConverter.toMiniMessage(prefix));
                    sb.append(player.getName());
                    if (suffix != null) sb.append(LegacyColorConverter.toMiniMessage(suffix));
                    display = sb.toString();
                } else {
                    display = player.getName();
                }
            } else {
                display = player.getName();
            }
            playerDisplays.add(display);
        }

        playerDisplays.sort(String.CASE_INSENSITIVE_ORDER);

        int perLine = plugin.getConfigManager().getPlayerListPerLine();
        StringBuilder line = new StringBuilder();
        int count = 0;

        for (String displayName : playerDisplays) {
            if (count > 0) line.append("<gray>, </gray>");
            line.append(displayName);
            count++;

            if (count >= perLine) {
                sender.sendMessage(plugin.getMiniMessage().deserialize(line.toString()));
                line = new StringBuilder();
                count = 0;
            }
        }

        if (count > 0) {
            sender.sendMessage(plugin.getMiniMessage().deserialize(line.toString()));
        }

        sender.sendMessage(lang.get(sender, "playerlist.footer", headerPlaceholders));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}