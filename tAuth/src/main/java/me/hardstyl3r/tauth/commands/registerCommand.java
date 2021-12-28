package me.hardstyl3r.tauth.commands;

import me.hardstyl3r.tauth.TAuth;
import me.hardstyl3r.tauth.enums.AuthType;
import me.hardstyl3r.tauth.events.PlayerAuthSuccessfulEvent;
import me.hardstyl3r.tauth.managers.LoginManager;
import me.hardstyl3r.tauth.objects.AuthUser;
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

import java.util.Collections;
import java.util.List;

public class registerCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final LoginManager loginManager;

    public registerCommand(TAuth plugin, UserManager userManager, LocaleManager localeManager, LoginManager loginManager) {
        plugin.getCommand("register").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.loginManager = loginManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getString("console_sender")));
            return true;
        }
        if (!sender.hasPermission("toolsies.register")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getString("no_permission")).replace("<permission>", "toolsies.register"));
            return true;
        }
        if (args.length == 2) {
            Player p = (Player) sender;
            AuthUser user = loginManager.getAuth(p);
            if (user != null) {
                if (user.isRegistered()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getString("register.already_registered")));
                    return true;
                }
            }
            if (loginManager.validatePassword(sender, args[0], args[1], l)) return true;
            if (loginManager.register(p, args[0])) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("register.registered")));
                Bukkit.getPluginManager().callEvent(new PlayerAuthSuccessfulEvent(p, user, AuthType.REGISTER));
                loginManager.stopKickTask(p);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getString("register.register_failed")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getString("register.usage")));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            sender.sendMessage(l.getString("console_sender"));
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return Collections.singletonList(localeManager.formatArgument(l.getString("common.password"), true));
        } else if (args.length == 2) {
            return Collections.singletonList(localeManager.formatArgument(l.getString("common.confirmPassword"), true));
        }
        return Collections.emptyList();
    }
}
