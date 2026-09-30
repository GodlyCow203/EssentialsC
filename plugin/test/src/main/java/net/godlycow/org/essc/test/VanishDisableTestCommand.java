package net.godlycow.org.essc.test;

import net.godlycow.org.essc.CommandRegistration;
import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;

public class VanishDisableTestCommand implements CommandExecutor {

    MiniMessage mini = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        var essc = Bukkit.getPluginManager().getPlugin("EssentialsC");

        if (essc == null) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#F54927>Error<dark_gray>] <white>EssC not found"));

            return true;
        }

        if (!(essc instanceof EssentialsC plugin)) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#F54927>Error<dark_gray>] <white>Instance not found"));
            return true;
        }


        sender.sendMessage(mini.deserialize("<#00A3FF>========================================"));

        boolean configEnabled = plugin.getConfigManager().isVanishEnabled();

        if (configEnabled) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Vanish is <green>enabled <white>in config"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Vanish is <red>disabled <white>in config"));
        }

        if (plugin.getVanishManager() == null) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>VanishManager is <red>null"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>VanishManager is <green>not null"));
        }

        boolean vanishCommandRegistered = CommandRegistration.isOwnedByE("vanish");
        if (vanishCommandRegistered) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>/vanish command is <green>registered"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>/vanish command is <red>not registered"));
        }


        boolean foundVanishListener = false;

        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            for (RegisteredListener listener : handlerList.getRegisteredListeners()) {
                if (listener.getPlugin().equals(plugin)) {
                    String name = listener.getListener().getClass().getSimpleName();
                    if (name.contains("Vanish") || name.contains("ServerListPing")) {
                        sender.sendMessage(mini.deserialize(
                                "<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Registered listener <listener>",
                                Placeholder.parsed("listener", name)));

                        foundVanishListener = true;
                    }
                }
            }
        }


        if (!foundVanishListener) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Did not find any registered Vanish listeners"));
        }

        sender.sendMessage(mini.deserialize("<#00A3FF>========================================"));
        if (!configEnabled && plugin.getVanishManager() == null
                && !vanishCommandRegistered && !foundVanishListener) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Vanish is fully <red>disabled"));

        } else if (configEnabled) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Vanish is <green>enabled <white>in config"));

        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Vanish is <yellow>partially active"));
        }
        
        sender.sendMessage(mini.deserialize("<#00A3FF>========================================"));

        return true;
    }
}
