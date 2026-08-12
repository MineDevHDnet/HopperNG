package net.minedevhd.minehopper.listeners;

import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.minedevhd.minehopper.config.Getter;
import net.minedevhd.minehopper.config.JsonConfig;
import net.minedevhd.minehopper.config.Setter;
import net.minedevhd.minehopper.hoppertask.HopperManager;
import net.minedevhd.minehopper.inventory.InventoryBuilder;
import net.minedevhd.minehopper.utils.MessageManager;

public class InventoryClickListener implements Listener {
	
	@EventHandler
	public void onEvent(InventoryClickEvent event) {
	    final Player player = (Player) event.getWhoClicked();

	    try {
	        if (player.getWorld().getName().equals("PW01") || player.getGameMode().equals(GameMode.CREATIVE)) {
	            if (event.getClickedInventory() != null && 
	                "§6Trichter-Einstellungen".equals(event.getClickedInventory().getName())) {
	                event.setCancelled(true);

	                String locationName = HopperManager.getLocationFromInventory(event.getClickedInventory());
	                Integer teleportItems = new Getter(locationName).getTeleportItems();
	                Boolean fasttick = new Getter(locationName).isFastTick();
	                Integer collectRadius = new Getter(locationName).getCollectRadius();

	                Setter setter = new Setter(locationName);
	                Getter getter = new Getter(locationName);

	                switch (event.getCurrentItem().getItemMeta().getDisplayName()) {
	                    case "§6Trichter":
	                        InventoryBuilder.reloadHopperInventory(player, locationName);
	                		player.sendMessage(MessageManager.getPrefix() + "§aDas Trichter-Inventar wurde neu geladen.");
	                        break;
	                    case "§6Anzahl der transportierten Items":
	                        if (teleportItems == 1) setter.setTeleportItems(12);
	                        if (teleportItems == 12) setter.setTeleportItems(64);
	                        if (teleportItems == 64) setter.setTeleportItems(1);

	                        String items = (getter.getTeleportItems() == 1 ? "1 Item" : getter.getTeleportItems() + " Items");
	                        player.sendMessage(MessageManager.getPrefix() + "§7Der Item-Transport wurde auf §e" + items + " §7geändert.");
	                        InventoryBuilder.reloadHopperInventory(player, locationName);
	                        break;
	                    case "§6Force Fast Tick":
	                        setter.setFastTick(!fasttick);
	                        player.sendMessage(MessageManager.getPrefix() + "§7Der Fast Tick wurde " + (getter.isFastTick() ? "§aaktiviert" : "§cdeaktiviert") + "§7.");
	                        InventoryBuilder.reloadHopperInventory(player, locationName);
	                        break;
	                    case "§6Mehrfach-Verbindungen":
	                    	InventoryBuilder.openMultiConnectionInventory(player, locationName);
	                        player.sendMessage(MessageManager.getPrefix() + "§cDiese Funktion steht noch nicht zur Verfügung.");
	                        break;
	                    case "§6Verbundene Trichter":
                    		if(event.isShiftClick()) {
                    			if(getter.isLinked()) {
//	                    			String oldHopper = getter.getLinkedHopper();
	                    			setter.setLinkedHopper("unknown");
	                    			
	                    			InventoryBuilder.reloadHopperInventory(player, locationName);
//	                    			player.sendMessage(MessageManager.getPrefix() + "§cDie Verbindung zum Trichter §6" + oldHopper + " §cwurde unterbrochen.");
	                            	player.playSound(player.getLocation(), Sound.BAT_DEATH, 10f, 10f);
	                    		}
	                    	} else {
	                    		final UUID uuid = player.getUniqueId();
	                        	
	                        	if(!HopperManager.isLinkList(uuid)) {
	                        		HopperManager.addLinkList(uuid, locationName);
	                        		player.closeInventory();
	                        		player.sendMessage(MessageManager.getPrefix() + "§7Die Verbindungs-Sequenz wurde gestartet, klicke auf einen Endpunkt.");
	                        	}
	                        	else {
	                        		HopperManager.removeLinkList(uuid);
	                        	}
	                    	}
	                        break;
	                    case "§6Trichter-Filter":
	                    	if(event.isShiftClick()) {
	                    		setter.setItemFilter(Material.AIR);
	                    		InventoryBuilder.reloadHopperInventory(player, locationName);
	                    		player.sendMessage(MessageManager.getPrefix() + "§7Der Itemfilter wurde bei §e" + locationName + " §7zurückgesetzt.");
                            	player.playSound(player.getLocation(), Sound.BAT_DEATH, 10f, 10f);
	                    	}
	                    	else {
	                    		InventoryBuilder.openFilterInventory(player, locationName);
	                    		player.sendMessage(MessageManager.getPrefix() + "§cDetallierte Einstellungen sind momentan nicht verfügbar.");
	                    	}
	                        break;
	                    case "§6Sammelradius §c§l-1":
	                        if (collectRadius > 0) {
	                            JsonConfig.save("minehopper." + locationName + ".collectRadius", collectRadius - 1);
//	                            player.sendMessage(MessageManager.getPrefix() + "§7Sammelradius auf §e" + (collectRadius - 1) + " §7gesetzt.");
	                            if (collectRadius - 1 == 0)
	                                player.sendMessage(MessageManager.getPrefix() + "§7Der minimale Sammelradius wurde erreicht.");
	                        } else {
	                            player.sendMessage(MessageManager.getPrefix() + "§7Der Sammelradius kann nicht kleiner als §e0 §7sein.");
	                        }
	                        InventoryBuilder.reloadHopperInventory(player, locationName);
	                        break;
	                    case "§6Sammelradius §a§l+1":
	                        if (collectRadius < 15) {
	                            JsonConfig.save("minehopper." + locationName + ".collectRadius", collectRadius + 1);
//	                            player.sendMessage(MessageManager.getPrefix() + "§7Sammelradius auf §e" + (collectRadius + 1) + " §7gesetzt.");
	                            if (collectRadius + 1 == 15)
	                                player.sendMessage(MessageManager.getPrefix() + "§7Der maximale Sammelradius wurde erreicht.");
	                        } else {
	                            player.sendMessage(MessageManager.getPrefix() + "§7Der Sammelradius kann nicht größer als §e15 §7sein.");
	                        }
	                        InventoryBuilder.reloadHopperInventory(player, locationName);
	                        break;
	                    case "§6Optische Anzeige":
	                        player.closeInventory();
	                        player.sendMessage(MessageManager.getPrefix() + "§cDiese Funktion steht noch nicht zur Verfügung.");
	                        break;
	                    default:
	                        break;
	                }
	            }
	            else 
	            	if (player.getOpenInventory() != null &&
	            		player.getOpenInventory().getTopInventory() != null &&
	            		"§6Trichter-Einstellungen".equals(player.getOpenInventory().getTopInventory().getName())) {
	            		
	            		if (event.getClickedInventory() != null &&
	            				event.getClickedInventory().equals(player.getInventory()) &&
	            				event.getCurrentItem() != null &&
	            				event.getCurrentItem().getType() != null) {
	            			
	            			event.setCancelled(true);
	            			
	            			String locationName = HopperManager.getLocationFromInventory(player.getOpenInventory().getTopInventory());
	            			if (locationName == null) {
	            				player.sendMessage(MessageManager.getPrefix() + "§4Fehler: §cKeine gültige Hopper-Position gefunden.");
	            				return;
	            			}
	            			Setter setter = new Setter(locationName);
	            			Material clicked = event.getCurrentItem().getType();
	            			
	            			if(clicked == null || clicked.equals(Material.AIR)) return;
	            			
	            			if(clicked.equals(Material.AIR)) {
	            				setter.setItemFilter(Material.AIR);
	            				player.sendMessage(MessageManager.getPrefix() + "§7Der Itemfilter bei §e" + locationName + " §7wurde zurückgesetzt.");
                            	player.playSound(player.getLocation(), Sound.BAT_DEATH, 10f, 10f);
	            			}
	            			else {
	            				setter.setItemFilter(clicked);
	            				player.sendMessage(MessageManager.getPrefix() + "§7Der Itemfilter §e" + HopperManager.formatMaterialName(clicked) + " §7wurde bei §e" + locationName + " §7gesetzt.");
								player.playSound(player.getLocation(), Sound.CLICK, 10f, 10f);
	            			}
	            			InventoryBuilder.reloadHopperInventory(player, locationName);
	            		}
	            	}
	            	else 
	            		if (event.getClickedInventory() != null &&
	            			"§6Trichter-Mehrfach-Verbindungen".equals(event.getClickedInventory().getName())) {
	    	        		event.setCancelled(true);
	    	        		
	    	        		switch (event.getCurrentItem().getItemMeta().getDisplayName()) {
							case "§eZurück zu den Einstellungen":
								String location = HopperManager.getLocationFromInventory(player.getOpenInventory().getTopInventory());
								InventoryBuilder.reloadHopperInventory(player, location);
								player.playSound(player.getLocation(), Sound.CLICK, 15f, 15f);
								break;
							default:
								break;
							}
	    	        	}
		            	else 
		            		if (event.getClickedInventory() != null &&
		            			"§6Trichter-Filter".equals(event.getClickedInventory().getName())) {
		    	        		event.setCancelled(true);
		    	        		
		    	        		switch (event.getCurrentItem().getItemMeta().getDisplayName()) {
								case "§eZurück zu den Einstellungen":
									String location = HopperManager.getLocationFromInventory(player.getOpenInventory().getTopInventory());
									InventoryBuilder.reloadHopperInventory(player, location);
									player.playSound(player.getLocation(), Sound.CLICK, 15f, 15f);
									break;
								default:
									break;
								}
		    	        	}
	        }
	    } catch (Exception ignored) {}
	}

}
