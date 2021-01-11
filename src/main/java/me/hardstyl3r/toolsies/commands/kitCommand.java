package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.KitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class kitCommand implements CommandExecutor {

    private final KitManager kitManager;

    public kitCommand(Toolsies plugin, KitManager kitManager) {
        plugin.getCommand("kit").setExecutor(this);
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("toolsies.kit")) {
            sender.sendMessage("You don't have permission to do this.");
            return true;
        }
        if (args.length == 1) {
            if (!kitManager.isKit(args[0])) {
                sender.sendMessage("Kit " + args[0] + " does not exist.");
                return true;
            }
            Player p = (Player) sender;
            kitManager.giveKit(p, args[0]);
            sender.sendMessage("Kit " + args[0].toLowerCase() + " has been added to your inventory.");
        } else {
            sender.sendMessage("Available kits: " + kitManager.getKits().toString());
        }
        return false;
    }
}
