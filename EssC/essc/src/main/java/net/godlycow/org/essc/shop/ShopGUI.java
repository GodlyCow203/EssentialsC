package net.godlycow.org.essc.shop;

import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.util.*;

public class ShopGUI {
    private final EssentialsC plugin;
    private final ShopManager shopManager;
    private final Player player;
    private final MiniMessage mm;
    private double cachedBalance;

    private static final int SLOT_PREV_PAGE        = 45;
    private static final int SLOT_BALANCE_CATEGORY  = 47;
    private static final int SLOT_BACK_BUTTON       = 48;
    private static final int SLOT_PAGE_INDICATOR    = 49;
    private static final int SLOT_NEXT_PAGE         = 53;

    private static final int SLOT_BALANCE_MAIN      = 50;
    private static final int SLOT_CLOSE_BUTTON      = 48;

    public ShopGUI(EssentialsC plugin, ShopManager shopManager, Player player) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.player = player;
        this.mm = MiniMessage.miniMessage();
        this.cachedBalance = 0.0;
    }

    public ShopGUI(EssentialsC plugin, ShopManager shopManager, Player player, double cachedBalance) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.player = player;
        this.mm = MiniMessage.miniMessage();
        this.cachedBalance = cachedBalance;
    }

    public void openMain() {
        String title = plugin.getConfigManager().getShopMainMenuTitle();
        int size = plugin.getConfigManager().getShopMainMenuSize();

        ShopHolder holder = new ShopHolder(ShopHolder.Type.MAIN);
        Inventory inv = Bukkit.createInventory(holder, size, mm.deserialize(title));

        if (plugin.getConfigManager().isShopFillEmptySlots()) {
            fillEmptySlots(inv);
        }

        for (ShopCategory category : shopManager.getCategories().values()) {
            if (!category.isEnabled()) continue;
            if (category.getPermission() != null && !player.hasPermission(category.getPermission())) continue;
            inv.setItem(category.getSlot(), createCategoryIcon(category));
        }

        addBalanceHead(inv, false);
        addCloseButton(inv);

        player.openInventory(inv);
    }

    public void openCategory(ShopCategory category, int page) {
        String title = plugin.getConfigManager().getShopCategoryMenuTitle()
                .replace("<category>", category.getDisplayName());

        ShopHolder holder = new ShopHolder(ShopHolder.Type.CATEGORY, category.getId(), page);
        Inventory inv = Bukkit.createInventory(holder, 54, mm.deserialize(title));

        if (plugin.getConfigManager().isShopFillEmptySlots()) {
            fillEmptySlots(inv);
        }

        String currencySingular = plugin.getConfigManager().getShopCurrencySingular();
        String currencyPlural   = plugin.getConfigManager().getShopCurrencyPlural();

        for (Map.Entry<Integer, ShopItem> entry : category.getPageItems(page).entrySet()) {
            ShopItem item = entry.getValue();
            if (item.getPermission() != null && !player.hasPermission(item.getPermission())) continue;
            inv.setItem(entry.getKey(), item.createDisplayItem(cachedBalance, currencySingular, currencyPlural));
        }

        addNavigation(inv, category, page);
        addBalanceHead(inv, true);
        addBackButton(inv);

        player.openInventory(inv);
    }

    private ItemStack createCategoryIcon(ShopCategory category) {
        ItemStack item = new ItemStack(category.getIcon());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(mm.deserialize(category.getDisplayName()));

        List<Component> lore = new ArrayList<>();
        for (String line : category.getLore()) {
            lore.add(mm.deserialize(line));
        }
        lore.add(mm.deserialize(""));
        lore.add(mm.deserialize("<color:#FFE66D>Click to browse!"));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void addBalanceHead(Inventory inv, boolean isCategory) {
        int slot = isCategory ? SLOT_BALANCE_CATEGORY : SLOT_BALANCE_MAIN;

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        PlayerProfile profile = Bukkit.createPlayerProfile(player.getUniqueId());
        meta.setOwnerProfile(profile);

        String formattedBalance  = String.format("%.2f", cachedBalance);
        String currencySingular  = plugin.getConfigManager().getShopCurrencySingular();
        String currencyPlural    = plugin.getConfigManager().getShopCurrencyPlural();
        String currency          = cachedBalance == 1.0 ? currencySingular : currencyPlural;

        meta.displayName(mm.deserialize("<color:#06FFA5>Your Balance"));

        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<color:#FFE66D>" + formattedBalance + " " + currency));
        lore.add(mm.deserialize(""));
        lore.add(mm.deserialize("<color:#AAAAAA>Click to refresh"));

        meta.lore(lore);
        head.setItemMeta(meta);
        inv.setItem(slot, head);
    }

    private void addNavigation(Inventory inv, ShopCategory category, int currentPage) {
        int maxPage = category.getMaxPage();

        if (currentPage > 1) {
            inv.setItem(SLOT_PREV_PAGE, createNavigationButton("<color:#06FFA5>Previous Page", Material.ARROW));
        }

        inv.setItem(SLOT_PAGE_INDICATOR, createPageIndicator(currentPage, maxPage));

        if (currentPage < maxPage) {
            inv.setItem(SLOT_NEXT_PAGE, createNavigationButton("<color:#06FFA5>Next Page", Material.ARROW));
        }
    }

    private ItemStack createNavigationButton(String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize(name));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPageIndicator(int current, int max) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<color:#FFE66D>Page " + current + "/" + max));
        item.setItemMeta(meta);
        return item;
    }

    private void addBackButton(Inventory inv) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<color:#FF6B6B>Back to Categories"));
        item.setItemMeta(meta);
        inv.setItem(SLOT_BACK_BUTTON, item);
    }

    private void addCloseButton(Inventory inv) {
        if (plugin.getConfigManager().isShopCloseButtonEnabled()) {
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(mm.deserialize("<color:#FF6B6B>Close"));
            item.setItemMeta(meta);
            inv.setItem(SLOT_CLOSE_BUTTON, item);
        }
    }

    private void fillEmptySlots(Inventory inv) {
        String materialName = plugin.getConfigManager().getShopFillMaterial();

        Material fillMaterial;
        try {
            fillMaterial = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            plugin.getLogger().warning("Invalid shop fill-material: " + materialName + ". Falling back to BLACK_STAINED_GLASS_PANE.");
            fillMaterial = Material.BLACK_STAINED_GLASS_PANE;
        }

        if (!fillMaterial.isItem()) {
            plugin.getLogger().warning("Shop fill-material " + fillMaterial.name() + " is not a valid item. Falling back to BLACK_STAINED_GLASS_PANE.");
            fillMaterial = Material.BLACK_STAINED_GLASS_PANE;
        }

        ItemStack filler = new ItemStack(fillMaterial);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            filler.setItemMeta(meta);
        }

        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    public void setCachedBalance(double balance) {
        this.cachedBalance = balance;
    }

    public double getCachedBalance() {
        return cachedBalance;
    }
}