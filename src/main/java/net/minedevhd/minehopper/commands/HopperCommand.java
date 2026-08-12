package net.minedevhd.minehopper.commands;

import net.minedevhd.minehopper.inventory.HopperMenu;
import net.minedevhd.minehopper.model.BlockKey;
import net.minedevhd.minehopper.service.HopperService;
import net.minedevhd.minehopper.utils.Messages;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HopperCommand implements CommandExecutor,TabCompleter {
    private final JavaPlugin plugin;private final HopperService service;private final HopperMenu menu;
    public HopperCommand(JavaPlugin plugin,HopperService service,HopperMenu menu){this.plugin=plugin;this.service=service;this.menu=menu;}
    @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){String sub=args.length==0?"help":args[0].toLowerCase(Locale.ROOT);switch(sub){case "manage"->manage(sender);case "stats"->stats(sender);case "reload"->reload(sender);case "prune"->prune(sender);case "help"->help(sender);default->{Messages.error(sender,"Unbekannter Unterbefehl. Nutze /"+label+" help.");return false;}}return true;}
    private void manage(CommandSender sender){if(!(sender instanceof Player player)){Messages.error(sender,"Dieser Befehl kann nur als Spieler verwendet werden.");return;}if(!player.hasPermission("hopperng.use")){Messages.error(player,"Dir fehlt die Berechtigung hopperng.use.");return;}Block target=player.getTargetBlockExact(6);if(target==null||target.getType()!=Material.HOPPER){Messages.error(player,"Schaue auf einen Hopper in maximal 6 Blöcken Entfernung.");return;}if(!service.settings().isWorldEnabled(target.getWorld())){Messages.error(player,"HopperNG ist in dieser Welt deaktiviert.");return;}if(!service.isManaged(target)&&service.register(target)==null){Messages.error(player,"Der Hopper konnte nicht registriert werden.");return;}menu.openMain(player,BlockKey.from(target));}
    private void stats(CommandSender sender){if(!admin(sender))return;HopperService.Stats s=service.stats();Messages.info(sender,"Verwaltet: "+s.managed()+" | geladen: "+s.loaded()+" | Fast-Tick: "+s.fast()+" | Ziele: "+s.targets());}
    private void reload(CommandSender sender){if(!admin(sender))return;plugin.reloadConfig();service.reload();Messages.success(sender,"Konfiguration und hoppers.yml wurden neu geladen.");}
    private void prune(CommandSender sender){if(!admin(sender))return;HopperService.PruneResult r=service.pruneInvalidLoadedEntries();Messages.success(sender,"Bereinigung abgeschlossen: "+r.removedSources()+" Quellen und "+r.removedTargets()+" Ziele entfernt; "+r.skippedUnloaded()+" ungeladene Einträge übersprungen.");}
    private void help(CommandSender sender){Messages.info(sender,"/hopperng manage - angesehenen Hopper verwalten");Messages.info(sender,"Alternativ: mit leerer Hand sneaken + Rechtsklick auf einen Hopper.");if(sender.hasPermission("hopperng.admin"))Messages.info(sender,"/hopperng stats | reload | prune");}
    private boolean admin(CommandSender sender){if(sender.hasPermission("hopperng.admin"))return true;Messages.error(sender,"Dir fehlt die Berechtigung hopperng.admin.");return false;}
    @Override public List<String> onTabComplete(CommandSender sender,Command command,String alias,String[] args){if(args.length!=1)return List.of();String prefix=args[0].toLowerCase(Locale.ROOT);List<String> options=new ArrayList<>(List.of("manage","help"));if(sender.hasPermission("hopperng.admin")){options.add("stats");options.add("reload");options.add("prune");}return options.stream().filter(o->o.startsWith(prefix)).sorted().toList();}
}
