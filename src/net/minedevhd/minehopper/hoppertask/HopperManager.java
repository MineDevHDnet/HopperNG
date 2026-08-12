package net.minedevhd.minehopper.hoppertask;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.minedevhd.minehopper.config.JsonConfig;
import net.minedevhd.minehopper.utils.MessageManager;

public final class HopperManager {

	private static HashMap<UUID, String> HOPPER_LINK_TASK = new HashMap<UUID, String>();
    public static final Map<String, LinkedHopper> LINKED_HOPPERS = new HashMap<>();
//    private static Thread watcherThread;
//    private static boolean running = false;

    public static Collection<LinkedHopper> getAll() {
        return LINKED_HOPPERS.values();
    }

    public static void loadAllFromFile() {
        if (!JsonConfig.exists("minehopper")) {
            Bukkit.getLogger().info("[MineHopper] Keine gespeicherten Hopper gefunden.");
            return;
        }

//        int count = 0;

        for (String key : JsonConfig.getSectionKeys("minehopper")) {
            if ("settings".equalsIgnoreCase(key)) continue;

            try {
                String[] coords = key.split(";");
                if (coords.length != 3) continue;

                double x = Double.parseDouble(coords[0]);
                double y = Double.parseDouble(coords[1]);
                double z = Double.parseDouble(coords[2]);

                World world = Bukkit.getWorld("world");
                if (world == null) continue;

                Block srcBlock = world.getBlockAt((int) x, (int) y, (int) z);
                if (srcBlock.getType() != Material.HOPPER) continue;
                Hopper src = (Hopper) srcBlock.getState();

                String linkedStr = JsonConfig.get("minehopper." + key + ".linkedHopper", String.class);
                Block linkedBlock = null;
                if (linkedStr != null && !linkedStr.equalsIgnoreCase("unknown")) {
                    String[] p = linkedStr.split(";");
                    if (p.length == 3) {
                        int lx = Integer.parseInt(p[0]);
                        int ly = Integer.parseInt(p[1]);
                        int lz = Integer.parseInt(p[2]);
                        linkedBlock = world.getBlockAt(lx, ly, lz);
                    }
                }

                int teleportItems = JsonConfig.get("minehopper." + key + ".teleportItems", Integer.class);
                boolean fastTick = JsonConfig.get("minehopper." + key + ".fastTick", Boolean.class);
                boolean multipleConnections = JsonConfig.get("minehopper." + key + ".multipleConnections", Boolean.class);
                int collectRadius = JsonConfig.get("minehopper." + key + ".collectRadius", Integer.class);
                String matName = JsonConfig.get("minehopper." + key + ".itemFilter", String.class);

                Material filter = (matName != null) ? Material.matchMaterial(matName) : Material.AIR;
                if (filter == null) filter = Material.AIR;

                LinkedHopper link = new LinkedHopper(
                        key, src, teleportItems, fastTick, multipleConnections, filter, collectRadius, null);
                link.setLinkedBlock(linkedBlock);

                LINKED_HOPPERS.put(key, link);
//                count++;

            } catch (Exception ex) {
                Bukkit.getLogger().warning("[MineHopper] Fehler beim Laden des Hoppers " + key + ": " + ex.getMessage());
            }
        }

//        Bukkit.getLogger().info("[MineHopper] Geladene Links: " + count);
    }

    /** Verlinkt zwei Hopper dauerhaft (in JsonConfig gespeichert) */
    public static void linkHoppers(Player player, Location targetLoc) {
        if (targetLoc == null || Bukkit.getWorld(targetLoc.getWorld().getName()) == null) return;
        
        String fromName = HopperManager.HOPPER_LINK_TASK.get(player.getUniqueId());
        String[] parts = fromName.split(";");
        
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);
        
        Block block = new Location(player.getWorld(), x, y, z).getBlock();
        
        if(block.getType() != Material.HOPPER) {
        	HopperManager.removeLinkList(player.getUniqueId());
        	targetLoc.getWorld().playEffect(targetLoc.add(0, 1, 0), Effect.SMOKE, 4);
        	player.sendMessage(MessageManager.getPrefix() + "§cDer Trichter bei §6" + JsonConfig.compLocation(block.getLocation()).replace(";", "§8, §6") + " §cwurde zerstört.");
        	player.playSound(player.getLocation(), Sound.BAT_DEATH, 10f, 10f);
        	return;
        }
        
        if(block.getLocation().equals(targetLoc)) {
        	HopperManager.removeLinkList(player.getUniqueId());
			block.getWorld().playEffect(block.getLocation().add(0, 1, 0), Effect.SMOKE, 4);
        	player.playSound(player.getLocation(), Sound.BAT_DEATH, 10f, 10f);
        	return;
        }
        
        JsonConfig.save("minehopper." + fromName + ".linkedHopper", JsonConfig.compLocation(targetLoc));
        loadAllFromFile();
        
