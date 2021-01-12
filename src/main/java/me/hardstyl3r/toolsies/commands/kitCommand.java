package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.KitManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class kitCommand implements CommandExecutor {

    private final KitManager kitManager;
    private final UserManager userManager;

    public kitCommand(Toolsies plugin, UserManager userManager, KitManager kitManager) {
        plugin.getCommand("kit").setExecutor(this);
        this.userManager = userManager;
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("This command does not work in console.");
            return true;
        }
        User u = userManager.getUser(sender);
        if (!sender.hasPermission("toolsies.kit")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', u.getLocale().getConfig().getString("no_permission")).replace("<permission>", "toolsies.locale"));
            return true;
        }
        if (args.length == 1) {
            if (!kitManager.isKit(args[0])) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', u.getLocale().getConfig().getString("kit.unknown_kit")).replace("<name>", args[0]));
                return true;
            }
            Player p = (Player) sender;
            kitManager.giveKit(p, args[0]);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', u.getLocale().getConfig().getString("kit.kit_applied")).replace("<name>", args[0]));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', u.getLocale().getConfig().getString("kit.available_kits")).replace("<kits>", kitManager.getKits().toString()));
        }
        return true;
    }
}
