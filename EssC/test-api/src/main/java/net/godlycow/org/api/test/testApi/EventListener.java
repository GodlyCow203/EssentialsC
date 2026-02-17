package net.godlycow.org.api.test.testApi;

import net.godlycow.org.essc.api.event.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EventListener implements Listener {
    private final Main plugin;
    private final ChatColor g = ChatColor.GRAY;
    private final ChatColor w = ChatColor.WHITE;
    private final ChatColor d = ChatColor.DARK_GRAY;

    public EventListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHomeCreate(HomeCreateEvent event) {
        TestRunner.markEventFired("HomeCreateEvent");
        Player player = event.getPlayer();
        String msg = g + "[" + d + "EVENT" + g + "] " + w + "HomeCreate" + g + " | " + d + player.getName() + g + " | " + d + event.getHome().getName();
        broadcast(msg);
    }

    @EventHandler
    public void onHomeDelete(HomeDeleteEvent event) {
        TestRunner.markEventFired("HomeDeleteEvent");
        String name = event.getPlayer() != null ? event.getPlayer().getName() : "Console";
        String msg = g + "[" + d + "EVENT" + g + "] " + w + "HomeDelete" + g + " | " + d + name + g + " | " + d + event.getHome().getName();
        broadcast(msg);
    }

    @EventHandler
    public void onHomeLimitCheck(HomeLimitCheckEvent event) {
        TestRunner.markEventFired("HomeLimitCheckEvent");
        String msg = g + "[" + d + "EVENT" + g + "] " + w + "HomeLimitCheck" + g + " | " + d + event.getPlayer().getName() + g + " | Max: " + d + event.getMaxHomes();
        broadcast(msg);
    }

    @EventHandler
    public void onHomeTeleport(HomeTeleportEvent event) {
        TestRunner.markEventFired("HomeTeleportEvent");
        String msg = g + "[" + d + "EVENT" + g + "] " + w + "HomeTeleport" + g + " | " + d + event.getPlayer().getName() + g + " | " + d + event.getHome().getName();
        broadcast(msg);
    }

    @EventHandler
    public void onHomeTeleportCancel(HomeTeleportCancelEvent event) {
        TestRunner.markEventFired("HomeTeleportCancelEvent");
        String home = event.getHome() != null ? event.getHome().getName() : "null";
        String msg = g + "[" + d + "EVENT" + g + "] " + w + "HomeTeleportCancel" + g + " | " + d + event.getPlayer().getName() + g + " | " + d + event.getReason();
        broadcast(msg);
    }

    @EventHandler
    public void onHomeTeleportComplete(HomeTeleportCompleteEvent event) {
        TestRunner.markEventFired("HomeTeleportCompleteEvent");
        String msg = g + "[" + d + "EVENT" + g + "] " + w + "HomeTeleportComplete" + g + " | " + d + event.getPlayer().getName() + g + " | " + d + event.getTeleportDuration() + "ms";
        broadcast(msg);
    }

    @EventHandler
    public void onHomeWarmupStart(HomeWarmupStartEvent event) {
        TestRunner.markEventFired("HomeWarmupStartEvent");
        String msg = g + "[" + d + "EVENT" + g + "] " + w + "HomeWarmupStart" + g + " | " + d + event.getPlayer().getName() + g + " | " + d + event.getWarmupSeconds() + "s";
        broadcast(msg);
    }

    private void broadcast(String message) {
        plugin.getServer().broadcastMessage(message);
        plugin.getLogger().info(ChatColor.stripColor(message));
    }
}