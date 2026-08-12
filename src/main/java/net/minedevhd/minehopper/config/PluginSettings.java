package net.minedevhd.minehopper.config;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public record PluginSettings(Set<String> enabledWorlds, boolean autoRegisterOnPlace, int defaultTransferAmount,
                             int defaultCollectRadius, boolean defaultFastTick, int normalIntervalTicks,
                             int fastIntervalTicks, int maxCollectRadius, int maxTargetsPerHopper,
                             int visualizerDurationSeconds, int visualizerPeriodTicks) {
    public PluginSettings {
        enabledWorlds = Set.copyOf(enabledWorlds);
        defaultTransferAmount = clamp(defaultTransferAmount, 1, 64);
        maxCollectRadius = clamp(maxCollectRadius, 0, 32);
        defaultCollectRadius = clamp(defaultCollectRadius, 0, maxCollectRadius);
        normalIntervalTicks = clamp(normalIntervalTicks, 1, 1200);
        fastIntervalTicks = clamp(fastIntervalTicks, 1, 1200);
        maxTargetsPerHopper = clamp(maxTargetsPerHopper, 1, 32);
        visualizerDurationSeconds = clamp(visualizerDurationSeconds, 1, 120);
        visualizerPeriodTicks = clamp(visualizerPeriodTicks, 1, 40);
    }

    public static PluginSettings load(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        Set<String> worlds = new HashSet<>();
        for (String world : config.getStringList("enabled-worlds")) {
            if (world != null && !world.isBlank()) worlds.add(world.toLowerCase(Locale.ROOT));
        }
        return new PluginSettings(worlds, config.getBoolean("auto-register-on-place", false),
                config.getInt("defaults.transfer-amount", 12), config.getInt("defaults.collect-radius", 1),
                config.getBoolean("defaults.fast-tick", false), config.getInt("intervals.normal-ticks", 8),
                config.getInt("intervals.fast-ticks", 1), config.getInt("limits.max-collect-radius", 15),
                config.getInt("limits.max-targets-per-hopper", 8), config.getInt("visualizer.duration-seconds", 30),
                config.getInt("visualizer.period-ticks", 10));
    }

    public boolean isWorldEnabled(World world) {
        if (world == null) return false;
        if (enabledWorlds.isEmpty() || enabledWorlds.contains("*")) return true;
        return enabledWorlds.contains(world.getName().toLowerCase(Locale.ROOT));
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
