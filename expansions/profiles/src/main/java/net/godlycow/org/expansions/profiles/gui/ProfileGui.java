package net.godlycow.org.expansions.profiles.gui;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.expansions.profiles.EssentialsCProfiles;
import net.godlycow.org.expansions.profiles.ProfileData;
import net.godlycow.org.expansions.profiles.messages.MessagesManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProfileGui implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd MMM yyyy");
    private static final String TITLE_MARKER = "";

    private final EssentialsCProfiles plugin;
    private final EssentialsC essc;
    private final MessagesManager msg;

    public ProfileGui(EssentialsCProfiles plugin, EssentialsC essc, MessagesManager msg) {
        this.plugin = plugin;
        this.essc   = essc;
        this.msg    = msg;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    public void open(Player viewer, OfflinePlayer target, boolean staffView) {
        Inventory loading = Bukkit.createInventory(null, 27,
                MM.deserialize(msg.guiTitleRaw("gui.title-loading")));
        fillLoading(loading);
        viewer.openInventory(loading);

        ProfileData.load(target, essc).thenAccept(data ->
                plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
                    if (!viewer.isOnline()) return;
                    viewer.openInventory(build(data));
                })
        );
    }

    private Inventory build(ProfileData data) {
        String titleKey = data.online ? "gui.title-online" : "gui.title-offline";
        String rawTitle = msg.guiTitleRaw(titleKey).replace("<player>", data.name);
        Inventory gui = Bukkit.createInventory(null, 27, MM.deserialize(rawTitle));

        ItemStack glass = emptyGlass();
        for (int s : new int[]{0, 1, 2, 3, 5, 6, 7, 8}) gui.setItem(s, glass);
        gui.setItem(4, buildHead(data));
        gui.setItem(10, buildBalance(data));
        gui.setItem(12, buildPlaytime(data));
        gui.setItem(14, buildHomes(data));
        gui.setItem(16, buildStatus(data));
        gui.setItem(19, buildKills(data));
        gui.setItem(21, buildDeaths(data));
        gui.setItem(22, buildClose());
        gui.setItem(23, buildFirstJoined(data));
        gui.setItem(25, buildLastSeen(data));

        return gui;
    }


    private ItemStack buildHead(ProfileData data) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        meta.setOwningPlayer(Bukkit.getOfflinePlayer(data.name));

        String nameKey = data.online ? "gui.head-name-online" : "gui.head-name-offline";
        meta.displayName(noItalic(MM.deserialize(
                msg.raw(nameKey).replace("<player>", data.name))));

        List<Component> lore = new ArrayList<>();

        if (data.nickname != null && !data.nickname.isEmpty()) {
            lore.add(noItalic(MM.deserialize(
                    msg.raw("gui.head-lore-nick").replace("<nick>", data.nickname))));
        }

        lore.add(noItalic(MM.deserialize(
                msg.raw(data.online ? "gui.head-lore-online" : "gui.head-lore-offline"))));

        if (data.afk) {
            String dur = data.afkDuration != null ? data.afkDuration : "?";
            lore.add(noItalic(MM.deserialize(
                    msg.raw("gui.head-lore-afk").replace("<duration>", dur))));
        }

        meta.lore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack buildBalance(ProfileData data) {
        String formatted = essc.getEconomyManager() != null
                ? essc.getEconomyManager().format(data.balance)
                : data.balance.toPlainString();
        return stat(Material.GOLD_INGOT,
                msg.raw("gui.balance-name"),
                msg.raw("gui.balance-value").replace("<value>", formatted));
    }

    private ItemStack buildPlaytime(ProfileData data) {
        return stat(Material.CLOCK,
                msg.raw("gui.playtime-name"),
                msg.raw("gui.playtime-value").replace("<value>", data.getPlaytime()));
    }

    private ItemStack buildHomes(ProfileData data) {
        String valueRaw;
        Player online = Bukkit.getPlayer(data.name);
        if (online != null && essc.getHomeManager() != null) {
            int max = essc.getHomeManager().getMaxHomes(online);
            String base = msg.raw("gui.homes-value").replace("<value>", String.valueOf(data.homeCount));
            valueRaw = max > 0
                    ? base + msg.raw("gui.homes-max").replace("<max>", String.valueOf(max))
                    : base;
        } else {
            valueRaw = msg.raw("gui.homes-value").replace("<value>", String.valueOf(data.homeCount));
        }
        return stat(Material.RED_BED, msg.raw("gui.homes-name"), valueRaw);
    }

    private ItemStack buildKills(ProfileData data) {
        return stat(Material.IRON_SWORD,
                msg.raw("gui.kills-name"),
                msg.raw("gui.kills-value").replace("<value>", String.valueOf(data.kills)));
    }

    private ItemStack buildDeaths(ProfileData data) {
        return stat(Material.SKELETON_SKULL,
                msg.raw("gui.deaths-name"),
                msg.raw("gui.deaths-value").replace("<value>", String.valueOf(data.deaths)));
    }

    private ItemStack buildFirstJoined(ProfileData data) {
        String dateStr = data.firstJoin > 0 ? DATE_FMT.format(new Date(data.firstJoin)) : "Unknown";
        return stat(Material.PAPER,
                msg.raw("gui.join-date-name"),
                msg.raw("gui.join-date-value").replace("<value>", dateStr));
    }

    private ItemStack buildLastSeen(ProfileData data) {
        String valueRaw;
        if (data.online) {
            valueRaw = msg.raw("gui.last-seen-online");
        } else if (data.lastSeen > 0) {
            valueRaw = msg.raw("gui.last-seen-offline").replace("<value>", formatAgo(data.lastSeen));
        } else {
            valueRaw = msg.raw("gui.last-seen-unknown");
        }
        return stat(Material.COMPASS, msg.raw("gui.last-seen-name"), valueRaw);
    }

    private ItemStack buildStatus(ProfileData data) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(noItalic(MM.deserialize(msg.raw("gui.status-name"))));

        List<Component> lore = new ArrayList<>();
        lore.add(noItalic(MM.deserialize(
                msg.raw(data.afk ? "gui.status-afk" : "gui.status-active"))));
        if (data.flying)   lore.add(noItalic(MM.deserialize(msg.raw("gui.status-flying"))));
        if (data.vanished) lore.add(noItalic(MM.deserialize(msg.raw("gui.status-vanished"))));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildClose() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(noItalic(MM.deserialize(msg.raw("gui.close-name"))));
        meta.lore(List.of());
        item.setItemMeta(meta);
        return item;
    }

    private void fillLoading(Inventory gui) {
        ItemStack g = emptyGlass();
        for (int i = 0; i < 27; i++) gui.setItem(i, g);
    }

    private ItemStack emptyGlass() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack stat(Material mat, String titleRaw, String valueRaw) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(noItalic(MM.deserialize(titleRaw)));
        meta.lore(List.of(noItalic(MM.deserialize(valueRaw))));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = MM.serialize(event.getView().title());
        if (!title.contains(TITLE_MARKER)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (clicked.getType() == Material.BARRIER) player.closeInventory();
    }

    private static Component noItalic(Component c) {
        return c.applyFallbackStyle(Style.style(TextDecoration.ITALIC.withState(false)));
    }

    private String formatAgo(long timestamp) {
        long diff    = System.currentTimeMillis() - timestamp;
        long days    = diff / 86_400_000L;
        long hours   = (diff % 86_400_000L) / 3_600_000L;
        long minutes = (diff % 3_600_000L) / 60_000L;
        if (days > 0)    return days + "d ago";
        if (hours > 0)   return hours + "h ago";
        if (minutes > 0) return minutes + "m ago";
        return "Just now";
    }
}
