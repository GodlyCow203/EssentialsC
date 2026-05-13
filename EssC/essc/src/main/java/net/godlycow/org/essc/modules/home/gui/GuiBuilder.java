package net.godlycow.org.essc.modules.home.gui;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.modules.home.Home;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GuiBuilder {

    static final int[] HOME_SLOTS = {10, 12, 14, 16, 19, 21, 23, 25};
    static final int HOMES_PER_PAGE = 8;

    private final EssentialsC plugin;
    private final HomeItems items;

    public GuiBuilder(EssentialsC plugin, HomeItems items) {
        this.plugin = plugin;
        this.items = items;
    }

    public Inventory buildHomeList(Player player, List<Home> homes, int page,
                                   int totalPages, GuiManager.SortMode sort) {
        int maxHomes = plugin.getHomeManager().getMaxHomes(player);
        int used = homes.size();
        String limitStr = maxHomes == Integer.MAX_VALUE ? "∞" : String.valueOf(maxHomes);

        Component title = lang(player, "home.gui.title", Map.of(
                "used", String.valueOf(used),
                "limit", limitStr,
                "page", String.valueOf(page + 1),
                "total", String.valueOf(totalPages)));

        Inventory gui = Bukkit.createInventory(
                new GuiManager.HomeHolder(null, GuiManager.GuiMode.LIST, null,
                        player.getUniqueId(), GuiManager.GuiViewMode.OWN_HOMES),
                54, title);

        fill(gui, player);

        int start = page * HOMES_PER_PAGE;
        int end = Math.min(start + HOMES_PER_PAGE, homes.size());

        for (int i = start; i < end; i++) {
            gui.setItem(HOME_SLOTS[i - start], items.homeDisplay(player, homes.get(i)));
        }

        gui.setItem(45, page > 0 ? items.prevPageButton(player, page) : items.placeholder(player, Material.GRAY_STAINED_GLASS_PANE));
        gui.setItem(46, items.sortButton(player, sort));
        gui.setItem(47, items.refreshButton(player));
        gui.setItem(48, items.searchButton(player));
        gui.setItem(49, player.hasPermission("essentialsc.home.admin")
                ? items.playerManagementButton(player)
                : items.placeholder(player, Material.GRAY_STAINED_GLASS_PANE));
        gui.setItem(50, items.createButton(player));
        gui.setItem(51, items.infoPanel(player, used, maxHomes, page + 1, totalPages));
        gui.setItem(52, items.placeholder(player, Material.GRAY_STAINED_GLASS_PANE));
        gui.setItem(53, page < totalPages - 1 ? items.nextPageButton(player, page) : items.closeButton(player));

        return gui;
    }

    public Inventory buildPlayerManagement(Player player) {
        Component title = lang(player, "home.gui.player_management_title");

        Inventory gui = Bukkit.createInventory(
                new GuiManager.HomeHolder(null, GuiManager.GuiMode.PLAYER_TYPE_SELECT,
                        null, null, GuiManager.GuiViewMode.ADMIN),
                54, title);

        fill(gui, player);

        gui.setItem(11, items.onlinePlayersButton(player));
        gui.setItem(13, items.offlinePlayersButton(player));
        gui.setItem(15, items.allPlayersButton(player));
        gui.setItem(29, items.searchPlayerButton(player));
        gui.setItem(31, items.backButton(player));
        gui.setItem(33, items.closeButton(player));

        return gui;
    }

    public Inventory buildPlayerList(Player player, List<OfflinePlayer> players,
                                     int page, int totalPages,
                                     GuiManager.PlayerListType type) {
        String typeStr = type.toString().charAt(0) + type.toString().substring(1).toLowerCase();
        Component title = lang(player, "home.gui.player_list_title", Map.of(
                "type", typeStr,
                "page", String.valueOf(page + 1),
                "total", String.valueOf(totalPages)));

        Inventory gui = Bukkit.createInventory(
                new GuiManager.HomeHolder(null, GuiManager.GuiMode.PLAYER_LIST,
                        null, null, GuiManager.GuiViewMode.ADMIN),
                54, title);

        fill(gui, player);

        int start = page * HOMES_PER_PAGE;
        int end = Math.min(start + HOMES_PER_PAGE, players.size());

        for (int i = start; i < end; i++) {
            gui.setItem(HOME_SLOTS[i - start], items.playerEntry(player, players.get(i)));
        }

        gui.setItem(45, page > 0 ? items.prevPageButton(player, page) : items.placeholder(player, Material.GRAY_STAINED_GLASS_PANE));
        gui.setItem(47, items.refreshButton(player));
        gui.setItem(48, items.searchPlayerButton(player));
        gui.setItem(49, items.playerManagementButton(player));
        gui.setItem(50, items.backButton(player));
        gui.setItem(53, page < totalPages - 1 ? items.nextPageButton(player, page) : items.closeButton(player));

        return gui;
    }

    public Inventory buildPlayerHomes(Player player, java.util.UUID targetUuid,
                                      String targetName, List<Home> homes) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetUuid);
        int maxHomes = offlinePlayer.isOnline()
                ? plugin.getHomeManager().getMaxHomes(offlinePlayer.getPlayer())
                : plugin.getConfigManager().getMaxHomes();

        String limitStr = maxHomes == Integer.MAX_VALUE ? "∞" : String.valueOf(maxHomes);
        Component title = lang(player, "home.gui.admin.player_title", Map.of(
                "player", targetName,
                "used", String.valueOf(homes.size()),
                "limit", limitStr));

        Inventory gui = Bukkit.createInventory(
                new GuiManager.HomeHolder(null, GuiManager.GuiMode.ADMIN_PLAYER,
                        null, targetUuid, GuiManager.GuiViewMode.ADMIN),
                54, title);

        fill(gui, player);

        for (int i = 0; i < Math.min(homes.size(), HOME_SLOTS.length); i++) {
            gui.setItem(HOME_SLOTS[i], items.adminHomeDisplay(player, homes.get(i), targetName));
        }

        gui.setItem(4, items.adminInfoPanel(player, targetName, homes.size(), maxHomes));
        gui.setItem(45, items.backButton(player));
        gui.setItem(53, items.closeButton(player));

        return gui;
    }

    public Inventory buildHomeDetails(Player player, Home home, java.util.UUID targetUuid) {
        boolean isAdmin = !targetUuid.equals(player.getUniqueId());
        String key = isAdmin ? "home.gui.admin.details_title" : "home.gui.details.title";
        Component title = lang(player, key, Map.of("name", home.getName()));

        Inventory gui = Bukkit.createInventory(
                new GuiManager.HomeHolder(null, GuiManager.GuiMode.DETAILS,
                        home.getName(), targetUuid,
                        isAdmin ? GuiManager.GuiViewMode.ADMIN : GuiManager.GuiViewMode.OWN_HOMES),
                54, title);

        fill(gui, player);

        String world = home.getWorld();
        gui.setItem(13, items.homeBigDisplay(player, home));
        gui.setItem(29, items.teleportButton(player, home.getName(), targetUuid));
        gui.setItem(31, items.renameButton(player, home.getName(), targetUuid));
        gui.setItem(33, items.updateButton(player, home.getName(), targetUuid));

        if (isAdmin) {
            gui.setItem(38, items.deleteButton(player, home.getName(), targetUuid));
            gui.setItem(42, items.backToPlayerButton(player, targetUuid));
        } else {
            gui.setItem(40, items.deleteButton(player, home.getName(), targetUuid));
        }

        gui.setItem(49, items.coordinatesPanel(player, home, world));

        return gui;
    }

    public Inventory buildCreate(Player player) {
        Component title = lang(player, "home.gui.create_title");
        Inventory gui = Bukkit.createInventory(
                new GuiManager.HomeHolder(null, GuiManager.GuiMode.CREATE,
                        null, player.getUniqueId(), GuiManager.GuiViewMode.OWN_HOMES),
                54, title);

        fill(gui, player);

        gui.setItem(20, items.namedCreateButton(player));
        gui.setItem(24, items.defaultCreateButton(player, plugin.getConfigManager().getDefaultHomeName()));
        gui.setItem(45, items.backButton(player));
        gui.setItem(53, items.closeButton(player));

        return gui;
    }

    public Inventory buildConfirmDelete(Player player, Home home, java.util.UUID targetUuid) {
        Component title = lang(player, "home.gui.confirm_delete_title", Map.of("name", home.getName()));
        boolean own = targetUuid.equals(player.getUniqueId());
        Inventory gui = Bukkit.createInventory(
                new GuiManager.HomeHolder(null, GuiManager.GuiMode.CONFIRM_DELETE,
                        home.getName(), targetUuid,
                        own ? GuiManager.GuiViewMode.OWN_HOMES : GuiManager.GuiViewMode.ADMIN),
                54, title);

        fill(gui, player);
        gui.setItem(13, items.confirmIcon(player, home, "delete"));
        gui.setItem(29, items.confirmYesButton(player, "delete", home.getName(), targetUuid));
        gui.setItem(33, items.confirmNoButton(player, home.getName(), targetUuid));

        return gui;
    }

    public Inventory buildConfirmUpdate(Player player, Home home, java.util.UUID targetUuid) {
        Component title = lang(player, "home.gui.confirm_update_title", Map.of("name", home.getName()));
        boolean own = targetUuid.equals(player.getUniqueId());
        Inventory gui = Bukkit.createInventory(
                new GuiManager.HomeHolder(null, GuiManager.GuiMode.CONFIRM_UPDATE,
                        home.getName(), targetUuid,
                        own ? GuiManager.GuiViewMode.OWN_HOMES : GuiManager.GuiViewMode.ADMIN),
                54, title);

        fill(gui, player);
        gui.setItem(13, items.confirmIcon(player, home, "update"));
        gui.setItem(29, items.confirmYesButton(player, "update", home.getName(), targetUuid));
        gui.setItem(33, items.confirmNoButton(player, home.getName(), targetUuid));

        return gui;
    }

    public List<Home> sortHomes(List<Home> homes, GuiManager.SortMode sort) {
        List<Home> sorted = new java.util.ArrayList<>(homes);
        switch (sort) {
            case ALPHABETICAL -> sorted.sort(Comparator.comparing(Home::getName));
            case DATE_CREATED -> sorted.sort(Comparator.comparingLong(Home::getCreatedAt).reversed());
            case WORLD        -> sorted.sort(Comparator.comparing(Home::getWorld).thenComparing(Home::getName));
        }
        return sorted;
    }


    private void fill(Inventory gui, Player player) {
        for (int i = 0; i < 9; i++) {
            if (isEmpty(gui, i)) gui.setItem(i, items.borderItem(player));
        }
        if (isEmpty(gui, 9))  gui.setItem(9,  items.borderItem(player));
        if (isEmpty(gui, 17)) gui.setItem(17, items.borderItem(player));
        if (isEmpty(gui, 18)) gui.setItem(18, items.borderItem(player));
        if (isEmpty(gui, 26)) gui.setItem(26, items.borderItem(player));
        for (int i = 27; i < 36; i++) {
            if (isEmpty(gui, i)) gui.setItem(i, items.borderItem(player));
        }
        if (isEmpty(gui, 36)) gui.setItem(36, items.borderItem(player));
        if (isEmpty(gui, 44)) gui.setItem(44, items.borderItem(player));
        for (int i = 0; i < gui.getSize(); i++) {
            if (isEmpty(gui, i)) gui.setItem(i, items.background(player));
        }
    }

    private boolean isEmpty(Inventory gui, int slot) {
        return gui.getItem(slot) == null || gui.getItem(slot).getType() == Material.AIR;
    }

    private Component lang(Player player, String key) {
        return plugin.getLanguageManager().get(player, key);
    }

    private Component lang(Player player, String key, Map<String, String> placeholders) {
        return plugin.getLanguageManager().get(player, key, placeholders);
    }
}