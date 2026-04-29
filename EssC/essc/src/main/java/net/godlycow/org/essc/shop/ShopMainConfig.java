package net.godlycow.org.essc.shop;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class ShopMainConfig {

    private final String title;
    private final int size;
    private final boolean fillEmpty;
    private final String fillMaterial;
    private final String categoryTitle;

    private final ButtonConfig prevPageButton;
    private final ButtonConfig nextPageButton;
    private final ButtonConfig pageIndicatorButton;
    private final ButtonConfig closeButton;
    private final ButtonConfig backButton;
    private final ButtonConfig balanceButton;

    public ShopMainConfig(YamlConfiguration config) {
        this.title         = config.getString("title", "<gradient:#00E5FF:#FFC857><bold>Server Shop</bold></gradient>");
        this.size          = config.getInt("size", 54);
        this.fillEmpty     = config.getBoolean("fill-empty", true);
        this.fillMaterial  = config.getString("fill-material", "BLACK_STAINED_GLASS_PANE");
        this.categoryTitle = config.getString("category-title", "<gradient:#00E5FF:#FFC857><bold><category></bold></gradient>");

        ConfigurationSection buttons = config.getConfigurationSection("buttons");

        this.prevPageButton      = readButton(buttons, "prev-page",      45, "ARROW",        null, "<color:#474747>Previous Page", false, -1);
        this.nextPageButton      = readButton(buttons, "next-page",       53, "ARROW",        null, "<color:#474747>Next Page",     false, -1);
        this.pageIndicatorButton = readButton(buttons, "page-indicator",  49, "PAPER",        null, "<color:#F5C827>Page <current>/<max>", false, -1);
        this.closeButton         = readButton(buttons, "close",           48, "BARRIER",      null, "<color:#F52727>Close",          true,  48);
        this.backButton          = readButton(buttons, "back",            48, "BARRIER",      null, "<color:#F52727>Back to Categories", false, -1);
        this.balanceButton       = readButton(buttons, "balance",         50, "PLAYER_HEAD",  null, "<color:#F5C827>Your Balance",   false, 47);
    }

    private ButtonConfig readButton(ConfigurationSection buttons, String key, int defaultSlot,
                                    String defaultMaterial, String defaultTexture, String defaultName,
                                    boolean hasEnabled, int defaultSlotCategory) {
        if (buttons == null) {
            return new ButtonConfig(defaultSlot, defaultMaterial, defaultTexture, defaultName, true, defaultSlotCategory);
        }
        ConfigurationSection sec = buttons.getConfigurationSection(key);
        if (sec == null) {
            return new ButtonConfig(defaultSlot, defaultMaterial, defaultTexture, defaultName, true, defaultSlotCategory);
        }
        int slot          = sec.getInt("slot", defaultSlot);
        String material   = sec.getString("material", defaultMaterial);
        String texture    = sec.getString("texture", defaultTexture);
        String name       = sec.getString("name", defaultName);
        boolean enabled   = !hasEnabled || sec.getBoolean("enabled", true);
        int slotCategory  = sec.getInt("slot-category", defaultSlotCategory != -1 ? defaultSlotCategory : slot);
        return new ButtonConfig(slot, material, texture, name, enabled, slotCategory);
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public boolean isFillEmpty() {
        return fillEmpty;
    }

    public String getFillMaterial() {
        return fillMaterial;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public ButtonConfig getPrevPageButton() {
        return prevPageButton;
    }

    public ButtonConfig getNextPageButton() {
        return nextPageButton;
    }

    public ButtonConfig getPageIndicatorButton() {
        return pageIndicatorButton;
    }

    public ButtonConfig getCloseButton() {
        return closeButton;
    }

    public ButtonConfig getBackButton() {
        return backButton;
    }

    public ButtonConfig getBalanceButton() {
        return balanceButton;
    }

    public static class ButtonConfig {

        private final int slot;
        private final String material;
        private final String texture;
        private final String name;
        private final boolean enabled;
        private final int slotCategory;

        public ButtonConfig(int slot, String material, String texture, String name, boolean enabled, int slotCategory) {
            this.slot         = slot;
            this.material     = material;
            this.texture      = (texture == null || texture.isBlank()) ? null : texture;
            this.name         = name;
            this.enabled      = enabled;
            this.slotCategory = slotCategory;
        }

        public int getSlot() {
            return slot;
        }

        public int getSlotCategory() {
            return slotCategory != -1 ? slotCategory : slot;
        }

        public String getMaterial() {
            return material;
        }

        public String getTexture() {
            return texture;
        }

        public String getName() {
            return name;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }
}
