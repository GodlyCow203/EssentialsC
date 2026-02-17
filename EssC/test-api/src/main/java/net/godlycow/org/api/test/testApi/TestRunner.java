package net.godlycow.org.api.test.testApi;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.*;

public class TestRunner {
    private static final Map<String, Boolean> eventsFired = new ConcurrentHashMap<>();
    private static final List<String> testResults = new CopyOnWriteArrayList<>();

    private final ChatColor g = ChatColor.GRAY;
    private final ChatColor w = ChatColor.WHITE;
    private final ChatColor d = ChatColor.DARK_GRAY;
    private final ChatColor r = ChatColor.RED;
    private final ChatColor gr = ChatColor.GREEN;

    public static void markEventFired(String eventName) {
        eventsFired.put(eventName, true);
    }

    public static void reset() {
        eventsFired.clear();
        testResults.clear();
    }

    public static boolean wasEventFired(String eventName) {
        return eventsFired.getOrDefault(eventName, false);
    }

    public void reportResult(Player player, String testName, boolean passed, String details) {
        String status = passed ? gr + "PASS" : r + "FAIL";
        String msg = g + "[" + status + g + "] " + w + testName + g + " | " + d + details;
        player.sendMessage(msg);
        testResults.add(testName + ": " + (passed ? "PASS" : "FAIL"));
    }

    public void printSummary(Player player) {
        int passed = (int) testResults.stream().filter(s -> s.contains("PASS")).count();
        int total = testResults.size();

        player.sendMessage(g + "=== " + w + "Test Summary" + g + " ===");
        player.sendMessage(w + "Passed: " + gr + passed + g + "/" + d + total);

        if (passed == total) {
            player.sendMessage(gr + "All tests passed!");
        } else {
            player.sendMessage(r + "Some tests failed!");
        }
    }
}