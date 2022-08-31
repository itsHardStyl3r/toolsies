package me.hardstyl3r.tauth.commands;

import me.hardstyl3r.tauth.TAuth;
import me.hardstyl3r.tauth.managers.LoginManagement;
import me.hardstyl3r.tauth.managers.LoginManager;
import me.hardstyl3r.tauth.objects.AuthUser;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class logoutCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final LoginManager loginManager;
    private final LoginManagement loginManagement;

    public logoutCommand(TAuth plugin, UserManager userManager, LocaleManager localeManager, LoginManager loginManager, LoginManagement loginManagement) {
        plugin.getCommand("logout").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.loginManager = loginManager;
        this.loginManagement = loginManagement;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            sender.sendMessage(l.getStringComponent("console_sender"));
            return true;
        }
        if (!sender.hasPermission("toolsies.logout")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.logout")));
            return true;
        }
        if (args.length == 0) {
            Player p = (Player) sender;
            AuthUser user = loginManager.getAuth(p);
            if (user != null) {
                if (!user.isLoggedIn()) {
                    sender.sendMessage(l.getStringComponent("logout.not_logged_in"));
                    return true;
                }
            }
            loginManagement.performLogout(p);
            sender.sendMessage(l.getStringComponent("logout.logged_out"));
        } else {
            sender.sendMessage(l.getStringComponent("logout.usage"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}
