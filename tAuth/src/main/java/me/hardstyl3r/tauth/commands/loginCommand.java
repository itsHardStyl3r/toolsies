package me.hardstyl3r.tauth.commands;

import me.hardstyl3r.tauth.TAuth;
import me.hardstyl3r.tauth.enums.AuthType;
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

public class loginCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final LoginManager loginManager;
    private final LoginManagement loginManagement;

    public loginCommand(TAuth plugin, UserManager userManager, LocaleManager localeManager, LoginManager loginManager, LoginManagement loginManagement) {
        plugin.getCommand("login").setExecutor(this);
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
        if (!sender.hasPermission("toolsies.login")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.login")));
            return true;
        }
        if (args.length == 1) {
            Player p = (Player) sender;
            AuthUser authUser = loginManager.getAuth(p);
            if (authUser != null) {
                if (authUser.isLoggedIn()) {
                    sender.sendMessage(l.getStringComponent("login.already_logged_in"));
                    return true;
                }
            }
            String password = args[0];
            if (loginManager.passwordMatches(authUser, password)) {
                loginManagement.performLogin(p);
                sender.sendMessage(l.getStringComponent("login.logged_in"));
            } else {
                sender.sendMessage(l.getStringComponent("login.incorrect_password"));
                loginManager.pushAuthHistory(authUser, AuthType.LOGIN, false, null);
            }
        } else {
            sender.sendMessage(l.getStringComponent("login.usage"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList(localeManager.formatArgument(userManager.determineLocale(sender).getString("common.password"), true));
        }
        return Collections.emptyList();
    }
}
