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

public class KitDisableTestCommand implements CommandExecutor {

    MiniMessage mini = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {

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

        boolean configEnabled = plugin.getConfigManager().isKitsEnabled();


        if (configEnabled) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Kits are <green>enabled <white>in config"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Kits are <red>disabled <white>in config"));
        }

        if ( plugin.getKitManager() == null) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>KitManager is <red>null"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>KitManager is <green>not null"));
        }

        if (plugin.getKitGuiManager()  == null) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>KitGuiManager is <red>null"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>KitGuiManager is <green>not null"));
        }

        boolean kitCommandRegistered = CommandRegistration.isOwnedByE("kit");
        if (kitCommandRegistered) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>/kit command is <green>registered"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>/kit command is <red>not registered"));
        }

        boolean kitsCommandRegistered = CommandRegistration.isOwnedByE("kits");
        if (kitsCommandRegistered ) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>/kits command is <green>registered"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>/kits command is <red>not registered"));
        }

        boolean foundKitListener = false;

        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            for (RegisteredListener listener : handlerList.getRegisteredListeners()) {
                if (listener.getPlugin().equals(plugin)) {
                    String name = listener.getListener().getClass().getSimpleName();
                    if (name.contains("Kit")) {
                        sender.sendMessage(mini.deserialize(
                                "<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Registered listener <listener>",
                                Placeholder.parsed("listener", name)));
                        foundKitListener = true;
                    }
                }
            }
        }

        if (!foundKitListener) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Did not find any registered Kit listeners"));
        }

        
        sender.sendMessage(mini.deserialize("<#00A3FF>========================================"));
        if (!configEnabled && plugin.getKitManager() == null && plugin.getKitGuiManager() == null
                && !kitCommandRegistered && !kitsCommandRegistered && !foundKitListener) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Kits are fully <red>disabled"));
        } else if (configEnabled) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Kits are <green>enabled <white>in config"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Kits are <yellow>partially active"));
        }
        sender.sendMessage(mini.deserialize("<#00A3FF>========================================"));

        return true;
    }
}
