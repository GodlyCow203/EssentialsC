package net.godlycow.org.essc.command.player;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.util.PaginatedList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerListCommand extends Command {

    public PlayerListCommand(EssentialsC plugin) {
        super(plugin, "playerlist", "essentialsc.playerlist", false, 0, "command.usage.playerlist");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int total = onlinePlayers.size();
        int max = Bukkit.getMaxPlayers();


        if (total == 0) {
            sender.sendMessage(lang.get(sender, "playerlist.empty"));
            return true;
        }

        int requestedPage = 1;
        if (args.length >= 1) {
            try {
                requestedPage = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                sender.sendMessage(lang.get(sender, "playerlist.invalid_page"));
                return true;
            }
        }

        List<Map.Entry<Player, String>> playerDisplays = new ArrayList<>();
        for (Player player : onlinePlayers) {
            playerDisplays.add(new AbstractMap.SimpleEntry<>(player, player.getName()));
        }

        playerDisplays.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getValue(), b.getValue()));

        PaginatedList<Map.Entry<Player, String>> paginated = new PaginatedList<>(playerDisplays, 10);
        int page = paginated.clamp(requestedPage);

        if (!paginated.isValidPage(requestedPage)) {
            sender.sendMessage(lang.get(sender, "playerlist.invalid_page"));
            return true;
        }

        Map<String, String> headerPlaceholders = new HashMap<>();
        headerPlaceholders.put("online", String.valueOf(total));
        headerPlaceholders.put("max", String.valueOf(max));
        headerPlaceholders.put("page", String.valueOf(page));
        headerPlaceholders.put("total_pages", String.valueOf(paginated.getTotalPages()));
        sender.sendMessage(lang.get(sender, "playerlist.header", headerPlaceholders));

        for (Map.Entry<Player, String> entry : paginated.getPage(page)) {
            String display = entry.getValue();

            Map<String, String> entryPlaceholders = new HashMap<>();
            entryPlaceholders.put("name", display);
            sender.sendMessage(lang.get(sender, "playerlist.entry", entryPlaceholders));
        }

        sender.sendMessage(lang.get(sender, "playerlist.footer", headerPlaceholders));

        if (sender instanceof Player) {
            boolean hasPrev = paginated.hasPreviousPage(page);
            boolean hasNext = paginated.hasNextPage(page);

            if (hasPrev || hasNext) {
                Map<String, String> navPlaceholders = new HashMap<>();
                navPlaceholders.put("prev_page", String.valueOf(page - 1));
                navPlaceholders.put("next_page", String.valueOf(page + 1));
                navPlaceholders.put("has_prev", String.valueOf(hasPrev));
                navPlaceholders.put("has_next", String.valueOf(hasNext));
                navPlaceholders.put("total_pages", String.valueOf(paginated.getTotalPages()));
                sender.sendMessage(lang.get(sender, "playerlist.navigation", navPlaceholders));
            }
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            List<String> playerDisplays = new ArrayList<>();

            for (Player player : onlinePlayers) {
                playerDisplays.add(player.getName());
            }

            playerDisplays.sort(String.CASE_INSENSITIVE_ORDER);
            PaginatedList<String> paginated = new PaginatedList<>(playerDisplays, 10);
            int totalPages = paginated.getTotalPages();

            return java.util.stream.IntStream.rangeClosed(1, totalPages).mapToObj(String::valueOf).filter(n -> n.startsWith(args[0])).toList();
        }
        return Collections.emptyList();
    }
}