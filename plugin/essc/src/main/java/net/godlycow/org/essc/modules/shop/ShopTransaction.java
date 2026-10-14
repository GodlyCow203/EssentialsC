package net.godlycow.org.essc.modules.shop;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ShopTransaction(Type type, UUID playerId, String playerName, Source source,
                              List<Entry> entries, double total) {

    public enum Type {
        BUY, SELL
    }

    public enum Source {
        SHOP("shop"),
        SELL_GUI("sell-gui"),
        QUICKSELL_HAND("quicksell-hand"),
        QUICKSELL_INVENTORY("quicksell-inventory");

        private final String id;

        Source(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    public record Entry(String itemId, String itemName, int amount, double price) {

        public static Entry of(ShopItem item, int amount, double price)
        {
            return new Entry(item.getId(), plainName(item), amount, price);
        }

        private static String plainName(ShopItem item)
        {

            if (item.getDisplayName() == null) {
                return item.getId();
            }

            String plain = PlainTextComponentSerializer.plainText()
                    .serialize(MiniMessage.miniMessage().deserialize(item.getDisplayName()));
            return plain.isBlank() ? item.getId() : plain;
        }

    }

    public static ShopTransaction of(Type type, Player player, Source source, List<Entry> entries) {
        double total = 0;
        for (Entry entry : entries)

        {
            total += entry.price();
        }
        return new ShopTransaction(type, player.getUniqueId(), player.getName(), source, List.copyOf(entries), total);
    }

    public int totalItems() {
        int count = 0;
        for (Entry entry : entries)

        {
            count += entry.amount();
        }

        return count;
    }


    public List<Entry> mergedEntries() {
        Map<String, Entry> merged = new LinkedHashMap<>();
        for (Entry entry : entries)
        {
            merged.merge(entry.itemId(), entry, (a, b) ->  new Entry(a.itemId(), a.itemName(), a.amount() + b.amount(), a.price() + b.price()));
        }
        return List.copyOf(merged.values());
    }


    public String describeItems(int maxLength) {

        List<Entry> merged = mergedEntries();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < merged.size(); i++)

        {
            Entry entry = merged.get(i);
            String line = (sb.isEmpty() ? "" : "\n") + entry.amount() + "x " + entry.itemName() + " [" + entry.itemId() + "]";
            boolean last = i == merged.size() - 1;
            int reserved = last ? 0 : 24;

            if (sb.length() + line.length() + reserved > maxLength)

            {
                sb.append(sb.isEmpty() ? "" : "\n").append("… and ").append(merged.size() - i).append(" more");
                break;
            }

            sb.append(line);
        }

        return sb.toString();
    }
}
