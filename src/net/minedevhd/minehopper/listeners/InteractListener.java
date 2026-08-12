package net.minedevhd.minehopper.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.minedevhd.minehopper.config.JsonConfig;
import net.minedevhd.minehopper.hoppertask.HopperManager;
import net.minedevhd.minehopper.inventory.InventoryBuilder;

public class InteractListener implements Listener {
	
	@EventHandler
	public void onEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		
		if(player.getWorld().getName().equals("PW01") || player.getGameMode().equals(GameMode.CREATIVE)) {
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if(HopperManager.isLinkList(player.getUniqueId())) {
					if(event.getClickedBlock().getType().equals(Material.CHEST) ||
	   				   event.getClickedBlock().getType().equals(Material.TRAPPED_CHEST) ||
	   				   event.getClickedBlock().getType().equals(Material.FURNACE) ||
	   				   event.getClickedBlock().getType().equals(Material.HOPPER) ||
	   				   event.getClickedBlock().getType().equals(Material.DROPPER) ||
	   				   event.getClickedBlock().getType().equals(Material.DISPENSER) ||
	   				   event.getClickedBlock().getType().equals(Material.BREWING_STAND)) {
						
						HopperManager.linkHoppers(event.getPlayer(), event.getClickedBlock().getLocation());
						event.setCancelled(true);
					}
				}
				
				if(event.getClickedBlock().getType().equals(Material.HOPPER)
				&& (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR))
				&& player.isSneaking()) {
					event.setCancelled(true);
					String loc = JsonConfig.compLocation(event.getClickedBlock().getLocation());
					
					if(!JsonConfig.exists("minehopper." + loc)) 
						InventoryBuilder.createDefaultHopper(event.getClickedBlock().getLocation(), 12, false, false, Material.AIR, 1, "unknown");
					
					String locationName = JsonConfig.compLocation(event.getClickedBlock().getLocation());
					InventoryBuilder.reloadHopperInventory(player, locationName);
//					player.sendMessage(MessageManager.getPrefix() + "§7Du schaust jetzt in den Trichter bei §e" + loc.replace(";", "§8, §e") + "§7.");
				}
			}
		}
	}
	
}
