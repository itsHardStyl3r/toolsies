package me.hardstyl3r.tauth.commands;

import me.hardstyl3r.tauth.TAuth;
import me.hardstyl3r.tauth.enums.AuthType;
import me.hardstyl3r.tauth.events.PlayerAuthSuccessfulEvent;
import me.hardstyl3r.tauth.managers.LoginManager;
import me.hardstyl3r.tauth.objects.AuthUser;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class changepasswordCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LoginManager loginManager;
    private final LocaleManager localeManager;

    public changepasswordCommand(TAuth plugin, UserManager userManager, LoginManager loginManager, LocaleManager localeManager) {
        plugin.getCommand("changepassword").setExecutor(this);
        this.userManager = userManager;
        this.loginManager = loginManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            sender.sendMessage(l.getString("console_sender"));
            return true;
        }
        if (!sender.hasPermission("toolsies.changepassword")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.changepassword")));
            return true;
        }
        if (args.length == 2) {
            Player p = (Player) sender;
            AuthUser authUser = loginManager.getAuth(p);
            String oldPassword = args[0];
            String newPassword = args[1];
            if (!loginManager.passwordMatches(authUser, oldPassword)) {
                sender.sendMessage(l.getStringComponent("changepassword.incorrect_password"));
                loginManager.pushAuthHistory(authUser, AuthType.CHANGEPW, false, null);
                return true;
            }
            if (loginManager.validatePassword(sender, newPassword, l)) return true;
            if (loginManager.changePassword(authUser, newPassword)) {
                sender.sendMessage(l.getStringComponent("changepassword.changed"));
                Bukkit.getPluginManager().callEvent(
                        new PlayerAuthSuccessfulEvent(p, authUser, AuthType.CHANGEPW));
                loginManager.pushAuthHistory(authUser, AuthType.CHANGEPW, true, null);
            } else {
                sender.sendMessage(l.getStringComponent("changepassword.change_failed"));
                loginManager.pushAuthHistory(authUser, AuthType.CHANGEPW, false, null);
            }
        } else {
            sender.sendMessage(l.getStringComponent("changepassword.usage"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (args.length == 1) {
            return Collections.singletonList(localeManager.formatArgument(l.getString("common.currentPassword"), true));
        } else if (args.length == 2) {
            return Collections.singletonList(localeManager.formatArgument(l.getString("common.newPassword"), true));
        }
        return Collections.emptyList();
    }
}