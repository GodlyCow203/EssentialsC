package net.godlycow.org.essc.shop;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.util.SkullTextureUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.format.TextDecoration;
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
        ShopMainConfig cfg = shopManager.getMainConfig();

        ShopHolder holder = new ShopHolder(ShopHolder.Type.MAIN);
        Inventory inv = Bukkit.createInventory(holder, cfg.getSize(), mm.deserialize(cfg.getTitle()));

        if (cfg.isFillEmpty()) {
            fillEmptySlots(inv, cfg.getFillMaterial());
        }

        for (ShopCategory category : shopManager.getCategories().values()) {
            if (!category.isEnabled()) continue;
            if (category.getPermission() != null && !player.hasPermission(category.getPermission())) continue;
            inv.setItem(category.getSlot(), createCategoryIcon(category));
        }

        addBalanceHead(inv, cfg, false);
        addCloseButton(inv, cfg);

        player.openInventory(inv);
    }

    public void openCategory(ShopCategory category, int page) {
        ShopMainConfig cfg = shopManager.getMainConfig();

        String title = cfg.getCategoryTitle().replace("<category>", category.getDisplayName());

        ShopHolder holder = new ShopHolder(ShopHolder.Type.CATEGORY, category.getId(), page);
        Inventory inv = Bukkit.createInventory(holder, 54, mm.deserialize(title));

        if (cfg.isFillEmpty()) {
            fillEmptySlots(inv, cfg.getFillMaterial());
        }

        String currencySingular = plugin.getConfigManager().getShopCurrencySingular();
        String currencyPlural   = plugin.getConfigManager().getShopCurrencyPlural();

        for (Map.Entry<Integer, ShopItem> entry : category.getPageItems(page).entrySet()) {
            ShopItem item = entry.getValue();
            if (item.getPermission() != null && !player.hasPermission(item.getPermission())) continue;
            inv.setItem(entry.getKey(), item.createDisplayItem(cachedBalance, currencySingular, currencyPlural));
        }

        addNavigation(inv, cfg, category, page);
        addBalanceHead(inv, cfg, true);
        addBackButton(inv, cfg);

        player.openInventory(inv);
    }

    private ItemStack createCategoryIcon(ShopCategory category) {
        ItemStack item = new ItemStack(category.getIcon());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(mm.deserialize(category.getDisplayName()).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        for (String line : category.getLore()) {
            lore.add(mm.deserialize(line).decoration(TextDecoration.ITALIC, false));
        }
        lore.add(mm.deserialize("").decoration(TextDecoration.ITALIC, false));
        lore.add(mm.deserialize("<color:#F5C827>Click to browse!").decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);

        if (category.getIcon() == Material.PLAYER_HEAD && category.getTextureUrl() != null) {
            if (meta instanceof SkullMeta skullMeta) {
                SkullTextureUtil.applyTexture(skullMeta, category.getTextureUrl(), plugin.getLogger());
                item.setItemMeta(skullMeta);
                return item;
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    private void addBalanceHead(Inventory inv, ShopMainConfig cfg, boolean isCategory) {
        ShopMainConfig.ButtonConfig btn = cfg.getBalanceButton();
        int slot = isCategory ? btn.getSlotCategory() : btn.getSlot();

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (btn.getTexture() != null) {
            SkullTextureUtil.applyTexture(meta, btn.getTexture(), plugin.getLogger());
        } else {
            PlayerProfile profile = Bukkit.createPlayerProfile(player.getUniqueId(), player.getName());
            meta.setOwnerProfile(profile);
        }

        String formattedBalance = String.format("%.2f", cachedBalance);
        String currencySingular = plugin.getConfigManager().getShopCurrencySingular();
        String currencyPlural   = plugin.getConfigManager().getShopCurrencyPlural();
        String currency         = cachedBalance == 1.0 ? currencySingular : currencyPlural;

        meta.displayName(mm.deserialize(btn.getName()).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<color:#57F527>" + formattedBalance + " " + currency).decoration(TextDecoration.ITALIC, false));
        lore.add(mm.deserialize("").decoration(TextDecoration.ITALIC, false));
        lore.add(mm.deserialize("<color:#474747>Click <gray>to refresh").decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        head.setItemMeta(meta);
        inv.setItem(slot, head);
    }

    private void addNavigation(Inventory inv, ShopMainConfig cfg, ShopCategory category, int currentPage) {
        int maxPage = category.getMaxPage();

        if (currentPage > 1) {
            ShopMainConfig.ButtonConfig prev = cfg.getPrevPageButton();
            inv.setItem(prev.getSlot(), createButton(prev, null));
        }

        ShopMainConfig.ButtonConfig indicator = cfg.getPageIndicatorButton();
        String indicatorName = indicator.getName()
                .replace("<current>", String.valueOf(currentPage))
                .replace("<max>", String.valueOf(maxPage));
        inv.setItem(indicator.getSlot(), createButton(indicator, indicatorName));

        if (currentPage < maxPage) {
            ShopMainConfig.ButtonConfig next = cfg.getNextPageButton();
            inv.setItem(next.getSlot(), createButton(next, null));
        }
    }

    private void addBackButton(Inventory inv, ShopMainConfig cfg) {
        ShopMainConfig.ButtonConfig btn = cfg.getBackButton();
        inv.setItem(btn.getSlot(), createButton(btn, null));
    }

    private void addCloseButton(Inventory inv, ShopMainConfig cfg) {
        ShopMainConfig.ButtonConfig btn = cfg.getCloseButton();
        if (!btn.isEnabled()) return;
        inv.setItem(btn.getSlot(), createButton(btn, null));
    }

    private ItemStack createButton(ShopMainConfig.ButtonConfig btn, String nameOverride) {
        Material material = parseMaterial(btn.getMaterial(), Material.PAPER);
        String name = nameOverride != null ? nameOverride : btn.getName();

        if (material == Material.PLAYER_HEAD && btn.getTexture() != null) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            SkullTextureUtil.applyTexture(meta, btn.getTexture(), plugin.getLogger());
            meta.displayName(mm.deserialize(name).decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
            return item;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize(name).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private Material parseMaterial(String name, Material fallback) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid shop button material: " + name + ". Falling back to " + fallback.name() + ".");
            return fallback;
        }
    }

    private void fillEmptySlots(Inventory inv, String materialName) {
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
}