        HopperManager.removeLinkList(player.getUniqueId());
        if(JsonConfig.get("minehopper." + fromName + ".linkedHopper", String.class) != null && 
           !JsonConfig.get("minehopper." + fromName + ".linkedHopper", String.class).equals("unknown")) {
//        	player.sendMessage(MessageManager.getPrefix() + "§aDer Trichter bei §e" + fromName.replace(";", "§8, §e") + "§a ist jetzt mit dem bei §e" + JsonConfig.compLocation(targetLoc).replace(";", "§8, §e") + "§a verbunden.");
        	block.getWorld().playEffect(block.getLocation().add(0, 1, 0), Effect.INSTANT_SPELL, 4);
        	player.playSound(player.getLocation(), Sound.LEVEL_UP, 10f, 10f);
        }
        else {
        	player.sendMessage(MessageManager.getPrefix() + "§cDer Trichter §6" + fromName.replace(";", "§8, §6") + "§c konnte nicht mit dem bei §6" + JsonConfig.compLocation(targetLoc).replace(";", "§8, §e") + "§c verbunden werden.");
        	targetLoc.getWorld().playEffect(targetLoc.add(0, 1, 0), Effect.SMOKE, 4);
        	player.playSound(player.getLocation(), Sound.BAT_DEATH, 10f, 10f);
        }
    }

    /** Startet automatisches Beobachten der JSON-Datei */
//    public static void startWatching(JavaPlugin plugin) {
//        if (running) return;
//        running = true;
//
//        File file = JsonConfig.getFile();
//        if (file == null || !file.exists()) return;
//
//        watcherThread = new Thread(() -> {
//            try {
//                WatchService watchService = FileSystems.getDefault().newWatchService();
//                Path dir = file.getParentFile().toPath();
//                dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
//
//                while (running) {
//                    try {
//                        WatchKey key = watchService.take();
//                        for (WatchEvent<?> event : key.pollEvents()) {
//                            Path changed = (Path) event.context();
//                            if (changed.getFileName().toString().equals(file.getName())) {
//                                Bukkit.getScheduler().runTask(plugin, HopperManager::loadAllFromFile);
//                            }
//                        }
//                        key.reset();
//                    } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); break; }
//                }
//            } catch (Exception e) { e.printStackTrace(); }
//        }, "MineHopper-FileWatcher");
//
//        watcherThread.setDaemon(true);
//        watcherThread.start();
//    }

    /** Stoppt den Datei-§berwacher */
//    public static void stopWatching() {
//        running = false;
//        if (watcherThread != null && watcherThread.isAlive()) {
//            watcherThread.interrupt();
//        }
//    }

    /** Sendet Nachricht an alle Online-Admins */
//	private static void notifyAdmins(String msg) {
//        for (Player p : Bukkit.getOnlinePlayers()) {
//            if (p.hasPermission("minehopper.admin")) p.sendMessage(msg);
//        }
//    }
    
    public static Map<UUID, String> getHopperLinkTask() { return HopperManager.HOPPER_LINK_TASK; }
    public static boolean isLinkList(UUID uuid) { return HopperManager.HOPPER_LINK_TASK.containsKey(uuid); }
	public static void removeLinkList(UUID uuid) { if(isLinkList(uuid)) HopperManager.HOPPER_LINK_TASK.remove(uuid); }
	
	public static void addLinkList(UUID uuid, String locationName) {
		removeLinkList(uuid);
		HopperManager.HOPPER_LINK_TASK.put(uuid, locationName);
	}
    
    public static String removePrefix(String input) {
        if (input == null) return null;
        int index = input.indexOf(';');
        return index == -1 ? input : input.substring(index + 1);
    }
    
    public static String getLocationFromInventory(Inventory inv) {
        if (inv == null) return null;
        if (inv.getSize() <= 13) return null;

        ItemStack item = inv.getItem(13);
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return null;

        for (String line : meta.getLore()) {
            if (line.startsWith("§7Position: §e")) {
                return line.replace("§7Position: §e", "").trim();
            }
        }
        return null;
    }
    
    public static String formatMaterialName(Material material) {
        String name = material.name().toLowerCase();
        String[] parts = name.split("_");
        StringBuilder builder = new StringBuilder();
        
        for (String part : parts) {
            builder.append(Character.toUpperCase(part.charAt(0)))
                   .append(part.substring(1))
                   .append(" ");
        }
        return builder.toString().trim();
    }
    
    public static void triggerUpdate(Block b) {
        // eigenen Block aktualisieren
        BlockState s = b.getState();
        s.update(true, true);

        // Nachbarn „anstupsen“, damit Comparatoren neu berechnen
        World w = b.getWorld();
        int x = b.getX(), y = b.getY(), z = b.getZ();

        updateBlock(w, x + 1, y, z);
        updateBlock(w, x - 1, y, z);
        updateBlock(w, x, y + 1, z);
        updateBlock(w, x, y - 1, z);
        updateBlock(w, x, y, z + 1);
        updateBlock(w, x, y, z - 1);
    }

    private static void updateBlock(World w, int x, int y, int z) {
        BlockState bs = w.getBlockAt(x, y, z).getState();
        bs.update(true, true);
    }
    
}