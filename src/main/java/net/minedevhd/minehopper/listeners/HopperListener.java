package net.minedevhd.minehopper.listeners;

import net.minedevhd.minehopper.inventory.HopperMenu;
import net.minedevhd.minehopper.model.BlockKey;
import net.minedevhd.minehopper.service.HopperService;
import net.minedevhd.minehopper.utils.Messages;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;

public final class HopperListener implements Listener {
    private final HopperService service; private final HopperMenu menu;
    public HopperListener(HopperService service,HopperMenu menu){this.service=service;this.menu=menu;}
    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=true) public void onInteract(PlayerInteractEvent event){
        if(event.getHand()!=EquipmentSlot.HAND||event.getAction()!=Action.RIGHT_CLICK_BLOCK)return;Block clicked=event.getClickedBlock();if(clicked==null)return;Player player=event.getPlayer();
        if(service.hasLinkSession(player)){event.setCancelled(true);HopperService.LinkResult result=service.completeLink(player,clicked);switch(result){case SUCCESS->Messages.success(player,"Ziel verbunden: "+BlockKey.from(clicked).display());case NOT_CONTAINER->Messages.error(player,"Dieser Block besitzt kein Inventar. Wähle einen anderen Zielblock.");case SELF->Messages.error(player,"Ein Hopper kann nicht mit sich selbst verbunden werden.");case DUPLICATE->Messages.error(player,"Dieses Ziel ist bereits verbunden.");case LIMIT_REACHED->Messages.error(player,"Das maximale Ziellimit wurde erreicht.");case SOURCE_MISSING->Messages.error(player,"Der Quell-Hopper existiert nicht mehr.");}return;}
        if(clicked.getType()!=Material.HOPPER||!player.isSneaking()||!player.getInventory().getItemInMainHand().getType().isAir())return;event.setCancelled(true);
        if(!player.hasPermission("hopperng.use")){Messages.error(player,"Dir fehlt die Berechtigung hopperng.use.");return;}if(!service.settings().isWorldEnabled(clicked.getWorld())){Messages.error(player,"HopperNG ist in dieser Welt deaktiviert.");return;}if(!service.isManaged(clicked)&&service.register(clicked)==null){Messages.error(player,"Der Hopper konnte nicht registriert werden.");return;}menu.openMain(player,BlockKey.from(clicked));
    }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void onPlace(BlockPlaceEvent event){if(!service.settings().autoRegisterOnPlace()||event.getBlockPlaced().getType()!=Material.HOPPER||!event.getPlayer().hasPermission("hopperng.use")||!service.settings().isWorldEnabled(event.getBlockPlaced().getWorld()))return;service.register(event.getBlockPlaced());}
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void onBreak(BlockBreakEvent event){BlockKey key=BlockKey.from(event.getBlock());if(service.isTrackedBlock(key))service.handleBlockRemoved(key);}
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void onEntityExplode(EntityExplodeEvent event){for(Block block:event.blockList()){BlockKey key=BlockKey.from(block);if(service.isTrackedBlock(key))service.handleBlockRemoved(key);}}
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void onBlockExplode(BlockExplodeEvent event){for(Block block:event.blockList()){BlockKey key=BlockKey.from(block);if(service.isTrackedBlock(key))service.handleBlockRemoved(key);}}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void onVanillaMove(InventoryMoveItemEvent event){InventoryHolder holder=event.getInitiator().getHolder();if(holder instanceof Hopper hopper&&service.isManaged(hopper.getBlock()))event.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void onVanillaPickup(InventoryPickupItemEvent event){InventoryHolder holder=event.getInventory().getHolder();if(holder instanceof Hopper hopper&&service.isManaged(hopper.getBlock()))event.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST) public void onInventoryClick(InventoryClickEvent event){menu.handleClick(event);}
    @EventHandler(priority=EventPriority.HIGHEST) public void onInventoryDrag(InventoryDragEvent event){if(menu.isMenu(event.getView().getTopInventory()))event.setCancelled(true);}
    @EventHandler public void onSneak(PlayerToggleSneakEvent event){if(event.isSneaking()&&service.cancelLink(event.getPlayer()))Messages.info(event.getPlayer(),"Verbindungsmodus abgebrochen.");}
    @EventHandler public void onQuit(PlayerQuitEvent event){service.cancelLink(event.getPlayer());}
}
