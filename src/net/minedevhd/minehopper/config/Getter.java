package net.minedevhd.minehopper.config;

import org.bukkit.Material;

public class Getter {

    private final String path;

    public Getter(String location) {
        this.path = "minehopper." + location + ".";
    }

    public int getTeleportItems() {
        Integer value = JsonConfig.get(path + "teleportItems", Integer.class);
        return value != null ? value : 12;
    }

    public boolean isFastTick() {
        Boolean value = JsonConfig.get(path + "fastTick", Boolean.class);
        return value != null && value;
    }

    public boolean isMultipleConnections() {
        Boolean value = JsonConfig.get(path + "multipleConnections", Boolean.class);
        return value != null && value;
    }

    public Material getItemFilter() {
        Material value = JsonConfig.get(path + "itemFilter", Material.class);
        if(value == Material.AIR) return Material.BARRIER;
        return value != null ? value : Material.BARRIER;
    }

    public int getCollectRadius() {
        Integer value = JsonConfig.get(path + "collectRadius", Integer.class);
        return value != null ? value : 1;
    }

    public String getLinkedHopper() {
        String value = JsonConfig.get(path + "linkedHopper", String.class);
        return value != null ? value : "unknown";
    }
    
    public boolean isLinked() {
    	return this.getLinkedHopper() != null && !this.getLinkedHopper().equals("unknown");
    }
    
}
