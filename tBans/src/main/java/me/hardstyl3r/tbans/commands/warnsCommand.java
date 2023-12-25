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
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class warnsCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    public warnsCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("warns").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.warns")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.warns")));
            return true;
        }
        if (!(sender instanceof Player)) {
            if (args.length == 0) {
                localeManager.sendUsage(sender, cmd, l);
                return true;
            }
        }
        if (args.length <= 1) {
            String target = sender.getName();
            ArrayList<Punishment> warns = punishmentManager.getPunishments(PunishmentType.WARN, target);
            if (args.length == 1) {
                target = args[0];
                if (!sender.hasPermission("toolsies.warns.others")) {
                    localeManager.sendUsage(sender, cmd, l);
                    return true;
                }
                if (userManager.getUser(target) == null) {
                    sender.sendMessage(l.getStringComponent("players.unknown", Placeholder.unparsed("player_name", args[0])));
                    return true;
                }
                warns = punishmentManager.getPunishments(PunishmentType.WARN, target);
            }
            warns.removeIf(punishmentManager::deleteIfExpired);
            if (warns.isEmpty()) {
                sender.sendMessage(l.getStringComponent("warns.no_warns" + ((sender.getName().equalsIgnoreCase(target)) ? "" : "_sender"), Placeholder.unparsed("player_name", target)));
                return true;
            }
            sender.sendMessage(l.getStringComponent("warns.warns" + ((sender.getName().equalsIgnoreCase(target)) ? "" : "_sender"), Placeholder.unparsed("warns", printWarns(warns)), Placeholder.unparsed("total", String.valueOf(warns.size())), Placeholder.unparsed("player_name", target)));
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    private String printWarns(ArrayList<Punishment> punishments) {
        StringBuilder sb = localeManager.getStringBuilder(true);
        for (int i = 0; i < punishments.size(); i++) {
            sb.append("#").append(punishments.get(i).getId());
            if (i < punishments.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.warns")) {
            if (args.length == 1) {
                Locale l = userManager.determineLocale(sender);
                if (sender.hasPermission("toolsies.warns.others")) {
                    return Collections.singletonList(localeManager.formatArgument(l.getString("common.player"), false));
                }
            }
        }
        return Collections.emptyList();
    }
}
