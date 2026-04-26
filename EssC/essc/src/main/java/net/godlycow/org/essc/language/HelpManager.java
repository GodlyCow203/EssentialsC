package net.godlycow.org.essc.language;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpManager {

    private final EssentialsC plugin;
    private final MiniMessage mm;
    private final Gson gson = new Gson();

    private final Map<String, Map<String, String>> cache = new HashMap<>();
    private String defaultLang;

    private static final String[] SUPPORTED_LANGUAGES = {
            "th_TH", "sv_SE", "fil_PH", "vi_VN", "id_ID", "uk_UA", "pl_PL", "nl_NL",
            "it_IT", "te_IN", "ta_IN", "gu_IN", "bn_BD", "mr_IN", "hi_IN", "de_DE",
            "ur_PK", "tr_TR", "ar_SA", "ar_EG", "ko_KR", "ru_RU", "ja_JP", "fr_FR",
            "zh_CN", "pt_BR", "de_CH", "es_ES", "en_GB", "en_US", "LB_lb"
    };

    public HelpManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.mm = plugin.getMiniMessage();
    }

    public void load(String defaultLanguage) {
        this.defaultLang = defaultLanguage;
        cache.clear();

        File helpFolder = new File(plugin.getDataFolder(), "lang/help");
        if (!helpFolder.exists()) helpFolder.mkdirs();

        for (String lang : SUPPORTED_LANGUAGES) {
            File file = new File(helpFolder, lang + ".json");
            if (!file.exists()) {
                String resource = "lang/help/" + lang + ".json";
                if (plugin.getResource(resource) != null) plugin.saveResource(resource, false);
            }
            migrateHelpFile(lang);
        }

        loadIntoCache(defaultLanguage);
    }

    public void reload() {
        plugin.debug("Reloading HelpManager...");
        load(defaultLang);
    }

    public void sendHelp(CommandSender sender, String command, String sub) {
        Map<String, String> map = resolveLanguageMap(sender);
        List<Component> lines = buildLines(map, command, sub);
        if (lines.isEmpty()) return;

        if (!(sender instanceof Player player)) {
            sendToConsole(sender, lines);
            return;
        }

        sendToPlayer(player, lines);
    }

    private void sendToConsole(CommandSender sender, List<Component> lines) {
        for (Component line : lines) {
            sender.sendMessage(Component.text(PlainTextComponentSerializer.plainText().serialize(line)));
        }
    }

    private void sendToPlayer(Player player, List<Component> lines) {
        boolean soundEnabled = plugin.getConfigManager().isHelpSoundEnabled();
        boolean animated = plugin.getConfigManager().isHelpAnimated();
        long delayTicks = plugin.getConfigManager().getHelpAnimationDelay();

        if (soundEnabled) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1.2f);
        }

        for (int i = 0; i < lines.size(); i++) {
            final Component line = lines.get(i);
            final boolean isLast = (i == lines.size() - 1);
            final long delay = animated ? Math.max(1L, (long) i * delayTicks) : 1L;

            plugin.getEssScheduler().runForEntityLater(player, () -> {
                if (!player.isOnline()) return;
                player.sendMessage(line);
                if (isLast && soundEnabled) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.4f, 1.8f);
                }
            }, delay);
        }
    }

    private List<Component> buildLines(Map<String, String> map, String command, String sub) {
        String prefix = sub != null ? "help." + command + "." + sub : "help." + command;
        if (sub != null && !hasAnyKey(map, prefix)) prefix = "help." + command;

        Styles s = new Styles(map);
        List<Component> out = new ArrayList<>();

        String title = sub != null ? "/" + command + " " + sub : "/" + command;
        out.add(render(s, "{rule}──{reset} {header}<bold>" + title + "</bold>{reset} {rule}──"));

        String desc = get(map, prefix + ".description");
        if (desc != null) {
            out.add(render(s, "{desc}" + desc));
            out.add(Component.empty());
        }

        addSyntaxSection(out, s, map, prefix + ".syntax");
        addEntrySection(out, s, map, prefix + ".args", str(map, "label.args", "Arguments"));
        addEntrySection(out, s, map, prefix + ".subcommands", str(map, "label.subcommands", "Subcommands"));
        addSimpleSection(out, s, map, prefix + ".examples", str(map, "label.examples", "Examples"), "example");
        addSimpleSection(out, s, map, prefix + ".permission", str(map, "label.permission", "Permission"), "perm");

        if (out.size() > 1) {
            out.add(render(s, "{rule}" + str(map, "style.rule_text", "──────────────────────────")));
        }

        return out;
    }

    private void addSyntaxSection(List<Component> out, Styles s, Map<String, String> map, String key) {
        String value = get(map, key);
        if (value == null) return;
        out.add(render(s, "{label}" + str(map, "label.syntax", "Syntax")));
        for (String raw : split(value)) {
            out.add(render(s, "  {desc}" + raw));
        }
    }

    private void addEntrySection(List<Component> out, Styles s, Map<String, String> map, String key, String label) {
        String value = get(map, key);
        if (value == null) return;
        String sep = str(map, "label.sep", "—");
        out.add(render(s, "{label}" + label));
        for (String raw : split(value)) {
            raw = raw.trim();
            int sp = raw.indexOf(' ');
            if (sp == -1) {
                out.add(render(s, "  {name}" + raw));
            } else {
                String name = raw.substring(0, sp);
                String detail = raw.substring(sp + 1).trim();
                out.add(render(s, "  {name}" + name + " {dim}" + sep + "{reset} {desc}" + detail));
            }
        }
    }

    private void addSimpleSection(List<Component> out, Styles s, Map<String, String> map, String key, String label, String styleKey) {
        String value = get(map, key);
        if (value == null) return;
        String sep = str(map, "label.sep", "—");
        out.add(render(s, "{label}" + label));
        for (String raw : split(value)) {
            raw = raw.trim();
            int colon = raw.indexOf(':');
            if (colon > 0 && colon < raw.length() - 1) {
                String prefix = raw.substring(0, colon).trim();
                String rest = raw.substring(colon + 1).trim();
                out.add(render(s, "  {name}" + prefix + " {dim}" + sep + "{reset} {" + styleKey + "}" + rest));
            } else {
                out.add(render(s, "  {" + styleKey + "}" + raw));
            }
        }
    }

    private Component render(Styles s, String template) {
        return mm.deserialize(s.apply(template));
    }

    private static class Styles {
        private final Map<String, String> tokens;

        Styles(Map<String, String> map) {
            tokens = new HashMap<>();
            tokens.put("{header}",  map.getOrDefault("style.header",  "<color:#FFF200>"));
            tokens.put("{rule}",    map.getOrDefault("style.rule",    "<dark_gray>"));
            tokens.put("{label}",   map.getOrDefault("style.label",   "<color:#FFF200>"));
            tokens.put("{desc}",    map.getOrDefault("style.desc",    "<color:#AAAAAA>"));
            tokens.put("{name}",    map.getOrDefault("style.name",    "<white>"));
            tokens.put("{dim}",     map.getOrDefault("style.dim",     "<dark_gray>"));
            tokens.put("{perm}",    map.getOrDefault("style.perm",    "<color:#AAAAAA>"));
            tokens.put("{example}", map.getOrDefault("style.example", "<color:#AAAAAA>"));
            tokens.put("{reset}",   "<reset>");
        }

        String apply(String template) {
            String result = template;
            for (Map.Entry<String, String> entry : tokens.entrySet()) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
            return result;
        }
    }

    private String[] split(String value) {
        String[] parts = value.split("\\\\n");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result.toArray(new String[0]);
    }

    private boolean hasAnyKey(Map<String, String> map, String prefix) {
        for (String key : map.keySet()) {
            if (key.startsWith(prefix + ".")) return true;
        }
        return false;
    }

    private String get(Map<String, String> map, String key) {
        String value = map.get(key);
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String str(Map<String, String> map, String key, String fallback) {
        String value = map.get(key);
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }

    private Map<String, String> resolveLanguageMap(CommandSender sender) {
        String locale = defaultLang;

        if (sender instanceof Player player) {
            String manualLang = plugin.getLanguageManager().getPlayerLanguage(player.getUniqueId());

            if (manualLang != null) {
                locale = manualLang;
            } else {
                String clientLocale = player.locale().toString();

                for (String supported : SUPPORTED_LANGUAGES) {
                    if (supported.equalsIgnoreCase(clientLocale)) {
                        locale = supported;
                        break;
                    }
                }
            }
        }

        if (!cache.containsKey(locale)) {
            loadIntoCache(locale);
        }

        Map<String, String> map = cache.get(locale);
        if (map == null || map.isEmpty()) {
            map = cache.get(defaultLang);
        }

        return map != null ? map : new HashMap<>();
    }

    private void loadIntoCache(String code) {
        File file = new File(plugin.getDataFolder(), "lang/help/" + code + ".json");
        if (!file.exists()) return;
        Map<String, String> messages = new HashMap<>();
        try (var reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json != null) {
                json.entrySet().forEach(e -> messages.put(e.getKey(), e.getValue().getAsString()));
            }
            cache.put(code, messages);
            plugin.debug("HelpManager cached: " + code + " (" + messages.size() + " keys)");
        } catch (Exception e) {
            plugin.getLogger().warning("[EssentialsC] Failed to load help/" + code + ".json: " + e.getMessage());
        }
    }

    private void migrateHelpFile(String code) {
        File file = new File(plugin.getDataFolder(), "lang/help/" + code + ".json");
        if (!file.exists()) return;
        var resource = plugin.getResource("lang/help/" + code + ".json");
        if (resource == null) return;
        try (var reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            JsonObject defaults = gson.fromJson(reader, JsonObject.class);
            if (defaults == null) return;
            JsonObject existing;
            try (var fr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                existing = gson.fromJson(fr, JsonObject.class);
                if (existing == null) existing = new JsonObject();
            }
            boolean dirty = false;
            for (var entry : defaults.entrySet()) {
                if (!existing.has(entry.getKey())) {
                    existing.add(entry.getKey(), entry.getValue());
                    dirty = true;
                }
            }
            if (dirty) {
                try (var writer = new java.io.FileWriter(file, StandardCharsets.UTF_8)) {
                    new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(existing, writer);
                }
                plugin.getLogger().info("[EssentialsC] Migrated missing keys in lang/help/" + code + ".json");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[EssentialsC] Failed to migrate help/" + code + ".json: " + e.getMessage());
        }
    }
}