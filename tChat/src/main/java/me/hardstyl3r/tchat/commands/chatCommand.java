package me.hardstyl3r.tchat.commands;

import me.hardstyl3r.tchat.TChat;
import me.hardstyl3r.tchat.managers.ChatManager;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class chatCommand implements CommandExecutor, TabCompleter {

    private final LocaleManager localeManager;
    private final UserManager userManager;
    private final ChatManager chatManager;

    public chatCommand(TChat plugin, UserManager userManager, LocaleManager localeManager, ChatManager chatManager) {
        plugin.getCommand("chat").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.chatManager = chatManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.chat")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getString("no_permission")).replace("<permission>", "toolsies.chat"));
            return true;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("clear") && sender.hasPermission("toolsies.chat.clear")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    for (int i = 0; i < 100; i++) p.sendMessage(" ");
                    for (String s : userManager.determineLocale(p).getStringList("chat.clear.broadcast"))
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', s)
                                .replace("<nick>", sender.getName()));
                }
            } else if ((args[0].equalsIgnoreCase("toggle") || args[0].equalsIgnoreCase("lock"))
                    && sender.hasPermission("toolsies.chat.toggle")) {
                chatManager.toggleLocked();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    for (String s : userManager.determineLocale(p).getStringList("chat.toggle.broadcast"))
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', s
                                        .replace("<type>", l.getString("chat.toggle.types." + chatManager.isLocked())))
                                .replace("<nick>", sender.getName()));
                }
            } else {
                localeManager.sendUsage(sender, cmd, l);
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender.hasPermission("toolsies.chat") && args.length == 1) {
            ArrayList<String> allarguments = new ArrayList<>(Arrays.asList("clear", "toggle"));
            allarguments.removeIf(s -> !sender.hasPermission("toolsies.chat." + s));
            return allarguments;
        }
        return Collections.emptyList();
    }
}
