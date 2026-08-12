package net.minedevhd.minehopper.listeners;

import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import net.minedevhd.minehopper.config.JsonConfig;

public class BlockExplodeListener implements Listener {
	
	@EventHandler
	public void onEvent(BlockExplodeEvent event) {
		String loc = JsonConfig.compLocation(event.getBlock().getLocation());
		
		for(Block block : event.blockList()) {
			if(JsonConfig.exists("minehopper." + loc)) {
				JsonConfig.delete("minehopper." + loc);
				block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4);
			}
		}
	}

}
