package net.minedevhd.minehopper;

import net.minedevhd.minehopper.commands.HopperCommand;
import net.minedevhd.minehopper.inventory.HopperMenu;
import net.minedevhd.minehopper.listeners.HopperListener;
import net.minedevhd.minehopper.persistence.HopperRepository;
import net.minedevhd.minehopper.service.HopperService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineHopper extends JavaPlugin {
    private HopperService hopperService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        HopperRepository repository = new HopperRepository(this);
        hopperService = new HopperService(this, repository);
        HopperMenu menu = new HopperMenu(this, hopperService);
        getServer().getPluginManager().registerEvents(new HopperListener(hopperService, menu), this);

        HopperCommand commandHandler = new HopperCommand(this, hopperService, menu);
        PluginCommand command = getCommand("hopperng");
        if (command == null) {
            getLogger().severe("Command 'hopperng' is missing from plugin.yml. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);
        hopperService.loadAndStart();
        getLogger().info("HopperNG 1.0.0 enabled.");
    }

    @Override
    public void onDisable() {
        if (hopperService != null) hopperService.shutdown();
    }
}
