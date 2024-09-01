package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class warnCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final PunishmentManager punishmentManager;
    private final LocaleManager localeManager;
    private final PunishmentType type = PunishmentType.WARN;

    /**
     * A command to warn a player.
     * Permissions: 'toolsies.warn', 'toolsies.warn.bypasspriority'
     */
    public warnCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("warn").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.warn")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.warn")));
            return true;
        }
        if (args.length > 1) {
            String target = args[0];
            if (!punishmentManager.canSenderPunishTarget(sender, target, type)) {
                sender.sendMessage(l.getStringComponent("warn.priority_too_high"));
                return true;
            }
            if (userManager.getUser(target) == null) {
                sender.sendMessage(l.getStringComponent("players.unknown", Placeholder.unparsed("player_name", args[0])));
                return true;
            }
            long duration = 0;
            String admin = sender.getName();
            String reason = localeManager.createMessage(args, 1);
            if (args.length > 2 && localeManager.isValidStringTime(args[1])) {
                duration = localeManager.parseTimeFromString(args[1]);
                long minimumDuration = punishmentManager.getMinimumDuration(type);
                if (duration < minimumDuration) {
                    sender.sendMessage(l.getStringComponent("punishments.duration_too_short",
                            Placeholder.unparsed("duration", localeManager.parseTimeWithTranslate(minimumDuration, l))));
                    return true;
                }
                reason = localeManager.createMessage(args, 2);
            }
            UUID uuid = userManager.getUserIgnoreCase(target).getUUID();
            Punishment punishment = punishmentManager.createPunishment(type, uuid, target, admin, reason, duration);
            sender.sendMessage(l.getStringComponent("warn.player_warned",
                    Placeholder.unparsed("player_name", target),
                    Formatter.choice("choice", duration),
                    Placeholder.unparsed("duration", localeManager.parseTimeWithTranslate(duration, l))));
            Player p = Bukkit.getPlayerExact(target);
            if (p != null) {
                Locale victimLocale = userManager.determineLocale(p);
                p.sendMessage(victimLocale.getStringComponent("warn.warn_notification",
                        Placeholder.unparsed("sender_name", punishment.getSender()),
                        Placeholder.unparsed("reason", reason)));
            }
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender.hasPermission("toolsies.warn")) {
            Locale l = userManager.determineLocale(sender);
            if (args.length == 1) {
                return null;
            } else if (args.length == 2) {
                return Collections.singletonList(localeManager.formatArgument(localeManager.getConfig().getString("duration_argument") + " | " + l.getString("common.reason"), true));
            } else {
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.reason"), true));
            }
        }
        return Collections.emptyList();
    }
}
