package net.minedevhd.minehopper.listeners;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import net.minedevhd.minehopper.config.JsonConfig;

public class BlockBreakListener implements Listener {
	
	@EventHandler
	public void onEvent(BlockBreakEvent event) {
		Player player = event.getPlayer();
		
		if(player.getWorld().getName().equals("PW01") || player.getGameMode().equals(GameMode.CREATIVE)) {
			String loc = JsonConfig.compLocation(event.getBlock().getLocation());
			
			if(JsonConfig.exists("minehopper." + loc)) {
//				String[] pos = loc.split(";");
				JsonConfig.delete("minehopper." + loc);
				event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.SMOKE, 4);

//				if(JsonConfig.exists("minehopper." + loc))
//					player.sendMessage(MessageManager.getPrefix() + "§4Fehler! §cDer Trichter bei §6X§8: §e" + pos[0] + "§8, §6Y§8: §e" + pos[1] + "§8, §6Z§8: §e" + pos[2] + " §cwurde nicht gelöscht.");
//				else
//					player.sendMessage(MessageManager.getPrefix() + "§7Der Trichter bei §6X§8: §e" + pos[0] + "§8, §6Y§8: §e" + pos[1] + "§8, §6Z§8: §e" + pos[2] + " §7wurde gelöscht.");
			}
		}
	}

}
