package net.minedevhd.minehopper.persistence;

import net.minedevhd.minehopper.model.BlockKey;
import net.minedevhd.minehopper.model.FilterMode;
import net.minedevhd.minehopper.model.ManagedHopper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HopperRepository {
    private final JavaPlugin plugin;
    private final File file;
    public HopperRepository(JavaPlugin plugin) { this.plugin = plugin; this.file = new File(plugin.getDataFolder(), "hoppers.yml"); }

    public Map<BlockKey, ManagedHopper> loadAll() {
        Map<BlockKey, ManagedHopper> result = new LinkedHashMap<>();
        if (!file.exists()) return result;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("hoppers");
        if (root == null) return result;
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id); if (section == null) continue;
            try {
                BlockKey source = BlockKey.read(section.getConfigurationSection("source"));
                if (source == null) { plugin.getLogger().warning("Skipping invalid hopper entry " + id + ": source is missing."); continue; }
                int transfer = section.getInt("transfer-amount", 12);
                boolean fast = section.getBoolean("fast-tick", false);
                int radius = section.getInt("collect-radius", 1);
                FilterMode mode = FilterMode.parse(section.getString("filter.mode"));
                ItemStack filter = section.getItemStack("filter.item");
                List<BlockKey> targets = new ArrayList<>();
                ConfigurationSection targetSection = section.getConfigurationSection("targets");
                if (targetSection != null) for (String targetId : targetSection.getKeys(false)) {
                    BlockKey target = BlockKey.read(targetSection.getConfigurationSection(targetId));
                    if (target != null && !source.equals(target) && !targets.contains(target)) targets.add(target);
                }
                result.put(source, new ManagedHopper(source, transfer, fast, radius, mode, filter, targets));
            } catch (RuntimeException ex) { plugin.getLogger().warning("Skipping invalid hopper entry " + id + ": " + ex.getMessage()); }
        }
        return result;
    }

    public void saveAll(Collection<ManagedHopper> hoppers) {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().severe("Could not create plugin data directory: " + plugin.getDataFolder()); return;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        List<ManagedHopper> sorted = hoppers.stream().sorted((a,b) -> a.source().compareTo(b.source())).toList();
        int index = 0;
        for (ManagedHopper hopper : sorted) {
            ConfigurationSection section = yaml.createSection("hoppers." + index++);
            hopper.source().write(section.createSection("source"));
            section.set("transfer-amount", hopper.transferAmount());
            section.set("fast-tick", hopper.fastTick());
            section.set("collect-radius", hopper.collectRadius());
            section.set("filter.mode", hopper.filterMode().name());
            section.set("filter.item", hopper.filterItem());
            int targetIndex = 0;
            for (BlockKey target : hopper.targets()) target.write(section.createSection("targets." + targetIndex++));
        }
        try { yaml.save(file); }
        catch (IOException ex) { plugin.getLogger().severe("Could not save hoppers.yml: " + ex.getMessage()); }
    }
}
