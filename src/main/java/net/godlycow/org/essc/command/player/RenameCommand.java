package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenameCommand extends Command {
    private final MiniMessage miniMessage;
    private List<String> blacklist;
    private boolean blacklistEnabled;

    public RenameCommand(EssentialsC plugin) {
        super(plugin, "rename", "essentialsc.rename", true, 1, "command.usage.rename");
        this.miniMessage = plugin.getMiniMessage();
        loadConfig();
    }

    public void loadConfig() {
        this.blacklistEnabled = plugin.getConfigManager().isRenameBlacklistEnabled();
        this.blacklist = plugin.getConfigManager().getRenameBlacklistWords();
        plugin.debug("Loaded " + blacklist.size() + " blacklisted words for rename");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        String newName = String.join(" ", args);

        if (blacklistEnabled && !player.hasPermission("essentialsc.rename.bypass")) {
            String checkName = newName.toLowerCase();
            for (String word : blacklist) {
                if (checkName.contains(word.toLowerCase())) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("word", word);
                    player.sendMessage(lang.get(player, "rename.blacklisted", placeholders));
                    plugin.debug(player.getName() + " tried to use blacklisted word: " + word);
                    return true;
                }
            }
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(lang.get(player, "rename.no_item"));
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage(lang.get(player, "rename.cannot_rename"));
            return true;
        }

        Component displayName = miniMessage.deserialize(newName);
        meta.displayName(displayName);
        item.setItemMeta(meta);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", newName);
        player.sendMessage(lang.get(player, "rename.success", placeholders));
        plugin.debug(player.getName() + " renamed item to: " + newName);

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}