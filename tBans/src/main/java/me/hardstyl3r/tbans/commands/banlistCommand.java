package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.managers.PunishmentManager;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.LogUtil;
import me.hardstyl3r.toolsies.utils.StringUtils;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class banlistCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;
    private final PunishmentManager punishmentManager;
    private final FileConfiguration config;

    /**
     * A command to display and search through list of bans. Does not include IP addresses for privacy reasons.
     * Permissions: 'toolsies.banlist'
     */
    public banlistCommand(TBans plugin, UserManager userManager, PunishmentManager punishmentManager, LocaleManager localeManager, FileConfiguration config) {
        plugin.getCommand("banlist").setExecutor(this);
        this.userManager = userManager;
        this.punishmentManager = punishmentManager;
        this.localeManager = localeManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.banlist")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.banlist")));
            return true;
        }
        int page = 0;
        ArrayList<Punishment> bans = punishmentManager.getPunishmentsByTypes(PunishmentType.BAN);
        ArrayList<Punishment> filteredBans = new ArrayList<>();
        ArrayList<String> senders = new ArrayList<>(), targets = new ArrayList<>();
        for (String argument : args) {
            if (argument.startsWith("s:")) {
                senders.add(argument.split("s:")[1]);
                continue;
            }
            if (argument.startsWith("t:")) {
                targets.add(argument.split("t:")[1]);
                continue;
            }
            if (!StringUtils.isNumeric(argument)) {
                sender.sendMessage(l.getStringComponent("banlist.unknown_page"));
                return true;
            }
            page = Integer.parseInt(argument) - 1;
            if (page < 0) page = 0;
        }
        if (!senders.isEmpty() && !targets.isEmpty()) {
            bans.removeIf(punishment -> !(targets.contains(punishment.getName()) && senders.contains(punishment.getSender())));
        } else {
            for (Punishment ban : bans) {
                for (String s : senders) {
                    if (ban.getSender().equalsIgnoreCase(s)) filteredBans.add(ban);
                }
                for (String t : targets) {
                    if (ban.getName().equalsIgnoreCase(t)) filteredBans.add(ban);
                }
            }
        }
        if (!filteredBans.isEmpty()) bans = filteredBans;
        if (bans.isEmpty()) {
            sender.sendMessage(l.getStringComponent("banlist.empty"));
            return true;
        }
        bans.sort((Comparator.comparing(Punishment::getDate)).reversed());
        int resultsPerPage = 5;
        try {
            resultsPerPage = config.getInt("banlistResultsPerPage");
        } catch (Exception e) {
            LogUtil.warn("[tBans] Failed to read banlistResultsPerPage from config.yml.");
        }
        if ((resultsPerPage * page) >= bans.size()) {
            page = 0;
        }
        sender.sendMessage(l.getStringComponent("banlist.header",
                Placeholder.unparsed("page", page + 1 + ""),
                Placeholder.unparsed("pages", (bans.size() + resultsPerPage - 1) / resultsPerPage + ""),
                Placeholder.unparsed("total", bans.size() + "")));
        for (int i = (resultsPerPage * page); i < (resultsPerPage * (page + 1)); i++) {
            if (i >= bans.size()) break;
            Punishment ban = bans.get(i);
            sender.sendMessage(l.getStringComponent("banlist.entry",
                            Placeholder.unparsed("player_name", ban.getName()),
                            Placeholder.unparsed("sender_name", ban.getSender()),
                            Placeholder.unparsed("ban_date", localeManager.getFullDate(ban.getDate())),
                            Placeholder.unparsed("number", i + 1 + ""))
                    .hoverEvent(HoverEvent.showText(l.getStringComponent("banlist.entry_hover",
                            Placeholder.unparsed("player_name", ban.getName()),
                            Placeholder.unparsed("reason", (ban.getReason() == null ? l.getString("common.none") : ban.getReason())),
                            Placeholder.unparsed("duration", localeManager.parseTimeWithTranslate(ban.getRemaining(), l)),
                            Placeholder.unparsed("ban_id", String.valueOf(ban.getId())),
                            Formatter.choice("choice", ban.getRemaining())))));
        }
        sender.sendMessage(l.getStringComponent("banlist.footer"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender.hasPermission("toolsies.banlist")) {
            Locale l = userManager.determineLocale(sender);
            return Collections.singletonList(localeManager.formatArgument(l.getString("common.page") + " | " +
                            "s:<" + l.getString("common.player") + "> | t:<" + l.getString("common.player") + "/" + l.getString("common.ip") + ">",
                    false));
        }
        return Collections.emptyList();
    }
}
