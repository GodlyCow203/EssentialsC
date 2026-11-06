package net.godlycow.org.essc.modules.punishment;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.util.DurationParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public final class BannedConnectionLogFilter extends AbstractFilter {

    public enum Mode
    {
        NORMAL, RATE_LIMITED, HIDDEN
    }


    private final EssentialsC plugin;
    private final PunishmentManager punishmentManager;

    private volatile Mode mode = Mode.NORMAL;
    private volatile long intervalMs = 600_000L;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Map<String, LastRejection> lastRejectionByName = new ConcurrentHashMap<>();
    private final Map<String, Pending> pendingByName = new ConcurrentHashMap<>();

    private boolean installed;

    public BannedConnectionLogFilter(EssentialsC plugin,  PunishmentManager punishmentManager) {
        super(Result.NEUTRAL, Result.NEUTRAL);
        this.plugin = plugin;
        this.punishmentManager = punishmentManager;
        reloadSettings();
    }

    public void install()
    {
        try {
            ((Logger) LogManager.getRootLogger()).addFilter(this);
            installed = true;

        } catch (Throwable t) {
            plugin.getLogger().warning("Could not install banned-connection log filter: " + t);
        }

    }

    public void uninstall() {
        if (!installed)
            return;
        try {
            ((Logger) LogManager.getRootLogger()).get().removeFilter(this);
        } catch (Throwable ignored)
        {
        }

        installed = false;
    }

    public void reloadSettings() {
        String raw = plugin.getConfig().getString("logging.banned-player-connections.mode", "normal");
        Mode parsed;
        try {
            parsed = Mode.valueOf(raw.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            parsed = Mode.NORMAL;
        }
        long seconds = plugin.getConfig().getLong("logging.banned-player-connections.interval-seconds", 600L);
        mode = parsed;
        intervalMs = Math.max(1L, seconds) * 1000L;
    }

    public void onRejected(String name, UUID uuid,  String ip, boolean ipBan, String banKey, Component kickMessage) {
        reloadSettings();
        Mode current = mode;
        if (current == Mode.NORMAL) return;

        long now = System.currentTimeMillis();

        String nameKey = name.toLowerCase(Locale.ROOT);
        lastRejectionByName.put(nameKey, new LastRejection(uuid, ip, ipBan, banKey));

        if (current == Mode.RATE_LIMITED) {
            Window window = windows.get(banKey);
            if (window == null || now - window.start >= intervalMs) {
                if (window != null && window.suppressed.get() > 0) {
                    plugin.getLogger().info("Suppressed " + window.suppressed.get() + " connection attempt(s) from banned "
                            + (ipBan ? "IP " + ip + " (last name: " + name + ")" : "player " + name + " (" + ip + ")")
                            + " in the last " + DurationParser.format(now - window.start) + ".");
                }
                windows.put(banKey, new Window(now));
                pendingByName.remove(nameKey);
                pruneWindows(now);
                return;
            }
            window.suppressed.incrementAndGet();
        }

        pendingByName.put(nameKey, new Pending(name, fingerprint(kickMessage), now + 15_000L));
    }

    @Override
    public Result filter(LogEvent event) {

        if (mode == Mode.NORMAL  || event == null)
            return Result.NEUTRAL;
        Message message = event.getMessage();
        if (message == null)
            return Result.NEUTRAL;

        String text = message.getFormattedMessage();
        if (text == null)
            return Result.NEUTRAL;

        if (text.startsWith("UUID of player ")) {

            return shouldHideUuidLine(text) ? Result.DENY : Result.NEUTRAL;
        }

        if (text.startsWith("Disconnecting ") || text.contains(" lost connection: ")) {
            return isPendingDisconnect(text) ? Result.DENY : Result.NEUTRAL;
        }
        return Result.NEUTRAL;
    }


    private boolean shouldHideUuidLine(String text) {
        String rest = text.substring("UUID of player ".length());
        int split = rest.lastIndexOf(" is ");
        if (split <= 0)
            return false;
        String name = rest.substring(0, split);
        UUID uuid;
        try {
            uuid = UUID.fromString(rest.substring(split + 4).trim());
        } catch (IllegalArgumentException e) {
            return false;
        }

        LastRejection last = lastRejectionByName.get(name.toLowerCase(Locale.ROOT));
        String currentKey = null;

        if (punishmentManager.isBanned(uuid)) {
            currentKey = banKey(punishmentManager.getBanEntry(uuid), uuid);
        } else if (last != null && last.ipBan && punishmentManager.isIpBanned(last.ip)) {
            currentKey = ipBanKey(punishmentManager.getIpBanEntry(last.ip), last.ip);
        }

        if (currentKey == null)
            return false;
        if (mode == Mode.HIDDEN)
            return true;

        if (last == null || !currentKey.equals(last.banKey))
            return false;

        Window window = windows.get(currentKey);
        return window != null && System.currentTimeMillis() - window.start < intervalMs;
    }

    private boolean isPendingDisconnect(String text) {
        if (pendingByName.isEmpty())
            return false;

        long now = System.currentTimeMillis();
        for (Map.Entry<String, Pending> entry : pendingByName.entrySet()) {
            Pending pending = entry.getValue();
            if (pending.expiresAt < now) {
                pendingByName.remove(entry.getKey(), pending);
                continue;
            }
            if (text.contains(pending.name)
                    && (pending.fingerprint.isEmpty() || text.contains(pending.fingerprint))) {
                return true;
            }
        }
        return false;
    }

    private void pruneWindows(long now) {
        if (windows.size() < 256)
            return;
        windows.entrySet().removeIf(e -> now - e.getValue().start >= intervalMs && e.getValue().suppressed.get() == 0);
        lastRejectionByName.entrySet().removeIf(e -> !windows.containsKey(e.getValue().banKey));
    }

    public static String banKey(PunishmentManager.BanEntry entry, UUID uuid) {
        return "ban|" + uuid + "|" + (entry != null ? entry.id() + "|" + entry.reason() : "");
    }

    public static String ipBanKey(PunishmentManager.IpBanEntry entry, String ip) {
        return "ipban|" + ip + "|" + (entry != null ? entry.time() + "|" + entry.reason() : "");
    }


    private static String fingerprint(Component kickMessage) {
        String plain = PlainTextComponentSerializer.plainText().serialize(kickMessage);
        for (String line : plain.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty())
                return trimmed;
        }
        return "";
    }

    private static final class Window {
        final long start;
        final AtomicInteger suppressed = new AtomicInteger();

        Window(long start) {
            this.start = start;
        }
    }

    private record LastRejection(UUID uuid, String ip, boolean ipBan, String banKey) {}

    private record Pending(String name, String fingerprint, long expiresAt) {}
}
