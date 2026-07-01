package net.godlycow.org.essc.server;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class ChatEventAdapter {

    private final Player player;
    private final Component message;
    private final boolean isPaperEvent;
    private AsyncChatEvent paperEvent;
    private AsyncPlayerChatEvent legacyEvent;

    private ChatEventAdapter(AsyncChatEvent event) {
        this.player = event.getPlayer();
        this.message = event.message();
        this.isPaperEvent = true;
        this.paperEvent = event;
    }

    private ChatEventAdapter(AsyncPlayerChatEvent event) {
        this.player = event.getPlayer();
        this.message = Component.text(event.getMessage());
        this.isPaperEvent = false;
        this.legacyEvent = event;
    }

    public static ChatEventAdapter of(AsyncChatEvent event) {
        return new ChatEventAdapter(event);
    }

    public static ChatEventAdapter of(AsyncPlayerChatEvent event) {
        return new ChatEventAdapter(event);
    }

    public Player getPlayer() {
        return player;
    }

    public Component getMessage() {
        return message;
    }

    public String getMessageAsString() {
        return PlainTextComponentSerializer.plainText().serialize(message);
    }

    public void cancel() {
        if (isPaperEvent) {
            paperEvent.setCancelled(true);
        } else {
            legacyEvent.setCancelled(true);
        }
    }

    public boolean isCancelled() {
        if (isPaperEvent) {
            return paperEvent.isCancelled();
        }
        return legacyEvent.isCancelled();
    }
}