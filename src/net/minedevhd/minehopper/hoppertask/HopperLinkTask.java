package net.minedevhd.minehopper.hoppertask;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.Item;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HopperLinkTask extends BukkitRunnable {

    private final boolean fastTick;

    public HopperLinkTask(boolean fastTick) {
        this.fastTick = fastTick;
    }

    @Override
    public void run() {
        for (LinkedHopper link : HopperManager.getAll()) {

            // ticktyp beachten
            if (link.isFastTick() != fastTick) continue;

            Hopper from = link.getSource();
            if (from == null) continue;

            Block fromBlock = from.getBlock();
            if (!fromBlock.getChunk().isLoaded()) fromBlock.getChunk().load();
            if (fromBlock.isBlockPowered() || fromBlock.isBlockIndirectlyPowered()) continue;

            Block toBlock = link.getLinkedBlock();
            if (toBlock == null) continue;
            if (!toBlock.getChunk().isLoaded()) toBlock.getChunk().load();

            Inventory fromInv = from.getInventory();
            boolean movedSomething = false;

            // =========================================================
            // 1. VON CONTAINER ÜBER DEM HOPPER ENTNEHMEN (plugingesteuert)
            // =========================================================
            Block above = fromBlock.getRelative(BlockFace.UP);
            BlockState aboveState = above.getState();
            if (aboveState instanceof InventoryHolder) {
                Inventory invAbove = ((InventoryHolder) aboveState).getInventory();
                int moveCap = link.getTeleportItems();

                for (int slot = 0; slot < invAbove.getSize(); slot++) {
                    ItemStack stack = invAbove.getItem(slot);
                    if (stack == null || stack.getType() == Material.AIR) continue;

                    int toMove = Math.min(stack.getAmount(), moveCap);
                    if (toMove <= 0) break;

                    ItemStack clone = stack.clone();
                    clone.setAmount(toMove);

                    HashMap<Integer, ItemStack> leftover = fromInv.addItem(clone);
                    int notPlaced = leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
                    int moved = toMove - notPlaced;

                    if (moved > 0) {
                        stack.setAmount(stack.getAmount() - moved);
                        if (stack.getAmount() <= 0) invAbove.setItem(slot, null);
                        movedSomething = true;
                    }

                    // nur ein slot pro tick (wie vanilla)
                    break;
                }
            }

            // =========================================================
            // 2. ITEMS AUS UMGEBUNG EINSUGEN (nicht direkt teleportieren)
            // =========================================================
            int collectRadius = link.getCollectRadius();
            if (collectRadius > 0) {
                Location center = fromBlock.getLocation().add(0.5, 0.5, 0.5);
                double r2 = collectRadius * collectRadius;

                List<Item> items = from.getWorld().getEntitiesByClass(Item.class).stream()
                        .filter(e -> e.isValid() && !e.isDead())
                        .filter(e -> e.getWorld().equals(center.getWorld()))
                        .filter(e -> e.getLocation().distanceSquared(center) <= r2)
                        .collect(Collectors.toList());

                Material filter = link.getItemFilter();

                for (Item entity : items) {
                    ItemStack stack = entity.getItemStack();
                    if (stack == null || stack.getType() == Material.AIR) continue;

                    // filter nur fürs einsaugen
                    if (filter != null && filter != Material.AIR && stack.getType() != filter) continue;

                    ItemStack toAdd = stack.clone();
                    HashMap<Integer, ItemStack> leftover = fromInv.addItem(toAdd);
                    int placed = toAdd.getAmount() - leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
                    if (placed <= 0) continue;

                    int remaining = stack.getAmount() - placed;
                    if (remaining > 0) {
                        stack.setAmount(remaining);
                        entity.setItemStack(stack);
                        entity.setVelocity(new Vector(0, 0, 0));
                    } else {
                        entity.remove();
                    }
                    movedSomething = true;
                    break; // wie vanilla: ein item pro tick
                }
            }

            // =========================================================
            // 3. TELEPORTIEREN – nur erster belegter slot im hopper
            // =========================================================
            Material targetType = toBlock.getType();
            if (!(targetType == Material.CHEST ||
                  targetType == Material.TRAPPED_CHEST ||
                  targetType == Material.FURNACE ||
                  targetType == Material.BURNING_FURNACE ||
                  targetType == Material.HOPPER ||
                  targetType == Material.DROPPER ||
                  targetType == Material.DISPENSER ||
                  targetType == Material.BREWING_STAND)) continue;

            BlockState targetState = toBlock.getState();
            if (!(targetState instanceof InventoryHolder)) continue;
            Inventory toInv = ((InventoryHolder) targetState).getInventory();

            int moveCap = link.getTeleportItems();
            for (int slot = 0; slot < fromInv.getSize(); slot++) {
                ItemStack item = fromInv.getItem(slot);
                if (item == null || item.getType() == Material.AIR) continue;

                int toMove = Math.min(item.getAmount(), moveCap);
                if (toMove <= 0) break;

                ItemStack clone = item.clone();
                clone.setAmount(toMove);

                HashMap<Integer, ItemStack> leftover = toInv.addItem(clone);
                int notPlaced = leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
                int moved = toMove - notPlaced;

                if (moved > 0) {
                    item.setAmount(item.getAmount() - moved);
                    if (item.getAmount() <= 0) fromInv.setItem(slot, null);
                    movedSomething = true;
                }
                break;
            }

            // =========================================================
            // 4. BLOCK-UPDATES – comparatoren / redstone
            // =========================================================
            if (movedSomething) {
            	HopperManager.triggerUpdate(fromBlock);
                HopperManager.triggerUpdate(toBlock);
            }
        }
    }

    public static void start(JavaPlugin plugin) {
        new HopperLinkTask(true).runTaskTimer(plugin, 1L, 1L);   // fastTick
        new HopperLinkTask(false).runTaskTimer(plugin, 20L, 60L); // slowTick
        Bukkit.getLogger().info("[MineHopper] HopperLinkTask gestartet.");
    }
}
