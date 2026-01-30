package net.godlycow.org.essc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public final class Messages {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private Messages() {}

    public static void send(CommandSender sender, Component component) {
        sender.sendMessage(component);
    }

    public static void send(CommandSender sender, String miniMessageString) {
        sender.sendMessage(parse(miniMessageString));
    }

    public static Component parse(String miniMessageString) {
        return miniMessage.deserialize(miniMessageString);
    }
}