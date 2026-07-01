package net.godlycow.org.expansions.profiles.chat;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.expansions.profiles.EssentialsCProfiles;
import net.godlycow.org.expansions.profiles.messages.MessagesManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChatProfileListener implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Style NO_ITALIC = Style.style(TextDecoration.ITALIC.withState(false));

    private final EssentialsCProfiles plugin;
    private final EssentialsC essc;
    private final MessagesManager msg;

    public ChatProfileListener(EssentialsCProfiles plugin, EssentialsC essc, MessagesManager msg) {
        this.plugin = plugin;
        this.essc   = essc;
        this.msg    = msg;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        Component hoverCard = buildHoverCard(sender);
        ChatRenderer upstream = event.renderer();

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            Component fullLine = upstream.render(source, sourceDisplayName, message, viewer);

            if (viewer instanceof Player vp && vp.hasPermission("essentialscprofiles.chat.hover")) {
                return fullLine
                        .hoverEvent(HoverEvent.showText(hoverCard))
                        .clickEvent(ClickEvent.runCommand("/profile " + source.getName()));
            }
            return fullLine;
        });
    }

    private Component buildHoverCard(Player player) {
        List<Component> lines = new ArrayList<>();

        lines.add(noItalic(MM.deserialize(
                msg.raw("chat.hover-header").replace("<player>", player.getName()))));
        lines.add(noItalic(MM.deserialize(msg.raw("chat.hover-separator"))));

        String nick = essc.getNickManager() != null
                ? essc.getNickManager().getCachedNickname(player.getUniqueId())
                : null;

        if (nick != null && !nick.isEmpty()) {
            lines.add(noItalic(MM.deserialize(
                    msg.raw("chat.hover-nick").replace("<nick>", nick))));
        }

        boolean afk = essc.getAfkManager() != null && essc.getAfkManager().isAFK(player);
        lines.add(noItalic(MM.deserialize(msg.raw(afk ? "chat.hover-afk" : "chat.hover-online"))));

        if (essc.getEconomyManager() != null) {
            String balStr;
            try {
                BigDecimal bal = essc.getEconomyManager()
                        .getBalance(player.getUniqueId())
                        .get(50, TimeUnit.MILLISECONDS);
                balStr = essc.getEconomyManager().format(bal != null ? bal : BigDecimal.ZERO);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                balStr = "...";
            }
            lines.add(noItalic(MM.deserialize(
                    msg.raw("chat.hover-balance").replace("<balance>", balStr))));
        }

        lines.add(noItalic(MM.deserialize(msg.raw("chat.hover-separator"))));
        lines.add(noItalic(MM.deserialize(msg.raw("chat.hover-footer"))));

        Component card = Component.empty();
        for (int i = 0; i < lines.size(); i++) {
            card = card.append(lines.get(i));
            if (i < lines.size() - 1) card = card.append(Component.newline());
        }
        return card;
    }

    private static Component noItalic(Component c) {
        return c.applyFallbackStyle(NO_ITALIC);
    }
}