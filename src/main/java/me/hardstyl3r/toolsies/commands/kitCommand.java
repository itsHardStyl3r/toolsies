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

import java.util.Set;

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
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command does not work in console.");
            return true;
        }
        User u = userManager.getUser(sender);
        if (!sender.hasPermission("toolsies.kit")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    u.getLocale().getConfig().getString("no_permission")).replace("<permission>", "toolsies.kit"));
            return true;
        }
        if (args.length == 1) {
            String kit = args[0];
            if (!kitManager.isKit(kit) || !sender.hasPermission("toolsies.kits." + kit.toLowerCase())) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        u.getLocale().getConfig().getString("kit.unknown_kit")).replace("<name>", kit));
                return true;
            }
            Player p = (Player) sender;
            kitManager.giveKit(p, kit);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    u.getLocale().getConfig().getString("kit.kit_applied")).replace("<name>", kit));
        } else {
            Set<String> all = kitManager.getKits(sender);
            if (all.isEmpty()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        u.getLocale().getConfig().getString("kit.no_kits_available")));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        u.getLocale().getConfig().getString("kit.available_kits")).replace("<kits>", all.toString()));
            }
        }
        return true;
    }
}
