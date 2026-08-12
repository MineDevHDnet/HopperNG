package net.minedevhd.minehopper.listeners;

import net.minedevhd.minehopper.hoppertask.HopperManager;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.InventoryHolder;

public class BlockHopperTickListener implements Listener {

    private boolean isLinked(Hopper hopper) {
        if (hopper == null) return false;
        Block hb = hopper.getBlock();
        return HopperManager.LINKED_HOPPERS.values().stream()
                .anyMatch(link -> link.getSource() != null
                        && link.getSource().getBlock().equals(hb));
    }

    // Verhindert, dass verlinkte Hopper Items zwischen Inventaren bewegen (ziehen/pushen)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(InventoryMoveItemEvent e) {
        InventoryHolder init = e.getInitiator() != null ? e.getInitiator().getHolder() : null;
        InventoryHolder src  = e.getSource()     != null ? e.getSource().getHolder()     : null;
        InventoryHolder dst  = e.getDestination()!= null ? e.getDestination().getHolder(): null;

        boolean linkedInitiator = init instanceof Hopper && isLinked((Hopper) init);
        boolean linkedSource    = src  instanceof Hopper && isLinked((Hopper) src);
        boolean linkedDest      = dst  instanceof Hopper && isLinked((Hopper) dst);

        // Blockiere Vanilla nur für verlinkte Hopper (egal ob Initiator, Quelle oder Ziel-Hopper)
        if (linkedInitiator || linkedSource || linkedDest) {
            e.setCancelled(true);
        }
    }

    // Verhindert, dass verlinkte Hopper frei liegende Items einsaugen
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(InventoryPickupItemEvent e) {
        InventoryHolder holder = e.getInventory() != null ? e.getInventory().getHolder() : null;
        if (holder instanceof Hopper && isLinked((Hopper) holder)) {
            e.setCancelled(true);
        }
    }
}
