package net.godlycow.org.essc.integration.discord;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.modules.shop.ShopTransaction;
import net.godlycow.org.essc.util.FormatUtil;

import java.awt.Color;

public class ShopSellNotifier extends DiscordNotifier {

    public ShopSellNotifier(EssentialsC plugin, DiscordSRVHook hook) {
        super(plugin, hook);
    }

    public void notifySell(ShopTransaction transaction) {

        if (!plugin.getConfigManager().isDiscordShopSellEnabled()) {
            return;
        }

        String channelName = plugin.getConfigManager().getDiscordSRVShopChannelName();
        Color color = parseColor(plugin.getConfigManager().getDiscordShopSellColor());
        String avatarUrl = getAvatarUrl(transaction.playerId());

        sendEmbedWithAvatar(
                channelName, "Shop Sale", color, avatarUrl,
                new EmbedField("Player", transaction.playerName(), true),
                new EmbedField("Source", transaction.source().id(), true),
                new EmbedField("Total Earned", FormatUtil.formatMoney(transaction.total(), plugin.getConfigManager().getEconomyFormat()), true),
                new EmbedField("Items", transaction.describeItems(1024), false)
        );
    }

    @Override
    protected String getSuccessMessage(String title) {
        return "Shop sale embed sent to Discord";
    }
}
