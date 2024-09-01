package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.StringUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.hardstyl3r.tbans.enums.PunishmentType.BAN;
import static me.hardstyl3r.tbans.enums.PunishmentType.IP;

public class unbanCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    /**
     * A command to unban players and IP addresses.
     * Permissions: 'toolsies.unban', 'toolsies.unban.ip'.
     */
    public unbanCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("unban").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.unban")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.unban")));
            return true;
        }
        if (args.length == 1) {
            String target = args[0];
            ArrayList<Punishment> bans = punishmentManager.getPunishmentsByName(target, BAN);
            if (sender.hasPermission("toolsies.unban.ip"))
                bans.addAll(punishmentManager.getPunishmentsByName(target, IP));
            PunishmentType type = (StringUtils.isIPv4(target) && sender.hasPermission("toolsies.unban.ip") ? IP : BAN);
            if (bans.isEmpty()) {
                sender.sendMessage(l.getStringComponent("punishments.target_is_not_banned",
                        Placeholder.unparsed("target_name", target),
                        Formatter.choice("target", (type == IP ? 1 : 0))));
                return true;
            }
            for (Punishment ban : bans) punishmentManager.deletePunishment(ban);
            sender.sendMessage(l.getStringComponent("unban.target_unbanned",
                    Placeholder.unparsed("target_name", target),
                    Formatter.choice("target", (type == IP ? 1 : 0))));
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (type == IP) break;
                Locale locale = userManager.determineLocale(p);
                p.sendMessage(locale.getStringComponent("unban.player_unbanned_broadcast",
                        Placeholder.unparsed("player_name", target),
                        Placeholder.unparsed("sender_name", sender.getName())));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender.hasPermission("toolsies.unban")) {
            if (args.length == 1) {
                ArrayList<String> bans = new ArrayList<>();
                for (Punishment ban : punishmentManager.getPunishmentsByTypes(BAN, IP)) {
                    if (ban.getType() == BAN) bans.add(ban.getName());
                    if (sender.hasPermission("toolsies.unban.ip") && ban.getType() == IP) bans.add(ban.getName());
                }
                return localeManager.formatTabArguments(args[0], bans);
            }
        }
        return Collections.emptyList();
    }
}
