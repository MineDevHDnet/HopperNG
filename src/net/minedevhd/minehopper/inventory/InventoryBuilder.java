package net.minedevhd.minehopper.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.minedevhd.minehopper.config.Getter;
import net.minedevhd.minehopper.config.JsonConfig;
import net.minedevhd.minehopper.utils.CustomHeadTextures;
import net.minedevhd.minehopper.utils.ItemUtil;

public class InventoryBuilder {
	
	private static void openHopperInventory(Player player, String location, Integer teleportItems, boolean fasttick, boolean multipleConnections, Material itemFilter, Integer collectRadius, String linkedHopper) {
		Inventory inv = Bukkit.createInventory(null, (9 * 5), "§6Trichter-Einstellungen");
		
		for(int i = 0; i < inv.getSize(); i++) {
			inv.setItem(i, new ItemUtil(Material.STAINED_GLASS_PANE).durability((short) 7).displayname("§0").build());
		}
		
		inv.setItem(10, new ItemUtil(Material.CHEST).amount(teleportItems).displayname("§6Anzahl der transportierten Items")
																				 .lore("§7Der Trichter kann unterschiedlich viele Items pro Tick verarbeiten.")
																				 .lore("§7Dieses betrifft sowohl das Einziehen als auch das Weiterleiten.")
																				 .lore("")
																				 .lore("§7Status: " + (teleportItems == 1 ? "§c1 Item" : teleportItems == 12 ? "§e12 Items" : teleportItems == 64 ? "§aganzer Stack" : "§fUnbekannt"))
																				 .lore("")
																				 .lore("§7Klicke, um die Transportmethode zu ändern.").build());
		
		if(player.hasPermission("minehopper.admin") || player.hasPermission("minehopper.fasttick.use")) {
			if(fasttick) {
				inv.setItem(11, new ItemUtil(Material.DIAMOND_BOOTS).displayname("§6Force Fast Tick")
						.lore("§7Erlaubt dauerhaft Fast-Ticks für diesen Trichter.")
						.lore("§cAdmin-Setting")
						.lore("")
						.lore("§7Status: §aaktiviert")
						.lore("")
						.lore("§7Klicke, um die Fast-Tick-Einstellung zu ändern.")
						.glow().build());
			}
			else {
				inv.setItem(11, new ItemUtil(Material.DIAMOND_BOOTS).displayname("§6Force Fast Tick")
						.lore("§7Erlaubt dauerhaft Fast-Ticks für diesen Trichter.")
						.lore("§7Status: §cdeaktiviert")
						.lore("")
						.lore("§7Klicke, um die Fast-Tick-Einstellung zu ändern.").build());
			}
		}
		
		inv.setItem(13, new ItemUtil(Material.HOPPER).displayname("§6Trichter")
															.lore("§7Position: §e" + location).build());
		
		inv.setItem(15, new ItemUtil(Material.REDSTONE_COMPARATOR).displayname("§6Mehrfach-Verbindungen")
																		 .lore("")
																		 .lore("§7Klicke, um die Mehrfach-Verbindung zu öffnen").build());
		
		if(linkedHopper.equals("unknown")) {
			inv.setItem(16, new ItemUtil(Material.HOPPER).displayname("§6Verbundene Trichter")
					.lore("")
					.lore("§7Klicke, um diesen Trichter neu zu verbinden (weiterzuleiden).").build());
		}
		else {
			inv.setItem(16, new ItemUtil(Material.HOPPER).displayname("§6Verbundene Trichter")
					.lore("§7Weiterleiten an §e" + linkedHopper)
					.lore("")
					.lore("§7Klicke, um diesen Trichter neu zu verbinden (weiterzuleiten).")
					.glow().build());
		}
		
		inv.setItem(28, new ItemUtil(new Getter(location).getItemFilter()).displayname("§6Trichter-Filter")
														 	 .lore("§7Der Trichter nimmt nur das gefilterte Material entgegen.")
														 	 .lore("§7Items können manuell weiterhin hinzugefügt werden.")
														 	 .lore("")
														 	 .lore("§7Verzauberungsfilter: §cdeaktiviert")
														 	 .lore("§7Effektfilter: §cdeaktiviert")
														 	 .lore("§7Signierungsfilter: §cdeaktiviert")
														 	 .lore("§7Beschreibungsfilter: §cdeaktiviert")
														 	 .lore("§7Namensfilter: §cdeaktiviert")
														 	 .lore("")
														 	 .lore("§eZum ändern wähle ein Item aus deinem Inventar.")
														 	 .lore("§7Klicke, um Filter-Einstellungen zu ändern.")
														 	 .lore("§7Klicke mit Umschalt/Shift, um die Filter zu löschen.").build());

		if(collectRadius > 0) {
			inv.setItem(30, new ItemUtil(Material.SKULL_ITEM).durability((short) 3).displayname("§6Sammelradius §c§l-1")
																	.lore("§7Klicke, um den Sammelradius um 1 zu verkleinern.")
																	.skullTexture(CustomHeadTextures.getHeadLeft()).build());

		}
		
		inv.setItem(31, new ItemUtil(Material.HOPPER_MINECART).unsafeStackSize(true).amount(collectRadius)
																					.displayname("§6Sammelradius")
																					.lore("§7Gegenstände werden im Radius von §e" + collectRadius + " §7eingesammelt").build());
		if(collectRadius < 15) {
			inv.setItem(32, new ItemUtil(Material.SKULL_ITEM).durability((short) 3).displayname("§6Sammelradius §a§l+1")
																	.lore("§7Klicke, um den Sammelradius um 1 zu erh§hen.")
																	.skullTexture(CustomHeadTextures.getHeadRight()).build());

		}
		
		inv.setItem(34, new ItemUtil(Material.EYE_OF_ENDER).displayname("§6Optische Anzeige")
																  .lore("§7Zeigt f§r §e30 Sekunden §7die Verbindung und den Sammelradius optisch an.")
																  .lore("")
																  .lore("§7Klicke, um die Anzeige zu starten.").build());
		
		player.openInventory(inv);
	}
	
