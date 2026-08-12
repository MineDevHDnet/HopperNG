package net.minedevhd.minehopper.hoppertask;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;

import net.minedevhd.minehopper.config.JsonConfig;

public class LinkedHopper {

    private final String name;
    private final Hopper source;
    private Block linkedBlock;
    private Material itemFilter;
    private int teleportItems;
    private int collectRadius;
    private boolean fastTick;
    private boolean multipleConnections;

    public LinkedHopper(String name,
                        Hopper source,
                        int teleportItems,
                        boolean fastTick,
                        boolean multipleConnections,
                        Material itemFilter,
                        int collectRadius,
                        Hopper linkedHopper) {
        this.name = name;
        this.source = source;
        this.teleportItems = teleportItems;
        this.fastTick = fastTick;
        this.multipleConnections = multipleConnections;
        this.itemFilter = itemFilter;
        this.collectRadius = collectRadius;
        this.linkedBlock = (linkedHopper != null) ? linkedHopper.getBlock() : null;
    }

    public String getName() { return name; }
    public Hopper getSource() { return source; }

    public Hopper getLinkedHopper() {
        if (linkedBlock != null && linkedBlock.getType() == Material.HOPPER)
            return (Hopper) linkedBlock.getState();
        return null;
    }
    
    public void updateFromConfig() {
        String base = "minehopper." + name + ".";

        // alle Werte aus JsonConfig neu abrufen
        int newTeleport = JsonConfig.get(base + "teleportItems", Integer.class);
        boolean newFastTick = JsonConfig.get(base + "fastTick", Boolean.class);
        int newCollectRadius = JsonConfig.get(base + "collectRadius", Integer.class);
        String matName = JsonConfig.get(base + "itemFilter", String.class);

        if (matName != null) {
            Material newFilter = Material.matchMaterial(matName);
            if (newFilter != null) this.itemFilter = newFilter;
        }

        this.teleportItems = newTeleport;
        this.fastTick = newFastTick;
        this.collectRadius = newCollectRadius;
    }

    public Block getLinkedBlock() { return linkedBlock; }
    public void setLinkedBlock(Block linkedBlock) { this.linkedBlock = linkedBlock; }

    public Material getItemFilter() { return itemFilter; }
    public void setItemFilter(Material itemFilter) { this.itemFilter = itemFilter; }

    public int getTeleportItems() { return teleportItems; }
    public void setTeleportItems(int teleportItems) { this.teleportItems = teleportItems; }

    public int getCollectRadius() { return collectRadius; }
    public void setCollectRadius(int collectRadius) { this.collectRadius = collectRadius; }

    public boolean isFastTick() { return fastTick; }
    public void setFastTick(boolean fastTick) { this.fastTick = fastTick; }

    public boolean isMultipleConnections() { return multipleConnections; }
    public void setMultipleConnections(boolean multipleConnections) { this.multipleConnections = multipleConnections; }
    
}
