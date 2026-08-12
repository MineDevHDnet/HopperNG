package net.minedevhd.minehopper;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.minedevhd.minehopper.config.JsonConfig;
import net.minedevhd.minehopper.hoppertask.HopperLinkTask;
import net.minedevhd.minehopper.hoppertask.HopperManager;
import net.minedevhd.minehopper.listeners.BlockBreakListener;
import net.minedevhd.minehopper.listeners.BlockHopperTickListener;
import net.minedevhd.minehopper.listeners.BlockPlaceListener;
import net.minedevhd.minehopper.listeners.InteractListener;
import net.minedevhd.minehopper.listeners.InventoryClickListener;

public class MineHopper extends JavaPlugin {
	
	private static MineHopper INSTANCE;
	
	/**
	{
	    "minehopper": {
	        "settings": {
	            "prefix": "&8[&6MineHopper&8]&r ",
	            "worlds": [ 
	              "PW01",
	              "PW02"
	            ]
	        },
	        "hopper-coordiante0": {
	            "fastTick": false,
	            "teleportItems": 12,
	            "itemFilter": "unknown",
	            "collectRadius": 1,
	            "connections": {
	                "single_linked": "linked-hopper-coordiante1",
	                "multiple": [
	                    "%linked-hopper-coordiante2%:%minecraft-id2%",
	                    "%linked-hopper-coordiante3%:%minecraft-id3%"
	                ]
	            }
	        }
	    }
	}
	 * */
	
	@Override
	public void onLoad() {
		this.setInstance(this);
		JsonConfig.init(MineHopper.getInstance(), "config.json");
		super.onLoad();
	}
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new InteractListener(), this);
		Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(), this);
		Bukkit.getPluginManager().registerEvents(new BlockBreakListener(), this);
//		Bukkit.getPluginManager().registerEvents(new BlockExplodeListener(), this);
		Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), this);
		Bukkit.getPluginManager().registerEvents(new BlockHopperTickListener(), this);
		
		if(!JsonConfig.exists("settings.prefix")) JsonConfig.save("settings.prefix", "&8[&6MineHopper&8]&r ");
		
		HopperManager.loadAllFromFile();
//	    HopperManager.startWatching(MineHopper.getInstance());
	    HopperLinkTask.start(MineHopper.getInstance());
		super.onEnable();
	}
	
	@Override
	public void onDisable() {
//	    HopperManager.stopWatching();
		super.onDisable();
	}
	
	public static MineHopper getInstance() {
		return MineHopper.INSTANCE;
	}
	
	public void setInstance(MineHopper iNSTANCE) {
		MineHopper.INSTANCE = iNSTANCE;
	}
	
}
