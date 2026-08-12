package net.minedevhd.minehopper.config;

import org.bukkit.Material;

public class Setter {

    private final String path;

    public Setter(String location) {
        this.path = "minehopper." + location + ".";
    }

    public void setTeleportItems(int amount) {
        JsonConfig.save(path + "teleportItems", amount);
    }

    public void setFastTick(boolean fastTick) {
        JsonConfig.save(path + "fastTick", fastTick);
    }

    public void setMultipleConnections(boolean multiple) {
        JsonConfig.save(path + "multipleConnections", multiple);
    }

    public void setItemFilter(Material material) {
        JsonConfig.save(path + "itemFilter", material);
    }

    public void setCollectRadius(int radius) {
        JsonConfig.save(path + "collectRadius", radius);
    }

    public void setLinkedHopper(String name) {
        JsonConfig.save(path + "linkedHopper", name);
    }
    
}
