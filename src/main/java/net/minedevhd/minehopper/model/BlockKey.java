package net.minedevhd.minehopper.model;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import java.util.Objects;

public record BlockKey(String world, int x, int y, int z) implements Comparable<BlockKey> {
    public BlockKey { Objects.requireNonNull(world, "world"); }
    public static BlockKey from(Block block) { return new BlockKey(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()); }
    public static BlockKey read(ConfigurationSection section) {
        if (section == null) return null;
        String world = section.getString("world");
        if (world == null || world.isBlank()) return null;
        return new BlockKey(world, section.getInt("x"), section.getInt("y"), section.getInt("z"));
    }
    public void write(ConfigurationSection section) {
        section.set("world", world); section.set("x", x); section.set("y", y); section.set("z", z);
    }
    public World resolveWorld() { return Bukkit.getWorld(world); }
    public boolean isChunkLoaded() {
        World resolved = resolveWorld();
        return resolved != null && resolved.isChunkLoaded(x >> 4, z >> 4);
    }
    public String display() { return world + " @ " + x + ", " + y + ", " + z; }
    @Override public int compareTo(BlockKey other) {
        int c = world.compareToIgnoreCase(other.world); if (c != 0) return c;
        c = Integer.compare(x, other.x); if (c != 0) return c;
        c = Integer.compare(y, other.y); if (c != 0) return c;
        return Integer.compare(z, other.z);
    }
}