	@SuppressWarnings("unused")
	public static void reloadHopperInventory(Player player, String locationName) {
		Getter getter = new Getter(locationName);

	    Integer teleportItems = getter.getTeleportItems();
	    Boolean fasttick = getter.isFastTick();
	    Boolean multipleConnections = getter.isMultipleConnections();
	    Material itemFilter = getter.getItemFilter();
	    Integer collectRadius = getter.getCollectRadius();
	    String linkedHopperName = getter.getLinkedHopper();

	    // Fallbacks bei null
	    if (teleportItems == null) teleportItems = 1;
	    if (fasttick == null) fasttick = false;
	    if (multipleConnections == null) multipleConnections = false;
	    if (itemFilter == null) itemFilter = Material.BARRIER;
	    if (collectRadius == null) collectRadius = 1;
	    if (linkedHopperName == null) linkedHopperName = "unknown";
		
		InventoryBuilder.openHopperInventory(player, locationName, teleportItems, fasttick, multipleConnections, itemFilter, collectRadius, linkedHopperName);
	}
	
	public static void createDefaultHopper(Location location, Integer teleportItems, boolean fasttick, boolean multipleConnections, Material itemFilter, Integer collectRadius, String linkedHopperName) {
		String locationName = JsonConfig.compLocation(location);
		
		JsonConfig.save("minehopper." + locationName + ".teleportItems", teleportItems);
		JsonConfig.save("minehopper." + locationName + ".fastTick", fasttick);
		JsonConfig.save("minehopper." + locationName + ".multipleConnections", multipleConnections);
		JsonConfig.save("minehopper." + locationName + ".itemFilter", itemFilter);
		JsonConfig.save("minehopper." + locationName + ".collectRadius", collectRadius);
		JsonConfig.save("minehopper." + locationName + ".linkedHopper", linkedHopperName);
	}
	
	public static void openMultiConnectionInventory(Player player, String location) {
		Inventory inv = Bukkit.createInventory(null, (9 * 6), "§6Trichter-Mehrfach-Verbindungen");
		
		for(int i = 0; i < inv.getSize(); i++) {
			inv.setItem(i, new ItemUtil(Material.STAINED_GLASS_PANE).durability((short) 7).displayname("§0").build());
		}
		
		inv.setItem(13, new ItemUtil(Material.HOPPER).displayname("§6Trichter")
															.lore("§7Position: §e" + location).build());

		inv.setItem(22, new ItemUtil(Material.BARRIER).displayname("§c§lBald verfügbar!")
															 .lore("")
															 .lore("§7Die Mehrfach-Verbindung wird bald hinzugefügt.").build());

		inv.setItem(45, new ItemUtil(Material.SKULL_ITEM).durability((short) 3).displayname("§eZurück zu den Einstellungen").skullTexture(CustomHeadTextures.getHeadLeft()).build());
		
		inv.setItem(52, new ItemUtil(Material.EYE_OF_ENDER).displayname("§6Optische Anzeige")
																  .lore("§7Zeigt f§r §e30 Sekunden §7die Verbindung und den Sammelradius optisch an.")
																  .lore("")
																  .lore("§7Klicke, um die Anzeige zu starten.").build());
		inv.setItem(53, new ItemUtil(Material.HOPPER).displayname("§eVerbindungsmodus starten")
				  .lore("§7Wenn der Verbindungsmodus aktiv ist, klicke mit")
				  .lore("§7einem Item in deiner Hand auf den gewünschten Block.")
				  .lore("§7Dann wird dieses Item, an diesen Block weitergeleitet.")
				  .lore("§7Zum Beenden des Verbindungsmodus, sneaken.")
				  .lore("")
				  .lore("§7Klicke, um den Verbindungsmodus zu starten.")
				  .glow().build());
		player.openInventory(inv);
	}
	
