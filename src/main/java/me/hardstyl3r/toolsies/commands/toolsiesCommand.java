package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.ConfigManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.tools.Tool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class toolsiesCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final ConfigManager configManager;

    public toolsiesCommand(Toolsies plugin, UserManager userManager, ConfigManager configManager) {
        plugin.getCommand("toolsies").setExecutor(this);
        this.userManager = userManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("toolsies.toolsies")) {
            sender.sendMessage("You don't have permission to do this.");
            return true;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("mysql")) {
                sender.sendMessage("Does " + args[1] + " exist in the database? " + (userManager.getUser(args[1]) != null ? "yes" : "no"));
            } else if (args[0].equalsIgnoreCase("config")) {
                sender.sendMessage(args[1] + " from config.yml: " + configManager.getConfig().get(args[1]));
            } else {
                sender.sendMessage("Unknown argument.");
            }
        } else {
            sender.sendMessage("MySQL connection: " + (Hikari.getHikari() != null ? ChatColor.GREEN + "yes" : ChatColor.RED + "no"));
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("toolsies.toolsies")) {
            return Collections.emptyList();
        }
        ArrayList<String> allarguments = new ArrayList<>(Arrays.asList("config", "mysql"));
        if (args.length == 1) {
            ArrayList<String> firstargument = new ArrayList<>();
            if (!args[0].isEmpty()) {
                for (String arg : allarguments) {
                    if (arg.toLowerCase().startsWith(args[0].toLowerCase())) {
                        firstargument.add(arg);
                    }
                }
            } else {
                return allarguments;
            }
            return firstargument;
        }
        return Collections.emptyList();
    }
}
