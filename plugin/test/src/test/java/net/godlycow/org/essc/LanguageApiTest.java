package net.godlycow.org.essc;

import net.godlycow.org.essc.api.impl.language.LanguageManagerImpl;
import net.godlycow.org.essc.language.LanguageManager;
import net.godlycow.org.essc.plugin.config.EssConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LanguageApiTest {

    private static final String CORE_EN = """
            {
              "prefix": "[EssC] ",
              "error.no_permission": "<prefix>No permission",
              "plugin.clash": "core value"
            }
            """;

    private static final String ADDON_EN = """
            {
              "plugin.welcome": "<prefix>Welcome <player>",
              "plugin.clash": "addon value"
            }
            """;

    @TempDir
    Path tempDir;

    private Path coreFolder;
    private Path addonFolder;
    private String addonJarEn;
    private LanguageManager internal;
    private LanguageManagerImpl api;
    private Plugin addon;

    @BeforeEach
    void setUp() throws IOException {
        coreFolder = tempDir.resolve("EssentialsC");
        addonFolder = tempDir.resolve("MyAddon");
        Files.createDirectories(coreFolder.resolve("lang"));
        Files.writeString(coreFolder.resolve("lang/en_US.json"), CORE_EN);
        addonJarEn = ADDON_EN;

        EssentialsC plugin = createPlugin();
        internal = new LanguageManager(plugin);
        when(plugin.getLanguageManager()).thenReturn(internal);
        internal.load("en_US");

        api = new LanguageManagerImpl(plugin);
        addon = createAddon();
    }

    private EssentialsC createPlugin() {
        EssentialsC plugin = mock(EssentialsC.class);
        when(plugin.getDataFolder()).thenReturn(coreFolder.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("LanguageApiTest"));
        when(plugin.getMiniMessage()).thenReturn(MiniMessage.miniMessage());

        EssConfig config = mock(EssConfig.class);
        when(config.getFallbackLanguage()).thenReturn("en_US");
        when(plugin.getConfigManager()).thenReturn(config);

        Server server = mock(Server.class);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        when(plugin.getServer()).thenReturn(server);
        return plugin;
    }

    private Plugin createAddon() {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getName()).thenReturn("Plugin");
        when(plugin.getDataFolder()).thenReturn(addonFolder.toFile());
        when(plugin.getResource("lang/en_US.json")).thenAnswer(inv ->
                new ByteArrayInputStream(addonJarEn.getBytes(StandardCharsets.UTF_8)));
        doAnswer(inv -> {
            String path = inv.getArgument(0);
            Path target = addonFolder.resolve(path);
            Files.createDirectories(target.getParent());
            Files.writeString(target, addonJarEn);
            return null;
        }).when(plugin).saveResource(anyString(), anyBoolean());
        return plugin;
    }

    private String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    void readsCoreKeys() {
        assertEquals("[EssC] No permission", plain(api.get(null, "error.no_permission")));
        assertTrue(api.hasKey("error.no_permission"));
        assertEquals("en_US", api.getDefaultLanguage());
        assertEquals("en_US", api.getLanguage(null));
        assertTrue(api.getAvailableLanguages().contains("en_US"));
    }

    @Test
    void registersAddonMessages() {
        assertFalse(api.hasKey("plugin.welcome"));

        api.registerMessages(addon, "en_US");

        assertTrue(Files.exists(addonFolder.resolve("lang/en_US.json")));
        assertTrue(api.hasKey("plugin.welcome"));
        assertEquals("[EssC] Welcome, Steve!",
                plain(api.get(null, "plugin.welcome", Map.of("player", "Steve"))));
        assertEquals("[EssC] Welcome, Steve!",
                api.getRaw(null, "plugin.welcome", Map.of("player", "Steve")));
    }

    @Test
    void coreKeysWinOnClash() {
        api.registerMessages(addon, "en_US");

        assertEquals("core value", api.getRaw(null, "plugin.clash", null));
    }

    @Test
    void addonMessagesSurviveReload() {
        api.registerMessages(addon, "en_US");

        internal.reload();

        assertTrue(api.hasKey("plugin.welcome"));
    }

    @Test
    void editedAddonFileIsUsedAndNewKeysAreMerged() throws IOException {
        api.registerMessages(addon, "en_US");
        Path file = addonFolder.resolve("lang/en_US.json");
        Files.writeString(file, """
                {
                  "plugin.welcome": "Edited <player>"
                }
                """);

        addonJarEn = """
                {
                  "plugin.welcome": "<prefix>Welcome <player>!",
                  "plugin.goodbye": "Bye <player>"
                }
                """;
        api.registerMessages(addon, "en_US");

        assertEquals("Edited Steve", api.getRaw(null, "plugin.welcome", Map.of("player", "Steve")));
        assertEquals("Bye Steve", api.getRaw(null, "plugin.goodbye", Map.of("player", "Steve")));
        assertTrue(Files.readString(file).contains("plugin.goodbye"));
    }

    @Test
    void missingResourceIsSkipped() {
        api.registerMessages(addon, "de_DE");

        assertFalse(Files.exists(addonFolder.resolve("lang/de_DE.json")));
    }

    @Test
    void sendsMessageToSender() {
        api.registerMessages(addon, "en_US");
        CommandSender sender = mock(CommandSender.class);

        api.send(sender, "plugin.welcome", Map.of("player", "Alex"));

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(sender).sendMessage(captor.capture());
        assertEquals("[EssC] Welcome, Alex!", plain(captor.getValue()));
    }

    @Test
    void registerRejectsNullPlugin() {
        assertThrows(IllegalArgumentException.class, () -> api.registerMessages(null, "en_US"));
    }
}
