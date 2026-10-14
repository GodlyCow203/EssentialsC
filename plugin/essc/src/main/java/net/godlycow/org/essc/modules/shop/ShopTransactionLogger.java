package net.godlycow.org.essc.modules.shop;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.integration.discord.DiscordSRVHook;
import net.godlycow.org.essc.plugin.config.EssConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ShopTransactionLogger {


    private final EssentialsC plugin;
    private final File logFolder;
    private final ExecutorService fileWriter = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Essc-ShopLog");
        thread.setDaemon(true);
        return thread;
    });


    public ShopTransactionLogger(EssentialsC plugin) {
        this.plugin = plugin;
        this.logFolder = new File(plugin.getDataFolder(), "logs" + File.separator + "shop");
    }

    public void log(ShopTransaction transaction) {

        if (transaction.entries().isEmpty()) {
            return;
        }

        EssConfig config = plugin.getConfigManager();
        String line = formatLine(transaction);

        if (config.isShopTransactionConsoleLog()) {
            plugin.getLogger().info("[Shop] " + line);
        }

        if (config.isShopTransactionFileLog()) {
            LocalDateTime now = LocalDateTime.now();
            String fileName = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";
            fileWriter.execute(() -> writeLine(fileName, "[" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] " + line));
        }

        if (transaction.total() < config.getShopTransactionDiscordMinTotal()) {
            return;
        }

        DiscordSRVHook hook = plugin.getDiscordSRVHook();
        if (hook != null && hook.isHooked()) {
            hook.sendShopTransactionEmbed(transaction);
        }
    }

    private String formatLine(ShopTransaction transaction)
    {
        StringBuilder items = new StringBuilder();
        for (ShopTransaction.Entry entry : transaction.mergedEntries())
        {

            if (!items.isEmpty()) items.append(", ");
            items.append(entry.amount()).append("x ").append(entry.itemId())
                    .append(" (").append(formatPrice(entry.price())).append(')');

        }

        return String.format(Locale.US, "%-4s %s (%s) via %s: %s | total %s",
                transaction.type(),
                transaction.playerName(),
                transaction.playerId(),
                transaction.source().id(),
                items,
                formatPrice(transaction.total()));
    }

    private String formatPrice(double price) {
        return String.format(Locale.US, "%.2f", price);
    }

    private void writeLine(String fileName, String line) {
        try {
            Files.createDirectories(logFolder.toPath());
            try (BufferedWriter writer = Files.newBufferedWriter(new File(logFolder, fileName).toPath(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND))
            {
                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e)
        {
            plugin.getLogger().warning("Failed to write shop transaction log: " + e.getMessage());
        }
    }

    public void shutdown() {
        fileWriter.shutdown();
        try
        {
            if (!fileWriter.awaitTermination(5, TimeUnit.SECONDS)) {
                fileWriter.shutdownNow();
            }

        }
        catch (InterruptedException e) {
            fileWriter.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
