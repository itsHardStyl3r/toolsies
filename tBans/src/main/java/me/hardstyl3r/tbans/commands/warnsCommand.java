package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class warnsCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final PunishmentManager punishmentManager;

    /**
     * A command to display all current warns.
     * Permissions: 'toolsies.warns', 'toolsies.warns.others'
     */
    public warnsCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager) {
        plugin.getCommand("warns").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.warns")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.warns")));
            return true;
        }
        String target = sender.getName();
        if (args.length > 0 && sender.hasPermission("toolsies.warns.others")) {
            target = args[0];
            if (userManager.getUser(target) == null) {
                sender.sendMessage(l.getStringComponent("players.unknown", Placeholder.unparsed("player_name", args[0])));
                return true;
            }
        }
        ArrayList<Punishment> warns = punishmentManager.getPunishmentsByName(target, PunishmentType.WARN);
        if (warns.isEmpty()) {
            sender.sendMessage(l.getStringComponent("warns.empty"));
            return true;
        }
        warns.sort((Comparator.comparing(Punishment::getDate)).reversed());
        sender.sendMessage(l.getStringComponent((target.equals(sender.getName()) ? "warns.header_self" : "warns.header"),
                Placeholder.unparsed("total", String.valueOf(warns.size())),
                Placeholder.unparsed("player_name", target)));
        for (Punishment w : warns) {
            sender.sendMessage(l.getStringComponent("warns.entry",
                            Placeholder.unparsed("reason", w.getReason()),
                            Placeholder.unparsed("sender_name", w.getSender()))
                    .hoverEvent(HoverEvent.showText(l.getStringComponent("warns.entry_hover",
                            Placeholder.unparsed("duration", localeManager.parseTimeWithTranslate(w.getRemaining(), l)),
                            Placeholder.unparsed("warn_id", String.valueOf(w.getId())),
                            Placeholder.unparsed("warn_date", localeManager.getFullDate(w.getDate())),
                            Formatter.choice("choice", w.getRemaining())))));
        }
        sender.sendMessage(l.getStringComponent("warns.footer"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender.hasPermission("toolsies.warns.others")) return null;
        return Collections.emptyList();
    }
}
