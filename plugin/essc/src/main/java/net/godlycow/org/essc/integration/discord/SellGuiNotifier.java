package net.godlycow.org.essc.integration.discord;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.modules.shop.ShopTransaction;
import net.godlycow.org.essc.util.FormatUtil;

import java.awt.Color;

public class SellGuiNotifier extends DiscordNotifier {

    public SellGuiNotifier(EssentialsC plugin, DiscordSRVHook hook) {
        super(plugin, hook);
    }

    public void notifySellGui(ShopTransaction transaction) {
        if (!plugin.getConfigManager().isDiscordSellGuiEnabled()) {
            return;
        }

        String channelName = plugin.getConfigManager().getDiscordSRVShopChannelName();
        Color color = parseColor(plugin.getConfigManager().getDiscordSellGuiColor());
        String avatarUrl = getAvatarUrl(transaction.playerId());

        sendEmbedWithAvatar(channelName, "Items Sold (Sell GUI)", color, avatarUrl,
                new EmbedField("Player", transaction.playerName(), true),
                new EmbedField("Items Sold", String.valueOf(transaction.totalItems()), true),
                new EmbedField("Total Earned", FormatUtil.formatMoney(transaction.total(), plugin.getConfigManager().getEconomyFormat()), true),
                new EmbedField("Items", transaction.describeItems(1024), false)
        );
    }

    @Override
    protected String getSuccessMessage(String title) {
        return "Sell GUI embed sent to Discord";
    }
}
