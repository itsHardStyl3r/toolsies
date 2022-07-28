package me.hardstyl3r.tchat.commands;

import me.hardstyl3r.tchat.TChat;
import me.hardstyl3r.tchat.managers.MessagingManagement;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class msgCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final MessagingManagement messagingManagement;

    public msgCommand(TChat plugin, UserManager userManager, LocaleManager localeManager, MessagingManagement messagingManagement) {
        plugin.getCommand("msg").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.messagingManagement = messagingManagement;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.msg")) {
            sender.sendMessage(l.getColoredString("no_permission").replace("<permission>", "toolsies.msg"));
            return true;
        }
        if (args.length > 1) {
            if (userManager.getUser(args[0]) == null) {
                sender.sendMessage(l.getColoredString("players.unknown").replace("<name>", args[0]));
                return true;
            }
            if (Bukkit.getPlayerExact(args[0]) == null) {
                sender.sendMessage(l.getColoredString("players.offline").replace("<name>", args[0]));
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            String message = localeManager.createMessage(args, 1);
            messagingManagement.sendMessage(sender, target, message, l);
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.msg")) {
            if (args.length == 1) {
                return null;
            } else {
                Locale l = userManager.determineLocale(sender);
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.message"), true));
            }
        }
        return Collections.emptyList();
    }
}
