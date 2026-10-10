
package net.godlycow.org.essc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserStoreMigrationTest {

    private static final UUID PLAYER_ID =
            UUID.fromString("27f4de78-5870-3e35-9027-0d2e4506bc19");

    private EssentialsC createPlugin(Path dataFolder)
    {
        EssentialsC plugin = mock(EssentialsC.class);
        when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("USMT"));
        return plugin;
    }

    private void createV2Database(Path dbFile) throws Exception
    {
        dbFile.getParent().toFile().mkdirs();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                    CREATE TABLE _schema_version (
                        version INTEGER PRIMARY KEY
                    )
                    """);
            stmt.execute("INSERT INTO _schema_version (version) VALUES (2)");

            stmt.execute("""
                    CREATE TABLE users (
                        uuid TEXT PRIMARY KEY,
                        username TEXT NOT NULL,
                        last_known_name TEXT,
                        first_join_time INTEGER DEFAULT 0,
                        last_join_time INTEGER DEFAULT 0,
                        last_ip TEXT,
                        logout_location TEXT,
                        logout_time INTEGER DEFAULT 0,
                        language_code TEXT,
                        back_location TEXT,
                        death_location TEXT,
                        fly_enabled BOOLEAN DEFAULT FALSE,
                        vanished BOOLEAN DEFAULT FALSE,
                        tpa_blocked BOOLEAN DEFAULT FALSE,
                        last_reply_target TEXT,
                        rtp_last_used INTEGER DEFAULT 0,
                        spawn_last_teleport INTEGER DEFAULT 0,
                        ban_reason TEXT,
                        ban_banner TEXT,
                        ban_time INTEGER DEFAULT 0,
                        ban_expires INTEGER DEFAULT 0,
                        mute_reason TEXT,
                        mute_muter TEXT,
                        mute_time INTEGER DEFAULT 0,
                        mute_expires INTEGER DEFAULT 0,
                        mute_offline_notification BOOLEAN DEFAULT FALSE,
                        scoreboard_disabled BOOLEAN DEFAULT FALSE,
                        rules_accepted BOOLEAN DEFAULT FALSE,
                        created_at INTEGER DEFAULT 0,
                        updated_at INTEGER DEFAULT 0
                    )
                    """);

            try (PreparedStatement insert = conn.prepareStatement("""
                    INSERT INTO users (uuid, username, last_known_name, vanished, fly_enabled)
                    VALUES (?, ?, ?, ?, ?)
                    """)) {
                insert.setString(1, PLAYER_ID.toString());
                insert.setString(2, "Cow0990");
                insert.setString(3, "Cow0990");
                insert.setBoolean(4, true);
                insert.setBoolean(5, false);
                assertEquals(1, insert.executeUpdate());
            }
        }
    }

    private int getSchemaVersion(Path dbFile) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COALESCE(MAX(version), 0) FROM _schema_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Set<String> getUserColumns(Path dbFile) throws Exception
    {

        Set<String> columns = new HashSet<>();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(users)"))
        {

            while (rs.next())

            {
                columns.add(rs.getString("name"));
            }
        }

        return columns;
    }

    @Test
    void migratesV2DatabaseToV4(@TempDir Path dataFolder) throws Exception {
        Path dbFile = dataFolder.resolve("databases").resolve("users.db");
        createV2Database(dbFile);

        UserStore store = new UserStore(createPlugin(dataFolder));

        assertEquals(4, getSchemaVersion(dbFile));
        assertTrue(getUserColumns(dbFile).contains("frozen"));
        assertFalse(getUserColumns(dbFile).contains("rules_accepted"));

        UserProfile profile = store.findByUuid(PLAYER_ID);

        assertNotNull(profile);
        assertEquals("Cow0990", profile.getUsername());
        assertTrue(profile.isVanished());
        assertFalse(profile.isFlyEnabled());
        assertFalse(profile.isFrozen());

        profile.setFrozen(true);
        assertTrue(store.save(profile));
        assertTrue(store.findByUuid(PLAYER_ID).isFrozen());

        profile.setFrozen(false);
        assertTrue(store.save(profile));
        assertFalse(store.findByUuid(PLAYER_ID).isFrozen());

        UserStore reopened = new UserStore(createPlugin(dataFolder));

        assertEquals(4, getSchemaVersion(dbFile));

        UserProfile savedProfile = reopened.findByUuid(PLAYER_ID);
        assertNotNull(savedProfile);
        assertEquals("Cow0990", savedProfile.getUsername());
        assertTrue(savedProfile.isVanished());
        assertFalse(savedProfile.isFrozen());
    }

    @Test
    void newDatabaseUsesV4Schema(@TempDir Path dataFolder) {
        UserStore store = new UserStore(createPlugin(dataFolder));

        UUID uuid = UUID.randomUUID();
        UserProfile profile = UserProfile.createDefault(uuid, "FreshPlayer", 1L);

        assertFalse(profile.isFrozen());
        assertTrue(store.save(profile));

        UserProfile savedProfile = store.findByUuid(uuid);
        assertNotNull(savedProfile);
        assertFalse(savedProfile.isFrozen());
    }
}
