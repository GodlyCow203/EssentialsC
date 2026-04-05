
package net.godlycow.org.essc.command.warp;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.warp.Warp;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class WarpsCommand extends Command {

    private final MiniMessage mm = MiniMessage.miniMessage();

    public WarpsCommand(EssentialsC plugin) {
        super(plugin, "warps", "essentialsc.warps", true, 0, "command.usage.warps");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (!plugin.getConfigManager().isWarpEnabled()) {
            player.sendMessage(lang.get(player, "warp.disabled"));
            plugin.debug("Warps command blocked: warp system disabled in config");
            return true;
        }

        List<Warp> visibleWarps = plugin.getWarpManager().getVisibleWarps().stream()
                .filter(w -> w.getPermission() == null || player.hasPermission(w.getPermission()))
                .collect(Collectors.toList());

        if (visibleWarps.isEmpty()) {
            player.sendMessage(lang.get(player, "warp.no_warps"));
            return true;
        }

        if (plugin.getConfigManager().isWarpGroupByCategory()) {
            sendCategorizedList(player, visibleWarps);
        } else {
            sendFlatList(player, visibleWarps);
        }

        return true;
    }

    private void sendFlatList(Player player, List<Warp> warps) {
        StringBuilder sb = new StringBuilder();
        sb.append(lang.get(player, "warp.list_header"));
        sb.append("\n");

        for (int i = 0; i < warps.size(); i++) {
            Warp w = warps.get(i);
            sb.append("<click:run_command:/warp ").append(w.getName()).append(">");
            sb.append("<hover:show_text:'");
            sb.append("World: ").append(w.getLocation().getWorld().getName()).append("\\n");
            if (!w.getDescription().isEmpty()) {
                sb.append(w.getDescription()).append("\\n");
            }
            if (w.getCost() > 0 && plugin.getConfigManager().isEconomyEnabled()) {
                sb.append("Cost: ").append(String.format("%.2f", w.getCost())).append(" ").append(plugin.getConfigManager().getCurrencyPlural());
            }
            sb.append("'>");
            sb.append("<color:#06FFA5>").append(w.getName()).append("</color>");
            sb.append("</hover>");
            sb.append("</click>");

            if (i < warps.size() - 1) {
                sb.append("<color:#888888>, </color>");
            }

            if ((i + 1) % 5 == 0 && i < warps.size() - 1) {
                sb.append("\n");
            }
        }

        player.sendMessage(mm.deserialize(sb.toString()));
    }

    private void sendCategorizedList(Player player, List<Warp> warps) {
        Map<String, List<Warp>> byCategory = warps.stream()
                .collect(Collectors.groupingBy(Warp::getCategory));

        player.sendMessage(lang.get(player, "warp.list_header"));

        byCategory.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String category = entry.getKey();
                    List<Warp> categoryWarps = entry.getValue();

                    Map<String, String> catPlaceholder = new HashMap<>();
                    catPlaceholder.put("category", category);
                    player.sendMessage(lang.get(player, "warp.category_header", catPlaceholder));

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < categoryWarps.size(); i++) {
                        Warp w = categoryWarps.get(i);
                        sb.append("<click:run_command:/warp ").append(w.getName()).append(">");
                        sb.append("<hover:show_text:'").append(w.getLocation().getWorld().getName()).append("'>");
                        sb.append("<color:#FFE66D>").append(w.getName()).append("</color>");
                        sb.append("</hover>");
                        sb.append("</click>");

                        if (i < categoryWarps.size() - 1) {
                            sb.append("<color:#888888>, </color>");
                        }
                    }
                    player.sendMessage(mm.deserialize(sb.toString()));
                });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!plugin.getConfigManager().isWarpEnabled()) {
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }
}