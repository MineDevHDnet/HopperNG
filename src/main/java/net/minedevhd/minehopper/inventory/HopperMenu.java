package net.minedevhd.minehopper.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minedevhd.minehopper.model.BlockKey;
import net.minedevhd.minehopper.model.FilterMode;
import net.minedevhd.minehopper.model.ManagedHopper;
import net.minedevhd.minehopper.service.HopperService;
import net.minedevhd.minehopper.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;

public final class HopperMenu {
    private enum MenuType { MAIN, TARGETS }
    private final HopperService service;
    private final NamespacedKey actionKey;
    private final NamespacedKey targetIndexKey;
    public HopperMenu(JavaPlugin plugin,HopperService service){this.service=service;actionKey=new NamespacedKey(plugin,"menu_action");targetIndexKey=new NamespacedKey(plugin,"target_index");}

    public void openMain(Player player,BlockKey source){
        ManagedHopper h=service.get(source);if(h==null){Messages.error(player,"Dieser Trichter wird nicht mehr von HopperNG verwaltet.");return;}
        MenuHolder holder=new MenuHolder(MenuType.MAIN,source);Inventory inv=Bukkit.createInventory(holder,45,title("HopperNG Einstellungen"));holder.inventory=inv;fill(inv);
        inv.setItem(10,item(Material.CHEST,Math.max(1,h.transferAmount()),"Transportmenge",List.of("Items pro Verarbeitungszyklus: "+h.transferAmount(),"Klicken: 1 → 12 → 64 → 1"),"transfer",false));
        boolean fastAllowed=player.hasPermission("hopperng.fasttick")||player.hasPermission("hopperng.admin");
        inv.setItem(11,item(Material.DIAMOND_BOOTS,1,"Fast-Tick",List.of("Status: "+(h.fastTick()?"aktiv":"deaktiviert"),fastAllowed?"Klicken zum Umschalten.":"Keine Berechtigung."),"fast",h.fastTick()));
        inv.setItem(13,item(Material.HOPPER,1,"Verwalteter Trichter",List.of(source.display()),null,false));
        inv.setItem(15,item(Material.COMPARATOR,1,"Ziele / Mehrfach-Verbindungen",List.of("Gespeicherte Ziele: "+h.targets().size(),"Ziele werden Round-Robin angesteuert.","Klicken zum Verwalten."),"targets",h.targets().size()>1));
        inv.setItem(16,item(Material.ENDER_PEARL,1,"Ziel hinzufügen",List.of("Startet den Verbindungsmodus.","Danach einen Inventar-Block rechtsklicken."),"link",!h.targets().isEmpty()));
        ItemStack filter=h.filterItem();if(filter==null||filter.getType().isAir())filter=new ItemStack(Material.BARRIER);else filter.setAmount(1);
        inv.setItem(28,item(filter,"Itemfilter",List.of("Modus: "+filterModeName(h.filterMode()),"Inventar-Item anklicken: Filter setzen","Rechtsklick: Material / Exakt umschalten","Shift-Klick: Filter löschen"),"filter",h.filterMode()!=FilterMode.OFF));
        if(h.collectRadius()>0)inv.setItem(30,item(Material.RED_DYE,1,"Sammelradius -1",List.of("Aktuell: "+h.collectRadius()),"radius_minus",false));
        inv.setItem(31,item(Material.HOPPER_MINECART,Math.max(1,h.collectRadius()),"Sammelradius",List.of("Radius: "+h.collectRadius(),"0 deaktiviert das Einsaugen von Boden-Items.","Container über dem Hopper werden weiterhin eingezogen."),null,false));
        if(h.collectRadius()<service.settings().maxCollectRadius())inv.setItem(32,item(Material.LIME_DYE,1,"Sammelradius +1",List.of("Maximum: "+service.settings().maxCollectRadius()),"radius_plus",false));
        inv.setItem(34,item(Material.ENDER_EYE,1,"Verbindungen anzeigen",List.of("Zeigt Radius und geladene Ziele optisch an."),"visualize",false));
        inv.setItem(40,item(Material.BARRIER,1,"HopperNG-Verwaltung entfernen",List.of("Der Block bleibt bestehen und arbeitet danach wieder vanilla.","Shift-Klick zum Entfernen."),"unmanage",false));
        inv.setItem(44,item(Material.OAK_DOOR,1,"Schließen",List.of(),"close",false));player.openInventory(inv);
    }

