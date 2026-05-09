package net.godlycow.org.essc.command.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.dump.DumpSectionCollector;
import net.godlycow.org.essc.dump.PasteUploadClient;
import net.godlycow.org.essc.util.EssLog;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DumpCommand extends Command {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private final DumpSectionCollector dumpSectionCollector;

    public DumpCommand(EssentialsC plugin) {
        super(plugin, "essc dump", "essc.dump", false);

        this.dumpSectionCollector = new DumpSectionCollector(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        List<String> sections = new ArrayList<>();

        for (String arg : args) {
            if (dumpSectionCollector.getSectionNames().contains(arg)) {
                sections.add(arg);
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dumpTimestamp", Instant.now().toEpochMilli());

        dumpSectionCollector.collect(sections, data);

        File dumpsDir = new File(plugin.getDataFolder(), "dumps");

        if (!dumpsDir.exists()) {
            dumpsDir.mkdirs();
        }

        String fileName =
                "essc_dump_" + Instant.now().toEpochMilli() + ".json";

        File dumpFile = new File(dumpsDir, fileName);

        try (FileWriter writer = new FileWriter(dumpFile)) {
            GSON.toJson(data, writer);

        } catch (IOException e) {
            sender.sendMessage(lang.get(sender, "dump.error.write"));
            EssLog.warn("Failed to write dump file: " + e.getMessage());
            return true;
        }

        sender.sendMessage(
                lang.get(sender, "dump.saved", Map.of("file", dumpFile.getPath()))
        );

        EssLog.info(
                "Dump saved to " + dumpFile.getPath() + " by " + sender.getName()
        );

        if (!sender.hasPermission("essc.dump.upload")) {
            return true;
        }

        sender.sendMessage(lang.get(sender, "dump.upload.starting"));

        plugin.getEssScheduler().runAsync(() -> {
            try {
                String viewerUrl = PasteUploadClient.upload(dumpFile);

                plugin.getEssScheduler().runGlobal(() -> {
                    sender.sendMessage(
                            lang.get(sender, "dump.upload.success", Map.of("url", viewerUrl))
                    );

                    EssLog.info("Dump upload link: " + viewerUrl);
                });

            } catch (IOException e) {
                String error =
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Unknown error";

                plugin.getEssScheduler().runGlobal(() ->
                        sender.sendMessage(
                                lang.get(sender, "dump.upload.error", Map.of("error", error))
                        )
                );

                EssLog.warn("Dump upload failed: " + error);
            }
        });

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length >= 1) {
            String partial = args[args.length - 1].toLowerCase();

            return dumpSectionCollector.getSectionNames().stream()
                    .filter(s -> s.toLowerCase().startsWith(partial))
                    .filter(s -> {
                        for (int i = 0; i < args.length - 1; i++) {
                            if (args[i].equalsIgnoreCase(s)) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .toList();
        }

        return List.of();
    }
}