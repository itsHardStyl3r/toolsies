package me.hardstyl3r.toolsies.commands;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.User;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class toolsiesCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;

    public toolsiesCommand(Toolsies plugin, UserManager userManager) {
        plugin.getCommand("toolsies").setExecutor(this);
        this.userManager = userManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            User u = userManager.getUser(sender);
            if (!sender.hasPermission("toolsies.toolsies")) {
                sender.sendMessage(u.getLocale().getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.toolsies")));
                return true;
            }
        }
        sender.sendMessage(ChatColor.GREEN + "nifty gifties");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}
