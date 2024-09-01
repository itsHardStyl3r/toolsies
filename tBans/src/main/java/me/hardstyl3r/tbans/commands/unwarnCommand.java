package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.StringUtils;
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

public class unwarnCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;

    /**
     * A command to remove a warning using ID.
     * Permissions: 'toolsies.unwarn'
     */
    public unwarnCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("unwarn").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.unwarn")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.unwarn")));
            return true;
        }
        if (args.length == 1) {
            if (!StringUtils.isNumeric(args[0])) {
                sender.sendMessage(l.getStringComponent("unwarn.wrong_id"));
                return true;
            }
            int i = Integer.parseInt(args[0]);
            Punishment warn = punishmentManager.getPunishmentById(i);
            if (warn == null || warn.getType() != PunishmentType.WARN) {
                sender.sendMessage(l.getStringComponent("unwarn.no_such_warn", Placeholder.unparsed("id", args[0])));
                return true;
            }
            punishmentManager.deletePunishment(warn);
            sender.sendMessage(l.getStringComponent("unwarn.player_unwarned",
                    Placeholder.unparsed("id", args[0]),
                    Placeholder.unparsed("player_name", warn.getName())));
            Player p = Bukkit.getPlayer(warn.getUUID());
            if (p != null) {
                p.sendMessage(userManager.determineLocale(p).getStringComponent("unwarn.unwarn_notification",
                        Placeholder.unparsed("sender_name", sender.getName()),
                        Placeholder.unparsed("id", args[0])));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender.hasPermission("toolsies.unwarn")) {
            if (args.length == 1) {
                List<String> arguments = new ArrayList<>();
                for (Punishment mute : punishmentManager.getPunishmentsByTypes(PunishmentType.WARN))
                    arguments.add(String.valueOf(mute.getId()));
                return localeManager.formatTabArguments(args[0], arguments);
            }
        }
        return Collections.emptyList();
    }
}
