package net.godlycow.org.essc.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.godlycow.org.essc.EssentialsC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderHook extends PlaceholderExpansion implements Listener {

    private final EssentialsC plugin;
    private final VanishPlaceholders vanishPlaceholders;
    private final HomePlaceholders homePlaceholders;
    private final ShopPlaceholders shopPlaceholders;

    public PlaceholderHook(EssentialsC plugin) {
        this.plugin = plugin;
        this.vanishPlaceholders = new VanishPlaceholders(plugin);
        this.homePlaceholders = new HomePlaceholders(plugin);
        this.shopPlaceholders = new ShopPlaceholders(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "essc";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return plugin.getDescription().getAuthors().isEmpty()
                ? "_GodlyCow"
                : String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    @Nullable
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        String vanishResult = vanishPlaceholders.onRequest(player, identifier);
        if (vanishResult != null) {
            return vanishResult;
        }

        String homeResult = homePlaceholders.onRequest(player, identifier);
        if (homeResult != null) {
            return homeResult;
        }

        String shopResult = shopPlaceholders.onRequest(player, identifier);
        if (shopResult != null) {
            return shopResult;
        }

        return null;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        homePlaceholders.clearCache(event.getPlayer().getUniqueId());
        shopPlaceholders.clearCache(event.getPlayer().getUniqueId());
    }

    public static List<String> getAllPlaceholders() {
        List<String> placeholders = new ArrayList<>();

        placeholders.addAll(VanishPlaceholders.getPlaceholderList());
        placeholders.addAll(HomePlaceholders.getPlaceholderList());
        placeholders.addAll(ShopPlaceholders.getPlaceholderList());

        return placeholders;
    }
}