    public void openTargets(Player player,BlockKey source){
        ManagedHopper h=service.get(source);if(h==null){Messages.error(player,"Dieser Trichter wird nicht mehr von HopperNG verwaltet.");return;}
        MenuHolder holder=new MenuHolder(MenuType.TARGETS,source);Inventory inv=Bukkit.createInventory(holder,54,title("HopperNG Ziele"));holder.inventory=inv;fill(inv);
        for(int i=0;i<h.targets().size()&&i<45;i++){BlockKey target=h.targets().get(i);ItemStack icon=targetIcon(target);ItemMeta meta=icon.getItemMeta();meta.displayName(text("Ziel "+(i+1),NamedTextColor.GOLD));meta.lore(List.of(text(target.display(),NamedTextColor.GRAY),text("Klicken zum Entfernen.",NamedTextColor.RED)));meta.getPersistentDataContainer().set(actionKey,PersistentDataType.STRING,"remove_target");meta.getPersistentDataContainer().set(targetIndexKey,PersistentDataType.INTEGER,i);icon.setItemMeta(meta);inv.setItem(i,icon);}
        inv.setItem(45,item(Material.ARROW,1,"Zurück",List.of(),"back",false));
        if(h.targets().size()<service.settings().maxTargetsPerHopper())inv.setItem(53,item(Material.LIME_DYE,1,"Neues Ziel hinzufügen",List.of("Startet den Verbindungsmodus."),"add_target",false));else inv.setItem(53,item(Material.BARRIER,1,"Ziellimit erreicht",List.of("Maximum: "+service.settings().maxTargetsPerHopper()),null,false));
        player.openInventory(inv);
    }

    public boolean isMenu(Inventory inventory){return inventory!=null&&inventory.getHolder() instanceof MenuHolder;}
    public boolean handleClick(InventoryClickEvent event){
        Inventory top=event.getView().getTopInventory();if(!(top.getHolder() instanceof MenuHolder holder))return false;event.setCancelled(true);if(!(event.getWhoClicked() instanceof Player player))return true;
        if(service.get(holder.source)==null){player.closeInventory();Messages.error(player,"Dieser Trichter existiert nicht mehr in HopperNG.");return true;}
        if(event.getRawSlot()>=top.getSize()){if(holder.type==MenuType.MAIN){ItemStack sample=event.getCurrentItem();if(sample!=null&&!sample.getType().isAir()&&service.setFilter(holder.source,sample)){click(player);Messages.success(player,"Itemfilter wurde auf "+readableMaterial(sample.getType())+" gesetzt.");openMain(player,holder.source);}}return true;}
        ItemStack clicked=event.getCurrentItem();if(clicked==null||clicked.getType().isAir()||!clicked.hasItemMeta())return true;String action=clicked.getItemMeta().getPersistentDataContainer().get(actionKey,PersistentDataType.STRING);if(action==null)return true;click(player);if(holder.type==MenuType.MAIN)handleMainAction(player,holder.source,action,event);else handleTargetAction(player,holder.source,action,clicked);return true;
    }