	public static void openFilterInventory(Player player, String location) {
		Inventory inv = Bukkit.createInventory(null, (9 * 6), "§6Trichter-Filter");
		
		for(int i = 0; i < inv.getSize(); i++) {
			inv.setItem(i, new ItemUtil(Material.STAINED_GLASS_PANE).durability((short) 7).displayname("§0").build());
		}
		
		inv.setItem(13, new ItemUtil(Material.HOPPER).displayname("§6Trichter")
															.lore("§7Position: §e" + location).build());

		inv.setItem(22, new ItemUtil(Material.BARRIER).displayname("§c§lBald verfügbar!")
															 .lore("")
															 .lore("§7Die Trichter-Filter werden bald hinzugefügt.").build());

		inv.setItem(28, new ItemUtil(Material.ENCHANTMENT_TABLE).displayname("§eFilter: Verzauberung")
				.lore("§7Filter von Verzauberungen der Gegenstände")
				.lore("")
				.lore("§7Status: §cdeaktivieren")
				.lore("")
				.lore("§7Klicke, um die Filterung zu §aaktivieren§7.").build());
		inv.setItem(37, new ItemUtil(Material.BARRIER).displayname("§eFilter: Keine Verzauberung")
				.lore("§7Filter von nicht Verzauberungen der Gegenstände")
				.lore("")
				.lore("§7Status: §cdeaktivieren")
				.lore("")
				.lore("§7Klicke, um die Filterung zu §aaktivieren§7.").build());

		inv.setItem(31, new ItemUtil(Material.FEATHER).displayname("§eFilter: Signierung")
				.lore("§7Filter von Signierungen der Gegenstände")
				.lore("")
				.lore("§7Status: §cdeaktivieren")
				.lore("")
				.lore("§7Klicke, um die Filterung zu §aaktivieren§7.").build());
		inv.setItem(40, new ItemUtil(Material.BARRIER).displayname("§eFilter: Keine Signierung")
				.lore("§7Filter von nicht Signierungen der Gegenstände")
				.lore("")
				.lore("§7Status: §cdeaktivieren")
				.lore("")
				.lore("§7Klicke, um die Filterung zu §aaktivieren§7.").build());

		inv.setItem(33, new ItemUtil(Material.PAPER).displayname("§eFilter: Beschreibung")
				.lore("§7Filter von Beschreibungen der Gegenstände")
				.lore("")
				.lore("§7Status: §cdeaktivieren")
				.lore("")
				.lore("§7Klicke, um die Filterung zu §aaktivieren§7.").build());
		inv.setItem(42, new ItemUtil(Material.BARRIER).displayname("§eFilter: Keine Beschreibung")
				.lore("§7Filter von Gegenst§nde ohne Beschreibungen")
				.lore("")
				.lore("§7Status: §cdeaktivieren")
				.lore("")
				.lore("§7Klicke, um die Filterung zu §aaktivieren§7.").build());
		inv.setItem(34, new ItemUtil(Material.NAME_TAG).displayname("§eFilter: Name")
				.lore("§7Filter von umbenannten Gegenständen")
				.lore("")
				.lore("§7Status: §cdeaktivieren")
				.lore("")
				.lore("§7Klicke, um die Filterung zu §aaktivieren§7.").build());

		inv.setItem(45, new ItemUtil(Material.SKULL_ITEM).durability((short) 3).displayname("§eZurück zu den Einstellungen").skullTexture(CustomHeadTextures.getHeadLeft()).build());
		player.openInventory(inv);
	}
	
}
