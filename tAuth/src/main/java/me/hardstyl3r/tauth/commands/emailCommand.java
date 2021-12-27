package me.hardstyl3r.tauth.commands;

import me.hardstyl3r.tauth.TAuth;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class emailCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final LoginManager loginManager;
    private final FileConfiguration emailConfig;

    public emailCommand(TAuth plugin, UserManager userManager, LocaleManager localeManager, LoginManager loginManager, FileConfiguration emailConfig) {
        plugin.getCommand("email").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
        this.loginManager = loginManager;
        this.emailConfig = emailConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getConfig().getString("console_sender")));
            return true;
        }
        if (!sender.hasPermission("toolsies.email")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.login"));
            return true;
        }
        Player p = (Player) sender;
        AuthUser authUser = loginManager.getAuth(p);
        String currentEmail = authUser.getEmail();
        if (args.length == 0) {
            if (currentEmail == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("email.email_no_email")));
                return true;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("email.email")).replace("<email>", currentEmail));
        } else if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("set") && args.length == 2) {
                String email = args[1].toLowerCase();
                String domain = (email.contains("@") ? (email.split("@").length >= 2 ? email.split("@")[1] : "") : "").toLowerCase();
                if (email.equalsIgnoreCase(currentEmail)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("email.email_set_current")));
                    return true;
                }
                if (emailConfig.getBoolean("email.regexEnable") && !email.matches(emailConfig.getString("email.regex"))) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("email.email_incorrect")));
                    return true;
                }
                if (!sender.hasPermission("toolsies.email.set.bypass") && emailConfig.getStringList("email.disallowedEmails").contains(email)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("email.email_disallowed")));
                    return true;
                }
                if (email.length() <= emailConfig.getInt("email.minEmailLength")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("email.email_too_short")));
                    return true;
                }
                if (email.length() >= emailConfig.getInt("email.maxEmailLength")) {
                    Bukkit.broadcastMessage(emailConfig.getInt("email.maxEmailLength") + "");
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("email.email_too_long")));
                    return true;
                }
                if (emailConfig.getBoolean("email.blacklistDomainsEnable") && emailConfig.getStringList("email.blacklistDomains").contains(domain)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("email.email_disallowed_domain")));
                    return true;
                }
                if (emailConfig.getBoolean("email.whitelistDomainsEnable") && !emailConfig.getStringList("email.whitelistDomains").contains(domain)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("email.email_disallowed_domain")));
                    return true;
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("email.email_set")));
                authUser.setEmail(email);
                loginManager.updateAuth(authUser);
            } else if (args[0].equalsIgnoreCase("clear")) {
                if (currentEmail == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("email.email_no_email")));
                    return true;
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        l.getConfig().getString("email.email_clear")));
                authUser.setEmail(null);
                loginManager.updateAuth(authUser);
            } else {
                localeManager.sendUsage(sender, cmd, l);
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.email")) {
            ArrayList<String> allarguments = new ArrayList<>(Arrays.asList("set", "clear"));
            Locale l = userManager.determineLocale(sender);
            if (args.length == 1) return localeManager.formatTabArguments(args[0], allarguments);
            if (args.length == 2 && args[0].equalsIgnoreCase("set"))
                return Collections.singletonList(localeManager.formatArgument(l.getConfig().getString("common.email"), true));
        }
        return Collections.emptyList();
    }
}
