package net.minedevhd.minehopper.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class Messages {
    private static final Component PREFIX = Component.text("[HopperNG] ", NamedTextColor.GOLD);
    private Messages() {}
    public static void info(CommandSender sender, String text) { sender.sendMessage(PREFIX.append(Component.text(text, NamedTextColor.GRAY))); }
    public static void success(CommandSender sender, String text) { sender.sendMessage(PREFIX.append(Component.text(text, NamedTextColor.GREEN))); }
    public static void error(CommandSender sender, String text) { sender.sendMessage(PREFIX.append(Component.text(text, NamedTextColor.RED))); }
}
