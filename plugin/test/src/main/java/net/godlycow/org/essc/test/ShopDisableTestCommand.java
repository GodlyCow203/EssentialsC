package net.godlycow.org.essc.test;

import net.godlycow.org.essc.CommandRegistration;
import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.modules.shop.ShopManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;

public class ShopDisableTestCommand implements CommandExecutor {

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

        boolean InconfigEnabled = plugin.getConfigManager().isShopEnabled();

        if (InconfigEnabled) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Shop is <green>enabled <white>in config"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Shop is <red>disabled <white>in config"));
        }

        ShopManager shopManager = plugin.getShopManager();

        if (shopManager == null) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>ShopManager is <red>null"));
        } else {

            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>ShopManager is <green>not null"));
        }

        boolean shopCommandRegistered = CommandRegistration.isOwnedByE("shop");


        if (shopCommandRegistered) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Shop command is <green>registered"));
        } else {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Shop command is <red>not registered"));
        }

        boolean foundShopListener = false;

        for (HandlerList handlerList : HandlerList.getHandlerLists()) {

            for (RegisteredListener listener :  handlerList.getRegisteredListeners()){
                if (listener.getPlugin().equals(plugin)) {
                    String name = listener.getListener().getClass().getSimpleName();
                    if  (name.contains("Shop")) {
                        sender.sendMessage(mini.deserialize(
                                "<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Registered listener <listener>",
                                Placeholder.parsed("listener", name)));
                        foundShopListener = true;
                    }
                }
            }
        }

        if (!foundShopListener) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Did not find any registered Shop listeners"));
        }

        sender.sendMessage(mini.deserialize("<#00A3FF>========================================"));
        if (!InconfigEnabled && shopManager == null && !shopCommandRegistered && !foundShopListener) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Shop is fully <red>disabled"));
        } else if (InconfigEnabled) {
            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Shop is <green>enabled <white>in config"));
        } else {

            sender.sendMessage(mini.deserialize("<dark_gray>[<#FFBF00>Info<dark_gray>] <white>Shop is <yellow>partially active"));
        }
        sender.sendMessage(mini.deserialize("<#00A3FF>========================================"));


        return true;
    }
}
