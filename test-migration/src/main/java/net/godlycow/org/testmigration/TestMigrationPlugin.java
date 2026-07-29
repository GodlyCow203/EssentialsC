package net.godlycow.org.testmigration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestMigrationPlugin extends JavaPlugin {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File oldSnapshotFile;
    private File newSnapshotFile;
    private File dbFile;

    @Override
    public void onEnable() {
        oldSnapshotFile = new File(getDataFolder(), "old-snapshot.json");
        newSnapshotFile = new File(getDataFolder(), "new-snapshot.json");
        dbFile = new File(getDataFolder().getParentFile(), "EssentialsC/databases/users.db");
        getDataFolder().mkdirs();
        getLogger().info("Migration test tool enabled.");
        getLogger().info("Looking for EssentialsC database at: " + dbFile.getAbsolutePath());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("migration")) return false;

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /migration test save <old|new>  /migration result");
            return true;
        }

        if (args[0].equalsIgnoreCase("test") && args.length >= 3 && args[1].equalsIgnoreCase("save")) {
            return handleSave(sender, args[2]);
        }

        if (args[0].equalsIgnoreCase("result")) {
            return handleResult(sender);
        }

        sender.sendMessage("§cUsage: /migration test save <old|new> | /migration result");
        return true;
    }

    private boolean handleSave(CommandSender sender, String label) {
        if (!dbFile.exists()) {
            sender.sendMessage("EssentialsC database not found at: " + dbFile.getAbsolutePath());
            sender.sendMessage("Make sure EssentialsC has run on this server at least once.");
            return true;
        }

        File target = label.equalsIgnoreCase("old") ? oldSnapshotFile : newSnapshotFile;

        sender.sendMessage("MigrationTest: Snapshotting database (" +  label + ") ...............");

        try {
            DbSnapshot snapshot = captureSnapshot();
            String json = gson.toJson(snapshot);
            try (FileWriter w = new FileWriter(target)) {
                w.write(json);
            }
            sender.sendMessage("[MigrationTest] Snapshot saved to" + target.getName()
                    + " (" + snapshot.users.size() + " users, "
                    + snapshot.ignoredEntries.size() + " ignored, "
                    + snapshot.ipEntries.size() + " IP entries, "
                    + snapshot.inventories.size() + " inventories)");
        } catch (Exception e) {

            sender.sendMessage("[MigrationTest] Snapshot failed: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    private boolean handleResult(CommandSender sender) {
        if (!oldSnapshotFile.exists()) {
            sender.sendMessage("No old snapshot found");
            return true;
        }
        if (!newSnapshotFile.exists()) {
            sender.sendMessage("No new snapshot found");
            return true;
        }

        sender.sendMessage("[MigrationTest] Comparing snapshots .....");

        try {
            DbSnapshot oldSnap = gson.fromJson(new FileReader(oldSnapshotFile), DbSnapshot.class);
            DbSnapshot newSnap = gson.fromJson(new FileReader(newSnapshotFile), DbSnapshot.class);

            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            //compase user count
            if (oldSnap.users.size() != newSnap.users.size()) {
                errors.add("User count mismatch: old=" + oldSnap.users.size() + " new=" + newSnap.users.size());
            }

            //ignnored entries
            if (oldSnap.ignoredEntries.size() != newSnap.ignoredEntries.size()) {
                warnings.add("Ignored player count mismatch old=" + oldSnap.ignoredEntries.size() + " new=" + newSnap.ignoredEntries.size());
            }

            //ip entries
            if (oldSnap.ipEntries.size() != newSnap.ipEntries.size()) {
                warnings.add("IP history entry count mismatch old=" + oldSnap.ipEntries.size() + " new=" + newSnap.ipEntries.size());
            }

            //inv
            if (oldSnap.inventories.size() != newSnap.inventories.size()) {
                warnings.add("Inventory count mismatch  old=" + oldSnap.inventories.size() + " new=" + newSnap.inventories.size());
            }

            //fields that are expected to change with time (not data corruption)
            Set<String> timeFields = Set.of("last_join_time", "updated_at", "logout_time");

            //compare field by field
            Set<String> allUuids = new HashSet<>(oldSnap.users.keySet());
            allUuids.addAll(newSnap.users.keySet());

            for (String uuid : allUuids) {
                Map<String, Object> oldUser = oldSnap.users.get(uuid);
                Map<String, Object> newUser = newSnap.users.get(uuid);

                if (oldUser == null) {
                    errors.add("User " + uuid + " exists in new snapshot but NOT in old (data created during migration?)");
                    continue;
                }
                if (newUser == null) {
                    errors.add("User " + uuid + " exists in old snapshot but MISSING from new snapshot -DATA LOST!");
                    continue;
                }

                //compare old fields
                for (Map.Entry<String, Object> field : oldUser.entrySet()) {
                    String key = field.getKey();
                    Object oldVal = field.getValue();
                    Object newVal = newUser.get(key);

                    //excpected
                    if (timeFields.contains(key))
                        continue;

                    if (oldVal == null && newVal == null)
                        continue;

                    if (oldVal == null || newVal == null) {
                        if (oldVal == null)
                            continue;
                        warnings.add("User" + uuid + " field '" + key + "' became null: old=" + quote(oldVal) + " new=null");

                        continue;
                    }

                    if (!Objects.equals(oldVal, newVal)) {
                        errors.add("User " + uuid + " field '" + key + "' differs: old=" + quote(oldVal) + " new=" + quote(newVal));
                    }
                }

                //check for extra fields in new
                for (String key : newUser.keySet()) {
                    if (!oldUser.containsKey(key)) {
                        Object val = newUser.get(key);
                        if (val != null) {
                            warnings.add("User " + uuid + " has NEW field '" + key + "' = " + quote(val) + " (was absent in old snapshot,prob null before)");
                        }
                    }
                }
            }

            //compare ignored players
            for (Map.Entry<String, List<String>> entry : oldSnap.ignoredEntries.entrySet()) {
                List<String> newIgnored = newSnap.ignoredEntries.get(entry.getKey());
                if (newIgnored == null) {
                    errors.add("Ignored players for " + entry.getKey() + " MISSING from new snapshot");

                    continue;
                }
                Set<String> oldSet = new HashSet<>(entry.getValue());
                Set<String> newSet = new HashSet<>(newIgnored);

                if (!oldSet.equals(newSet)) {
                    Set<String> missing = new HashSet<>(oldSet);
                    missing.removeAll(newSet);
                    Set<String> extra = new HashSet<>(newSet);
                    extra.removeAll(oldSet);
                    if (!missing.isEmpty())
                        errors.add("Ignored players for " + entry.getKey() + " missing: " + missing);
                    if (!extra.isEmpty())
                        warnings.add("Ignored players for " + entry.getKey() + " extra: " + extra);
                }
            }

            //compare ip history
            for (Map.Entry<String, List<String>> entry : oldSnap.ipEntries.entrySet()) {
                List<String> newIps = newSnap.ipEntries.get(entry.getKey());
                if (newIps == null) {
                    warnings.add("IP history for " + entry.getKey() + " MISSING from new snapshot");
                    continue;
                }
                //ip history can grow. only check old values
                for (String ip : entry.getValue()) {
                    if (!newIps.contains(ip)) {
                        warnings.add("IP " + ip + " for " + entry.getKey() + " missing from new snapshot");
                    }
                }
            }

            // inv
            for (Map.Entry<String, String> entry : oldSnap.inventories.entrySet()) {
                String newInv = newSnap.inventories.get(entry.getKey());
                if (newInv == null) {
                    errors.add("Inventory for " + entry.getKey() + " MISSING from new snapshot - DATA LOST!");
                } else if (!entry.getValue().equals(newInv)) {
                    errors.add("Inventory for " + entry.getKey() + " CONTENT MISMATCH (data corrupted !!!!!!!!!)");
                }
            }

            //send results
            if (errors.isEmpty() && warnings.isEmpty()) {
                sender.sendMessage(" MIGRATION VERIFIED: No data loss or corruption detected!");
                sender.sendMessage("All " + oldSnap.users.size() + " users, "
                        + oldSnap.ignoredEntries.size() + " ignored players, "
                        + oldSnap.ipEntries.size() + " IP entries, "
                        + oldSnap.inventories.size() + " inventories intact.");
            } else {
                if (!errors.isEmpty()) {
                    sender.sendMessage("" + errors.size() + " ERROR(S) FOUND:");
                    for (String err : errors) {
                        sender.sendMessage("  - " + err);
                    }
                }
                if (!warnings.isEmpty()) {
                    sender.sendMessage(" " + warnings.size() + " WARNING(S):");
                    for (String w : warnings) {
                        sender.sendMessage("  - " + w);
                    }
                }
                if (errors.isEmpty()) {
                    sender.sendMessage("No  data loss detected (warnings only)");
                }
            }

        } catch (Exception e) {
            sender.sendMessage("[MigrationTest] Comparison failed: " + e.getMessage());
            StringBuilder trace = new StringBuilder();
            for (StackTraceElement el : e.getStackTrace()) {
                trace.append(el.toString()).append("\n");
                if (trace.length() > 2000) break;
            }
            sender.sendMessage("Stacktrace: " + trace);
            e.printStackTrace();
        }
        return true;
    }

    private DbSnapshot captureSnapshot() throws Exception {
        DbSnapshot snap = new DbSnapshot();
        snap.capturedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
             Statement stmt = conn.createStatement()) {

            //schema version
            try (ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(version), 0) FROM _schema_version")) {
                if (rs.next())
                    snap.schemaVersion = rs.getInt(1);
            } catch (Exception ignored) {}

            //users
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM users ORDER BY uuid")) {
                while (rs.next()) {
                    Map<String, Object> user = new LinkedHashMap<>();
                    user.put("uuid", rs.getString("uuid"));
                    user.put("username",  rs.getString("username"));
                    user.put("last_known_name", rs.getString("last_known_name"));
                    user.put("first_join_time", rs.getLong("first_join_time"));
                    user.put("last_join_time"  , rs.getLong("last_join_time"));
                    user.put("last_ip", rs.getString("last_ip"));
                    user.put("logout_location", rs.getString("logout_location"));
                    user.put("logout_time", rs.getLong("logout_time"));
                    user.put("language_code", rs.getString("language_code"));
                    user.put("back_location", rs.getString("back_location"));
                    user.put("death_location", rs.getString("death_location"));
                    user.put("fly_enabled", rs.getBoolean("fly_enabled"));
                    user.put("vanished" , rs.getBoolean("vanished"));
                    user.put("tpa_blocked", rs.getBoolean("tpa_blocked"));
                    user.put("last_reply_target", rs.getString("last_reply_target"));
                    user.put("rtp_last_used", rs.getLong("rtp_last_used"));
                    user.put("spawn_last_teleport", rs.getLong("spawn_last_teleport"));
                    user.put("ban_reason", rs.getString("ban_reason"));
                    user.put("ban_banner", rs.getString("ban_banner"));
                    user.put("ban_time", rs.getLong("ban_time"));
                    user.put("ban_expires", rs.getLong("ban_expires"));
                    user.put("mute_reason",  rs.getString("mute_reason"));
                    user.put("mute_muter", rs.getString("mute_muter"));
                    user.put("mute_time", rs.getLong("mute_time"));
                    user.put("mute_expires", rs.getLong("mute_expires"));
                    user.put("mute_offline_notification", rs.getBoolean("mute_offline_notification"));
                    user.put("scoreboard_disabled", rs.getBoolean("scoreboard_disabled"));
                    user.put("rules_accepted", rs.getBoolean("rules_accepted"));
                    user.put("created_at", rs.getLong("created_at"));
                    user.put("updated_at", rs.getLong("updated_at"));

                    snap.users.put(rs.getString("uuid"), user);
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT uuid, ignored_uuid FROM user_ignored_players ORDER BY uuid, ignored_uuid")) {
                while (rs.next()) {
                    snap.ignoredEntries.computeIfAbsent(rs.getString("uuid"), k -> new ArrayList<>())
                            .add(rs.getString("ignored_uuid"));
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT uuid, ip FROM user_ip_history ORDER BY uuid, recorded_at")) {
                while (rs.next()) {
                    snap.ipEntries.computeIfAbsent(rs.getString("uuid"), k -> new ArrayList<>())
                            .add(rs.getString("ip"));
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT uuid, inventory_data FROM user_inventories ORDER BY uuid")) {
                while (rs.next()) {
                    snap.inventories.put(rs.getString("uuid"), rs.getString("inventory_data"));
                }
            }
        }

        return snap;
    }

    private String quote(Object val) {
        if (val == null)

            return "null";
        if (val instanceof String)

            return "\"" + val + "\"";


        return String.valueOf(val);
    }

    static class DbSnapshot {
        int schemaVersion;
        String capturedAt;
        Map<String, Map<String, Object>> users = new LinkedHashMap<>();
        Map<String, List<String>> ignoredEntries = new LinkedHashMap<>();
        Map<String, List<String>> ipEntries = new LinkedHashMap<>();
        Map<String, String> inventories = new LinkedHashMap<>();
    }
}
