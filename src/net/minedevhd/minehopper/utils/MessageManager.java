package net.minedevhd.minehopper.utils;

import net.minedevhd.minehopper.config.JsonConfig;

public class MessageManager {
	
	public static String getPrefix() { 
		String prefix = JsonConfig.get("settings.prefix", String.class);
		if(JsonConfig.exists("settings.prefix")) return prefix.replace("&", "§"); 
		return "§8[§6MineHopper§8]§r ";
	}

}