    private void handleMainAction(Player player,BlockKey source,String action,InventoryClickEvent event){switch(action){
        case "transfer"->{int amount=service.cycleTransferAmount(source);Messages.success(player,"Transportmenge: "+amount+" Item(s) pro Zyklus.");openMain(player,source);}
        case "fast"->{if(!player.hasPermission("hopperng.fasttick")&&!player.hasPermission("hopperng.admin")){Messages.error(player,"Dir fehlt die Berechtigung hopperng.fasttick.");return;}Boolean enabled=service.toggleFastTick(source);if(enabled!=null){Messages.success(player,"Fast-Tick wurde "+(enabled?"aktiviert":"deaktiviert")+".");openMain(player,source);}}
        case "targets"->openTargets(player,source);case "link"->startLink(player,source);
        case "filter"->{if(event.isShiftClick()){service.clearFilter(source);Messages.success(player,"Itemfilter wurde entfernt.");openMain(player,source);}else if(event.isRightClick()){FilterMode mode=service.toggleFilterMode(source);if(mode==null)Messages.info(player,"Wähle zuerst ein Item aus deinem Inventar als Filter.");else{Messages.success(player,"Filtermodus: "+filterModeName(mode)+".");openMain(player,source);}}else Messages.info(player,"Klicke unten in deinem Inventar auf das gewünschte Filter-Item.");}
        case "radius_minus"->{int radius=service.adjustCollectRadius(source,-1);Messages.success(player,"Sammelradius: "+radius+".");openMain(player,source);}case "radius_plus"->{int radius=service.adjustCollectRadius(source,1);Messages.success(player,"Sammelradius: "+radius+".");openMain(player,source);}
        case "visualize"->{player.closeInventory();service.visualize(player,source);Messages.info(player,"Optische Anzeige gestartet.");}
        case "unmanage"->{if(!event.isShiftClick()){Messages.info(player,"Zum Entfernen der HopperNG-Verwaltung Shift-Klick verwenden.");return;}player.closeInventory();if(service.unmanage(source))Messages.success(player,"HopperNG-Verwaltung entfernt. Der Hopper arbeitet wieder vanilla.");}
        case "close"->player.closeInventory();default->{} }}
    private void handleTargetAction(Player player,BlockKey source,String action,ItemStack clicked){switch(action){case "back"->openMain(player,source);case "add_target"->startLink(player,source);case "remove_target"->{Integer index=clicked.getItemMeta().getPersistentDataContainer().get(targetIndexKey,PersistentDataType.INTEGER);if(index!=null){BlockKey removed=service.removeTarget(source,index);if(removed!=null)Messages.success(player,"Ziel entfernt: "+removed.display());openTargets(player,source);}}default->{}}}
    private void startLink(Player player,BlockKey source){if(service.beginLink(player,source)){player.closeInventory();Messages.info(player,"Verbindungsmodus aktiv: Rechtsklicke jetzt einen Inventar-Block. Sneaken bricht ab.");}}
    private ItemStack targetIcon(BlockKey target){Material material=Material.CHEST;World world=target.resolveWorld();if(world!=null&&target.isChunkLoaded()){Block block=world.getBlockAt(target.x(),target.y(),target.z());if(block.getType().isItem()&&!block.getType().isAir())material=block.getType();}return new ItemStack(material);}
    private void fill(Inventory inv){ItemStack filler=item(Material.GRAY_STAINED_GLASS_PANE,1," ",List.of(),null,false);for(int i=0;i<inv.getSize();i++)inv.setItem(i,filler);}
    private ItemStack item(Material material,int amount,String name,List<String> lore,String action,boolean glow){return item(new ItemStack(material,Math.max(1,Math.min(amount,material.getMaxStackSize()))),name,lore,action,glow);}
    private ItemStack item(ItemStack stack,String name,List<String> lore,String action,boolean glow){ItemStack result=stack.clone();ItemMeta meta=result.getItemMeta();meta.displayName(text(name,NamedTextColor.GOLD));List<Component> lines=new ArrayList<>();for(String line:lore)lines.add(text(line,NamedTextColor.GRAY));meta.lore(lines);if(action!=null)meta.getPersistentDataContainer().set(actionKey,PersistentDataType.STRING,action);if(glow){meta.addEnchant(Enchantment.UNBREAKING,1,true);meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);}result.setItemMeta(meta);return result;}
    private Component title(String value){return Component.text(value,NamedTextColor.GOLD).decoration(TextDecoration.ITALIC,false);}private Component text(String value,NamedTextColor color){return Component.text(value,color).decoration(TextDecoration.ITALIC,false);}
    private String filterModeName(FilterMode mode){return switch(mode){case OFF->"Aus";case MATERIAL->"Material";case EXACT->"Exaktes Item inkl. Meta";};}
    private String readableMaterial(Material material){String[] words=material.name().toLowerCase().split("_");StringBuilder result=new StringBuilder();for(String word:words){if(!result.isEmpty())result.append(' ');result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));}return result.toString();}
    private void click(Player player){player.playSound(player.getLocation(),Sound.UI_BUTTON_CLICK,0.6f,1.1f);}
    private static final class MenuHolder implements InventoryHolder{private final MenuType type;private final BlockKey source;private Inventory inventory;private MenuHolder(MenuType type,BlockKey source){this.type=type;this.source=source;}@Override public Inventory getInventory(){return inventory;}}
}
