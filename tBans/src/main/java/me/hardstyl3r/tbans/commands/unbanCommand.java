package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class unbanCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    public unbanCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("unban").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.unban")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.unban")));
            return true;
        }
        if (args.length == 1) {
            String target = args[0];
            punishmentManager.deleteIfExpired(PunishmentType.BAN, target);
            if (!punishmentManager.isPunished(PunishmentType.BAN, target)) {
                sender.sendMessage(l.getStringComponent("punishments.player_is_not_banned", Placeholder.unparsed("player_name", target)));
                return true;
            }
            Punishment ban = punishmentManager.getPunishment(PunishmentType.BAN, target);
            punishmentManager.deletePunishment(ban, sender.getName());
            sender.sendMessage(l.getStringComponent("unban.unban", Placeholder.unparsed("player_name", ban.getName())));
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.unban")) {
            if (args.length == 1) {
                Locale l = userManager.determineLocale(sender);
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.player"), true));
            }
        }
        return Collections.emptyList();
    }
}
