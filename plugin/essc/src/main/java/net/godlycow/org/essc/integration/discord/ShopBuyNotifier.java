package net.godlycow.org.essc.integration.discord;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.modules.shop.ShopTransaction;
import net.godlycow.org.essc.util.FormatUtil;

import java.awt.Color;

public class ShopBuyNotifier extends DiscordNotifier {

    public ShopBuyNotifier(EssentialsC plugin, DiscordSRVHook hook) {
        super(plugin, hook);
    }

    public void notifyBuy(ShopTransaction transaction) {
        if (!plugin.getConfigManager().isDiscordShopBuyEnabled()) {
            return;
        }

        String channelName = plugin.getConfigManager().getDiscordSRVShopChannelName();
        Color color = parseColor(plugin.getConfigManager().getDiscordShopBuyColor());
        String avatarUrl = getAvatarUrl(transaction.playerId());

        sendEmbedWithAvatar(channelName, "Shop Purchase", color, avatarUrl,
                new EmbedField("Player", transaction.playerName(), true),
                new EmbedField("Source", transaction.source().id(), true),
                new EmbedField("Total Paid", FormatUtil.formatMoney(transaction.total(), plugin.getConfigManager().getEconomyFormat()), true),
                new EmbedField("Items", transaction.describeItems(1024), false)
        );
    }

    @Override
    protected String getSuccessMessage(String title) {
        return "Shop purchase embed sent to Discord";
    }
}
