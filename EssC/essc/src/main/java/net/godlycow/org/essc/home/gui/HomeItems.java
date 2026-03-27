package net.godlycow.org.essc.home.gui;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.home.Home;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeItems {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final NamespacedKey keyAction;
    private final EssentialsC plugin;

    public HomeItems(EssentialsC plugin) {
        this.plugin = plugin;
        this.keyAction = new NamespacedKey(plugin, "hgui_action");
    }

    public ItemStack background(Player player) {
        return create(player, Material.BLACK_STAINED_GLASS_PANE, lang(player, "home.gui.items.background"), List.of());
    }

    public ItemStack borderItem(Player player) {
        return create(player, Material.GRAY_STAINED_GLASS_PANE, lang(player, "home.gui.items.border"), List.of());
    }

    public ItemStack placeholder(Player player, Material material) {
        return create(player, material, Component.empty(), List.of());
    }

    public ItemStack homeDisplay(Player player, Home home) {
        Component name = lang(player, "home.gui.items.home.name", Map.of("name", home.getName()));
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.home.lore.world", Map.of("world", home.getWorld())),
                lang(player, "home.gui.items.home.lore.coords", Map.of(
                        "x", formatCoord(home.getX()),
                        "y", formatCoord(home.getY()),
                        "z", formatCoord(home.getZ()))),
                Component.empty(),
                lang(player, "home.gui.items.home.lore.click")
        );

        ItemStack item = create(player, getWorldMaterial(home.getWorld()), name, lore);
        tag(item, "home:" + home.getName());
        return item;
    }

    public ItemStack homeBigDisplay(Player player, Home home) {
        Component name = lang(player, "home.gui.items.home_big.name", Map.of("name", home.getName()));
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.home_big.lore.created", Map.of("date", formatDate(home.getCreatedAt()))),
                lang(player, "home.gui.items.home_big.lore.world", Map.of("world", home.getWorld())),
                lang(player, "home.gui.items.home_big.lore.location", Map.of(
                        "x", formatCoord(home.getX()),
                        "y", formatCoord(home.getY()),
                        "z", formatCoord(home.getZ()))),
                Component.empty(),
                lang(player, "home.gui.items.home_big.lore.footer")
        );

        ItemStack item = create(player, getWorldMaterial(home.getWorld()), name, lore);
        tag(item, "home:" + home.getName());
        return item;
    }

    public ItemStack closeButton(Player player) {
        Component name = lang(player, "home.gui.items.close.name");
        List<Component> lore = List.of(Component.empty(), lang(player, "home.gui.items.close.lore"));
        ItemStack item = create(player, Material.BARRIER, name, lore);
        tag(item, "action:close");
        return item;
    }

    public ItemStack backButton(Player player) {
        Component name = lang(player, "home.gui.items.back.name");
        List<Component> lore = List.of(Component.empty(), lang(player, "home.gui.items.back.lore"));
        ItemStack item = create(player, Material.ARROW, name, lore);
        tag(item, "action:back");
        return item;
    }

    public ItemStack backToPlayerButton(Player player, UUID targetUuid) {
        Component name = lang(player, "home.gui.items.back_player.name");
        List<Component> lore = List.of(Component.empty(), lang(player, "home.gui.items.back_player.lore"));
        ItemStack item = create(player, Material.ARROW, name, lore);
        tag(item, "action:back_player:" + targetUuid);
        return item;
    }

    public ItemStack prevPageButton(Player player, int current) {
        Component name = lang(player, "home.gui.items.prev.name");
        List<Component> lore = List.of(Component.empty(), lang(player, "home.gui.items.prev.lore", Map.of("page", String.valueOf(current))));
        ItemStack item = create(player, Material.ARROW, name, lore);
        tag(item, "action:page:" + (current - 1));
        return item;
    }

    public ItemStack nextPageButton(Player player, int current) {
        Component name = lang(player, "home.gui.items.next.name");
        List<Component> lore = List.of(Component.empty(), lang(player, "home.gui.items.next.lore", Map.of("page", String.valueOf(current + 2))));
        ItemStack item = create(player, Material.SPECTRAL_ARROW, name, lore);
        tag(item, "action:page:" + (current + 1));
        return item;
    }

    public ItemStack sortButton(Player player, GuiManager.SortMode mode) {
        String modeStr = mode.toString().replace("_", " ");
        Component name = lang(player, "home.gui.items.sort.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.sort.lore.current", Map.of("mode", modeStr)),
                Component.empty(),
                lang(player, "home.gui.items.sort.lore.cycle"),
                lang(player, "home.gui.items.sort.lore.alpha"),
                lang(player, "home.gui.items.sort.lore.date"),
                lang(player, "home.gui.items.sort.lore.world")
        );
        ItemStack item = create(player, Material.COMPARATOR, name, lore);
        tag(item, "action:sort:" + mode.name());
        return item;
    }

    public ItemStack refreshButton(Player player) {
        Component name = lang(player, "home.gui.items.refresh.name");
        List<Component> lore = List.of(Component.empty(), lang(player, "home.gui.items.refresh.lore"));
        ItemStack item = create(player, Material.WHITE_CANDLE, name, lore);
        tag(item, "action:refresh");
        return item;
    }

    public ItemStack searchButton(Player player) {
        Component name = lang(player, "home.gui.items.search.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.search.lore"),
                Component.empty(),
                lang(player, "home.gui.items.search.footer")
        );
        ItemStack item = create(player, Material.SPYGLASS, name, lore);
        tag(item, "action:search");
        return item;
    }

    public ItemStack createButton(Player player) {
        Component name = lang(player, "home.gui.items.create.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.create.lore1"),
                lang(player, "home.gui.items.create.lore2"),
                Component.empty(),
                lang(player, "home.gui.items.create.footer")
        );
        ItemStack item = create(player, Material.EMERALD, name, lore);
        tag(item, "action:create");
        return item;
    }

    public ItemStack playerManagementButton(Player player) {
        Component name = lang(player, "home.gui.items.manage.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.manage.lore1"),
                lang(player, "home.gui.items.manage.lore2"),
                Component.empty(),
                lang(player, "home.gui.items.manage.footer")
        );
        ItemStack item = create(player, Material.NETHER_STAR, name, lore);
        tag(item, "action:manage_players");
        return item;
    }

    public ItemStack namedCreateButton(Player player) {
        Component name = lang(player, "home.gui.items.create_named.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.create_named.lore"),
                Component.empty(),
                lang(player, "home.gui.items.create_named.footer")
        );
        ItemStack item = create(player, Material.NAME_TAG, name, lore);
        tag(item, "action:create_named");
        return item;
    }

    public ItemStack defaultCreateButton(Player player, String defaultName) {
        Component name = lang(player, "home.gui.items.create_default.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.create_default.lore1"),
                lang(player, "home.gui.items.create_default.lore2", Map.of("name", defaultName)),
                Component.empty(),
                lang(player, "home.gui.items.create_default.footer")
        );
        ItemStack item = create(player, Material.OAK_SAPLING, name, lore);
        tag(item, "action:create_default");
        return item;
    }

    public ItemStack teleportButton(Player player, String homeName, UUID targetUuid) {
        Component name = lang(player, "home.gui.items.teleport.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.teleport.lore1", Map.of("name", homeName)),
                lang(player, "home.gui.items.teleport.lore2"),
                Component.empty(),
                lang(player, "home.gui.items.teleport.footer")
        );
        ItemStack item = create(player, Material.ENDER_PEARL, name, lore);
        tag(item, "action:teleport:" + homeName + ":" + targetUuid);
        return item;
    }

    public ItemStack renameButton(Player player, String homeName, UUID targetUuid) {
        Component name = lang(player, "home.gui.items.rename.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.rename.lore1", Map.of("name", homeName)),
                lang(player, "home.gui.items.rename.lore2"),
                Component.empty(),
                lang(player, "home.gui.items.rename.footer")
        );
        ItemStack item = create(player, Material.NAME_TAG, name, lore);
        tag(item, "action:rename:" + homeName + ":" + targetUuid);
        return item;
    }

    public ItemStack updateButton(Player player, String homeName, UUID targetUuid) {
        Component name = lang(player, "home.gui.items.update.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.update.lore1", Map.of("name", homeName)),
                lang(player, "home.gui.items.update.lore2"),
                Component.empty(),
                lang(player, "home.gui.items.update.footer")
        );
        ItemStack item = create(player, Material.COMPASS, name, lore);
        tag(item, "action:update:" + homeName + ":" + targetUuid);
        return item;
    }

    public ItemStack deleteButton(Player player, String homeName, UUID targetUuid) {
        Component name = lang(player, "home.gui.items.delete.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.delete.lore1", Map.of("name", homeName)),
                Component.empty(),
                lang(player, "home.gui.items.delete.warning")
        );
        ItemStack item = create(player, Material.RED_TERRACOTTA, name, lore);
        tag(item, "action:delete:" + homeName + ":" + targetUuid);
        return item;
    }


    public ItemStack infoPanel(Player player, int used, int max, int page, int total) {
        String maxStr = max == Integer.MAX_VALUE ? "∞" : String.valueOf(max);
        Material mat = (max != Integer.MAX_VALUE && used >= max) ? Material.RED_CONCRETE : Material.KNOWLEDGE_BOOK;
        Component name = lang(player, "home.gui.items.info.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.info.lore.used", Map.of("used", String.valueOf(used), "max", maxStr)),
                lang(player, "home.gui.items.info.lore.page", Map.of("page", String.valueOf(page), "total", String.valueOf(total))),
                Component.empty(),
                lang(player, "home.gui.items.info.lore.hint")
        );
        return create(player, mat, name, lore);
    }

    public ItemStack coordinatesPanel(Player player, Home home, String worldName) {
        Component name = lang(player, "home.gui.items.coords.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.coords.lore.world", Map.of("world", worldName)),
                lang(player, "home.gui.items.coords.lore.x",     Map.of("x", String.format("%.2f", home.getX()))),
                lang(player, "home.gui.items.coords.lore.y",     Map.of("y", String.format("%.2f", home.getY()))),
                lang(player, "home.gui.items.coords.lore.z",     Map.of("z", String.format("%.2f", home.getZ()))),
                lang(player, "home.gui.items.coords.lore.yaw",   Map.of("yaw", String.format("%.1f", home.getYaw()))),
                lang(player, "home.gui.items.coords.lore.pitch", Map.of("pitch", String.format("%.1f", home.getPitch())))
        );
        return create(player, Material.FILLED_MAP, name, lore);
    }

    public ItemStack confirmIcon(Player player, Home home, String type) {
        boolean isDelete = type.equals("delete");
        Material mat = isDelete ? Material.RED_CONCRETE : Material.YELLOW_CONCRETE;
        String key = isDelete ? "home.gui.items.confirm_delete" : "home.gui.items.confirm_update";

        Component name = lang(player, key + ".name", Map.of("name", home.getName()));
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, key + ".lore1", Map.of("name", home.getName())),
                lang(player, key + ".lore2")
        );
        return create(player, mat, name, lore);
    }

    public ItemStack confirmYesButton(Player player, String type, String homeName, UUID targetUuid) {
        Component name = lang(player, "home.gui.items.yes.name");
        List<Component> lore = List.of(Component.empty(), lang(player, "home.gui.items.yes.lore", Map.of("action", type)));
        ItemStack item = create(player, Material.LIME_CONCRETE, name, lore);
        tag(item, "action:confirm_" + type + ":" + homeName + ":" + targetUuid);
        return item;
    }

    public ItemStack confirmNoButton(Player player, String homeName, UUID targetUuid) {
        Component name = lang(player, "home.gui.items.no.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.no.lore1"),
                lang(player, "home.gui.items.no.lore2"),
                Component.empty(),
                lang(player, "home.gui.items.no.footer")
        );
        ItemStack item = create(player, Material.RED_CONCRETE, name, lore);
        tag(item, "action:back_details:" + homeName + ":" + targetUuid);
        return item;
    }


    public ItemStack onlinePlayersButton(Player player) {
        Component name = lang(player, "home.gui.items.online.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.online.lore1"),
                lang(player, "home.gui.items.online.lore2"),
                Component.empty(),
                lang(player, "home.gui.items.online.footer")
        );
        ItemStack item = create(player, Material.LIME_CONCRETE, name, lore);
        tag(item, "action:player_type:ONLINE");
        return item;
    }

    public ItemStack offlinePlayersButton(Player player) {
        Component name = lang(player, "home.gui.items.offline.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.offline.lore1"),
                lang(player, "home.gui.items.offline.lore2"),
                Component.empty(),
                lang(player, "home.gui.items.offline.footer")
        );
        ItemStack item = create(player, Material.RED_CONCRETE, name, lore);
        tag(item, "action:player_type:OFFLINE");
        return item;
    }

    public ItemStack allPlayersButton(Player player) {
        Component name = lang(player, "home.gui.items.all.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.all.lore1"),
                lang(player, "home.gui.items.all.lore2"),
                Component.empty(),
                lang(player, "home.gui.items.all.footer")
        );
        ItemStack item = create(player, Material.CYAN_CONCRETE, name, lore);
        tag(item, "action:player_type:ALL");
        return item;
    }

    public ItemStack searchPlayerButton(Player player) {
        Component name = lang(player, "home.gui.items.search_player.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.items.search_player.lore"),
                Component.empty(),
                lang(player, "home.gui.items.search_player.footer")
        );
        ItemStack item = create(player, Material.SPYGLASS, name, lore);
        tag(item, "action:search_player");
        return item;
    }

    public ItemStack playerEntry(Player viewer, OfflinePlayer target) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return item;

        meta.setOwningPlayer(target);
        String targetName = target.getName() != null ? target.getName() : "Unknown";
        String status = target.isOnline() ? "<green>● Online" : "<red>● Offline";

        meta.displayName(MM.deserialize("<blue><bold>" + targetName));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(MM.deserialize("<gray>Status: " + status));
        lore.add(MM.deserialize("<gray>UUID: <dark_gray>" + target.getUniqueId().toString().substring(0, 8) + "…"));
        lore.add(Component.empty());
        lore.add(MM.deserialize("<blue>Click to manage homes"));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(keyAction, PersistentDataType.STRING,
                "action:player:" + target.getUniqueId() + ":" + targetName);
        item.setItemMeta(meta);
        return item;
    }


    public ItemStack adminHomeDisplay(Player player, Home home, String targetName) {
        Component name = lang(player, "home.gui.admin.home.name", Map.of("name", home.getName()));
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.admin.home.lore.owner",  Map.of("player", targetName)),
                lang(player, "home.gui.admin.home.lore.world",  Map.of("world", home.getWorld())),
                lang(player, "home.gui.admin.home.lore.coords", Map.of(
                        "x", formatCoord(home.getX()),
                        "y", formatCoord(home.getY()),
                        "z", formatCoord(home.getZ()))),
                Component.empty(),
                lang(player, "home.gui.admin.home.lore.click")
        );

        ItemStack item = create(player, getWorldMaterial(home.getWorld()), name, lore);
        tag(item, "home_admin:" + home.getName());
        return item;
    }

    public ItemStack adminInfoPanel(Player player, String targetName, int used, int max) {
        String maxStr = max == Integer.MAX_VALUE ? "∞" : String.valueOf(max);
        Component name = lang(player, "home.gui.admin.info.name");
        List<Component> lore = List.of(
                Component.empty(),
                lang(player, "home.gui.admin.info.lore.player", Map.of("player", targetName)),
                lang(player, "home.gui.admin.info.lore.used",   Map.of("used", String.valueOf(used), "max", maxStr)),
                Component.empty(),
                lang(player, "home.gui.admin.info.lore.back")
        );
        return create(player, Material.KNOWLEDGE_BOOK, name, lore);
    }


    private ItemStack create(Player player, Material mat, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(name);
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    private void tag(ItemStack item, String action) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(keyAction, PersistentDataType.STRING, action);
        item.setItemMeta(meta);
    }

    private Material getWorldMaterial(String world) {
        String lower = world.toLowerCase();
        if (lower.contains("nether")) return Material.NETHERRACK;
        if (lower.contains("end"))    return Material.END_STONE;
        if (lower.contains("ocean"))  return Material.WATER_BUCKET;
        if (lower.contains("desert")) return Material.SAND;
        if (lower.contains("snow"))   return Material.SNOW_BLOCK;
        return Material.GRASS_BLOCK;
    }

    private String formatCoord(double val) { return String.format("%.1f", val); }

    private String formatDate(long timestamp) {
        long diff = System.currentTimeMillis() / 1000L - timestamp;
        if (diff < 60)    return "Just now";
        if (diff < 3600)  return (diff / 60) + "m ago";
        if (diff < 86400) return (diff / 3600) + "h ago";
        return (diff / 86400) + "d ago";
    }

    private Component lang(Player player, String key) {
        return plugin.getLanguageManager().get(player, key);
    }

    private Component lang(Player player, String key, Map<String, String> placeholders) {
        return plugin.getLanguageManager().get(player, key, placeholders);
    }

    public NamespacedKey getActionKey() { return keyAction; }

    public String getAction(PersistentDataContainer pdc) {
        return pdc.get(keyAction, PersistentDataType.STRING);
    }

    public boolean hasAction(PersistentDataContainer pdc) {
        return pdc.has(keyAction, PersistentDataType.STRING);
    }
}