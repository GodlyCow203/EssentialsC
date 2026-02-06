package net.godlycow.org.essc.shop;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class ShopManager {
    private final EssentialsC plugin;
    private final ShopDatabase database;
    private final Map<String, ShopCategory> categories;
    private File shopFolder;
    private YamlConfiguration mainConfig;
    private ShopListener shopListener;

    public ShopManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.database = new ShopDatabase(plugin);
        this.categories = new HashMap<>();

        if (!plugin.getConfigManager().isShopEnabled()) {
            plugin.debug("Shop system is disabled");
            return;
        }

        try {
            database.connect();
            loadShop();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize shop: " + e.getMessage());
        }
    }

    public void setShopListener(ShopListener listener) {
        this.shopListener = listener;
    }

    private void loadShop() {
        saveResourceFiles();
        shopFolder = new File(plugin.getDataFolder(), "shop");
        if (!shopFolder.exists()) {
            shopFolder.mkdirs();
            createDefaultFiles();
        }

        File mainFile = new File(shopFolder, "main.yml");
        if (!mainFile.exists()) {
            createDefaultMainFile(mainFile);
        }
        mainConfig = YamlConfiguration.loadConfiguration(mainFile);

        ConfigurationSection catsSection = mainConfig.getConfigurationSection("categories");
        if (catsSection != null) {
            for (String key : catsSection.getKeys(false)) {
                loadCategory(key, catsSection.getConfigurationSection(key));
            }
        }

        File[] files = shopFolder.listFiles((dir, name) -> name.endsWith(".yml") && !name.equals("main.yml"));
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                String categoryId = fileName.substring(0, fileName.length() - 4);

                if (!categories.containsKey(categoryId)) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    ShopCategory category = new ShopCategory(categoryId);
                    category.setDisplayName(config.getString("name", categoryId));
                    category.setIcon(Material.valueOf(config.getString("material", "CHEST")));
                    category.setTextureUrl(config.getString("texture"));
                    category.setLore(config.getStringList("lore"));
                    category.setSlot(config.getInt("slot", 0));
                    category.setFileName(fileName);
                    category.setPermission(config.getString("permission"));
                    category.setEnabled(config.getBoolean("enabled", true));

                    loadItems(category, config);
                    categories.put(categoryId, category);
                }
            }
        }

        plugin.debug("Loaded " + categories.size() + " shop categories");
    }

    private void saveResourceFiles() {
        File shopDir = new File(plugin.getDataFolder(), "shop");
        if (!shopDir.exists()) {
            shopDir.mkdirs();
        }

        String[] files = {"main.yml", "farming.yml", "mining.yml", "spawners.yml", "enchanted_books.yml", "tools.yml", "blocks.yml", "redstone.yml", "misc.yml" };

        for (String fileName : files) {
            File file = new File(shopDir, fileName);
            if (!file.exists()) {
                plugin.saveResource("shop/" + fileName, false);
            }
        }
    }

    private void loadCategory(String id, ConfigurationSection section) {
        ShopCategory category = new ShopCategory(id);
        category.setDisplayName(section.getString("name", id));
        category.setIcon(Material.valueOf(section.getString("material", "CHEST")));
        category.setTextureUrl(section.getString("texture"));
        category.setLore(section.getStringList("lore"));
        category.setSlot(section.getInt("slot", 0));
        category.setFileName(section.getString("file", id + ".yml"));
        category.setPermission(section.getString("permission"));
        category.setEnabled(section.getBoolean("enabled", true));

        File itemFile = new File(shopFolder, category.getFileName());
        if (itemFile.exists()) {
            YamlConfiguration itemConfig = YamlConfiguration.loadConfiguration(itemFile);
            loadItems(category, itemConfig);
        }

        categories.put(id, category);
    }

    private void loadItems(ShopCategory category, YamlConfiguration config) {
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) return;

        for (String itemId : itemsSection.getKeys(false)) {
            ConfigurationSection itemSec = itemsSection.getConfigurationSection(itemId);
            if (itemSec == null) continue;

            ShopItem item = new ShopItem(itemId);
            item.setCategory(category.getId());
            item.setMaterial(Material.valueOf(itemSec.getString("material", "STONE")));
            item.setAmount(itemSec.getInt("amount", 1));
            item.setDisplayName(itemSec.getString("name"));
            item.setLore(itemSec.getStringList("lore"));
            item.setSlot(itemSec.getInt("slot", 0));
            item.setPage(itemSec.getInt("page", 1));
            item.setBuyPrice(itemSec.getDouble("buy-price", 0));
            item.setSellPrice(itemSec.getDouble("sell-price", 0));
            item.setBuyable(itemSec.getBoolean("buyable", true));
            item.setSellable(itemSec.getBoolean("sellable", true));
            item.setStock(itemSec.getInt("stock", -1));
            item.setPermission(itemSec.getString("permission"));
            item.setGlow(itemSec.getBoolean("glow", false));
            item.setTextureUrl(itemSec.getString("texture-url"));
            item.setSkullOwner(itemSec.getString("skull-owner"));
            item.setMaxStack(itemSec.getInt("max-stack", 64));

            item.setSpawner(itemSec.getBoolean("spawner", false));
            item.setSpawnerType(itemSec.getString("spawner-type", "PIG"));

            item.setEnchantedBook(itemSec.getBoolean("enchanted-book", false));
            ConfigurationSection storedEnchants = itemSec.getConfigurationSection("enchanted-book-enchants");
            if (storedEnchants != null) {
                for (String enchKey : storedEnchants.getKeys(false)) {
                    try {
                        Enchantment ench = org.bukkit.Registry.ENCHANTMENT
                                .get(org.bukkit.NamespacedKey.minecraft(enchKey.toLowerCase()));
                        if (ench != null) {
                            item.addStoredEnchantment(ench, storedEnchants.getInt(enchKey));
                        }
                    } catch (Exception ignored) {}
                }
            }

            ConfigurationSection enchants = itemSec.getConfigurationSection("enchantments");
            if (enchants != null) {
                for (String enchKey : enchants.getKeys(false)) {
                    try {
                        Enchantment ench = org.bukkit.Registry.ENCHANTMENT
                                .get(org.bukkit.NamespacedKey.minecraft(enchKey.toLowerCase()));
                        if (ench != null) {
                            item.addEnchantment(ench, enchants.getInt(enchKey));
                        }
                    } catch (Exception ignored) {}
                }
            }

            item.setCommands(itemSec.getStringList("commands"));
            category.addItem(item);
        }
    }
    private void createDefaultFiles() {
        File mainFile = new File(shopFolder, "main.yml");
        createDefaultMainFile(mainFile);
        File farmingFile = new File(shopFolder, "farming.yml");
        createExampleCategoryFile(farmingFile, "farming");
    }

    private void createDefaultMainFile(File file) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("title", "<gradient:#06FFA5:#FFE66D>Server Shop</gradient>");
        config.set("size", 54);
        config.set("fill-empty", true);
        config.set("fill-material", "BLACK_STAINED_GLASS_PANE");

        ConfigurationSection categories = config.createSection("categories");

        ConfigurationSection farming = categories.createSection("farming");
        farming.set("name", "<color:#06FFA5>Farming");
        farming.set("material", "WHEAT");
        farming.set("slot", 20);
        farming.set("file", "farming.yml");
        farming.set("lore", Arrays.asList(
                "<color:#AAAAAA>Buy and sell farming items",
                "",
                "<color:#FFE66D>Click to open"
        ));

        ConfigurationSection mining = categories.createSection("mining");
        mining.set("name", "<color:#FFE66D>Mining");
        mining.set("material", "DIAMOND_PICKAXE");
        mining.set("slot", 22);
        mining.set("file", "mining.yml");

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create default shop files");
        }
    }

    private void createExampleCategoryFile(File file, String name) {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection items = config.createSection("items");

        ConfigurationSection wheat = items.createSection("wheat");
        wheat.set("material", "WHEAT");
        wheat.set("name", "<color:#FFE66D>Wheat");
        wheat.set("lore", Arrays.asList(
                "<color:#AAAAAA>Basic farming crop",
                "",
                "<color:#06FFA5>Good for bread!"
        ));
        wheat.set("buy-price", 10);
        wheat.set("sell-price", 5);
        wheat.set("slot", 10);
        wheat.set("page", 1);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create category file");
        }
    }

    public void openMainShop(Player player) {
        if (!plugin.getConfigManager().isShopEnabled()) {
            player.sendMessage(plugin.getLanguageManager().get(player, "shop.disabled"));
            return;
        }

        if (plugin.getEconomyManager() == null) {
            ShopGUI gui = new ShopGUI(plugin, this, player, 0.0);
            gui.openMain();
            if (shopListener != null) {
                shopListener.setSession(player, new ShopSession(null, 1));
            }
            return;
        }

        plugin.getEconomyManager().getBalance(player.getUniqueId()).thenAccept(balance -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                ShopGUI gui = new ShopGUI(plugin, this, player, balance.doubleValue());
                gui.openMain();
                if (shopListener != null) {
                    shopListener.setSession(player, new ShopSession(null, 1));
                }
            });
        });
    }

    public void openCategory(Player player, String categoryId, int page) {
        ShopCategory category = categories.get(categoryId);
        if (category == null || !category.isEnabled()) {
            player.sendMessage(plugin.getLanguageManager().get(player, "shop.invalid-category"));
            return;
        }

        if (category.getPermission() != null && !player.hasPermission(category.getPermission())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "error.no_permission"));
            return;
        }

        if (plugin.getEconomyManager() == null) {
            ShopGUI gui = new ShopGUI(plugin, this, player, 0.0);
            gui.openCategory(category, page);
            if (shopListener != null) {
                shopListener.setSession(player, new ShopSession(categoryId, page));
            }
            return;
        }

        plugin.getEconomyManager().getBalance(player.getUniqueId()).thenAccept(balance -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                ShopGUI gui = new ShopGUI(plugin, this, player, balance.doubleValue());
                gui.openCategory(category, page);
                if (shopListener != null) {
                    shopListener.setSession(player, new ShopSession(categoryId, page));
                }
            });
        });
    }

    public void processPurchase(Player player, ShopItem item, int amount) {
        if (!item.isBuyable()) {
            player.sendMessage(plugin.getLanguageManager().get(player, "shop.not-buyable"));
            return;
        }

        if (item.getPermission() != null && !player.hasPermission(item.getPermission())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "error.no_permission"));
            return;
        }

        if (item.getStock() == 0) {
            player.sendMessage(plugin.getLanguageManager().get(player, "shop.out-of-stock"));
            return;
        }

        if (item.getStock() > 0 && item.getStock() < amount) {
            player.sendMessage(plugin.getLanguageManager().get(player, "shop.not-enough-stock"));
            return;
        }

        double totalPrice = item.getBuyPrice() * amount;

        ItemStack giveItem = item.createItemStack();
        giveItem.setAmount(amount);

        if (!canFitInInventory(player, giveItem)) {
            player.sendMessage(plugin.getLanguageManager().get(player, "shop.inventory-full"));
            return;
        }

        if (plugin.getEconomyManager() == null) {
            completePurchase(player, item, amount, totalPrice, giveItem, null);
            return;
        }

        BigDecimal price = BigDecimal.valueOf(totalPrice);

        plugin.getEconomyManager().has(player.getUniqueId(), price).thenAccept(hasEnough -> {
            if (!hasEnough) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(plugin.getLanguageManager().get(player, "shop.not-enough-money"));
                });
                return;
            }

            plugin.getEconomyManager().withdraw(player.getUniqueId(), price).thenAccept(success -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!success) {
                        player.sendMessage(plugin.getLanguageManager().get(player, "error.internal"));
                        return;
                    }
                    completePurchase(player, item, amount, totalPrice, giveItem, price);
                });
            });
        });
    }

    private boolean canFitInInventory(Player player, ItemStack item) {
        int freeSlots = 0;
        for (ItemStack invItem : player.getInventory().getStorageContents()) {
            if (invItem == null || invItem.getType().isAir()) {
                freeSlots++;
            } else if (invItem.isSimilar(item) && invItem.getAmount() < invItem.getMaxStackSize()) {
                int space = invItem.getMaxStackSize() - invItem.getAmount();
                if (space >= item.getAmount()) return true;
            }
        }
        return freeSlots > 0;
    }

    private void completePurchase(Player player, ShopItem item, int amount, double totalPrice,
                                  ItemStack giveItem, BigDecimal price) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(giveItem);
        if (!leftover.isEmpty()) {
            leftover.values().forEach(drop ->
                    player.getWorld().dropItemNaturally(player.getLocation(), drop));
        }

        if (item.getStock() > 0) {
            item.setStock(item.getStock() - amount);
        }

        if (plugin.getConfigManager().isShopLogTransactions()) {
            database.logPurchase(player.getUniqueId(), item.getId(), amount, totalPrice);
        }

        for (String cmd : item.getCommands()) {
            String formatted = cmd.replace("%player%", player.getName())
                    .replace("%amount%", String.valueOf(amount));
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), formatted);
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", item.getDisplayName() != null ? item.getDisplayName() : item.getId());
        placeholders.put("amount", String.valueOf(amount));
        placeholders.put("price", String.valueOf(totalPrice));
        player.sendMessage(plugin.getLanguageManager().get(player, "shop.purchase-success", placeholders));

        refreshPlayerGUI(player);

        plugin.debug(player.getName() + " bought " + amount + "x " + item.getId() + " for " + totalPrice);
    }

    public void processSale(Player player, ShopItem item, int amount) {
        if (!item.isSellable()) {
            player.sendMessage(plugin.getLanguageManager().get(player, "shop.not-sellable"));
            return;
        }

        ItemStack checkItem = item.createComparisonItem(amount);

        if (!containsMatchingItem(player, checkItem, amount)) {
            player.sendMessage(plugin.getLanguageManager().get(player, "shop.not-enough-items"));
            return;
        }

        removeMatchingItems(player, checkItem, amount);

        double totalPrice = item.getSellPrice() * amount;

        if (plugin.getEconomyManager() != null) {
            BigDecimal price = BigDecimal.valueOf(totalPrice);

            plugin.getEconomyManager().deposit(player.getUniqueId(), price).thenAccept(v -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    completeSale(player, item, amount, totalPrice);
                });
            });
        } else {
            completeSale(player, item, amount, totalPrice);
        }
    }

    private boolean containsMatchingItem(Player player, ItemStack checkItem, int amount) {
        int count = 0;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem == null || invItem.getType().isAir()) continue;
            if (isItemMatching(invItem, checkItem)) {
                count += invItem.getAmount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    private void removeMatchingItems(Player player, ItemStack checkItem, int amount) {
        int toRemove = amount;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem == null || invItem.getType().isAir()) continue;
            if (isItemMatching(invItem, checkItem)) {
                int remove = Math.min(invItem.getAmount(), toRemove);
                invItem.setAmount(invItem.getAmount() - remove);
                toRemove -= remove;
                if (toRemove <= 0) break;
            }
        }
    }

    private boolean isItemMatching(ItemStack playerItem, ItemStack checkItem) {
        if (playerItem.getType() != checkItem.getType()) return false;

        ItemMeta playerMeta = playerItem.getItemMeta();
        ItemMeta checkMeta = checkItem.getItemMeta();

        if (playerItem.getType() == Material.ENCHANTED_BOOK) {
            if (!(playerMeta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta playerBook) ||
                    !(checkMeta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta checkBook)) {
                return false;
            }
            return playerBook.getStoredEnchants().equals(checkBook.getStoredEnchants());
        }

        if (playerItem.getType() == Material.SPAWNER) {
            if (!(playerMeta instanceof BlockStateMeta playerBlock) ||
                    !(checkMeta instanceof BlockStateMeta checkBlock)) {
                return false;
            }
            if (!(playerBlock.getBlockState() instanceof CreatureSpawner playerSpawner) ||
                    !(checkBlock.getBlockState() instanceof CreatureSpawner checkSpawner)) {
                return false;
            }
            return playerSpawner.getSpawnedType() == checkSpawner.getSpawnedType();
        }

        if (playerMeta != null && checkMeta != null) {
            return playerMeta.getEnchants().equals(checkMeta.getEnchants());
        }
        return true;
    }

    private void completeSale(Player player, ShopItem item, int amount, double totalPrice) {
        if (plugin.getConfigManager().isShopLogTransactions()) {
            database.logSale(player.getUniqueId(), item.getId(), amount, totalPrice);
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", item.getDisplayName() != null ? item.getDisplayName() : item.getId());
        placeholders.put("amount", String.valueOf(amount));
        placeholders.put("price", String.valueOf(totalPrice));
        player.sendMessage(plugin.getLanguageManager().get(player, "shop.sale-success", placeholders));

        refreshPlayerGUI(player);
    }

    private void refreshPlayerGUI(Player player) {
        ShopSession session = shopListener != null ? shopListener.getSession(player) : null;

        if (session != null && session.getCategoryId() != null) {
            openCategory(player, session.getCategoryId(), session.getPage());
        } else {
            openMainShop(player);
        }
    }

    public Map<String, ShopCategory> getCategories() {
        return categories;
    }

    public ShopCategory getCategory(String id) {
        return categories.get(id);
    }

    public void reload() {
        categories.clear();

        File mainFile = new File(shopFolder, "main.yml");
        if (mainFile.exists()) {
            mainConfig = YamlConfiguration.loadConfiguration(mainFile);
        }

        ConfigurationSection catsSection = mainConfig.getConfigurationSection("categories");
        if (catsSection != null) {
            for (String key : catsSection.getKeys(false)) {
                loadCategory(key, catsSection.getConfigurationSection(key));
            }
        }

        plugin.debug("Shop reloaded with " + categories.size() + " categories");
    }

    public void shutdown() {
        database.disconnect();
    }
}