package net.minedevhd.minehopper.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import net.minedevhd.minehopper.inventory.InventoryBuilder;

public class BlockPlaceListener implements Listener {
	
	@EventHandler
	public void onEvent(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		
		if(player.getWorld().getName().equals("PW01") || player.getGameMode().equals(GameMode.CREATIVE)) {
			if(event.getBlock().getType().equals(Material.HOPPER)) {
				Location blockPos = event.getBlock().getLocation();
//				String loc = JsonConfig.compLocation(blockPos);
				
				InventoryBuilder.createDefaultHopper(blockPos, 12, false, false, Material.AIR, 1, "unknown");
				
//				String[] pos = loc.split(";");
//				player.sendMessage(MessageManager.getPrefix() + "§7Der Trichter bei §6X§8: §e" + pos[0] + "§8, §6Y§8: §e" + pos[1] + "§8, §6Z§8: §e" + pos[2] + " §7wurde gespeichert.");
			}
		}
	}

}
