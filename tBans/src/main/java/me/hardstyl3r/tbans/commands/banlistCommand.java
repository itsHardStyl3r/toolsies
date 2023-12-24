package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.LogUtil;
import me.hardstyl3r.toolsies.utils.StringUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class banlistCommand implements CommandExecutor, TabCompleter {

    private final UserManager userManager;
    private final LocaleManager localeManager;

    public banlistCommand(TBans plugin, UserManager userManager, LocaleManager localeManager) {
        plugin.getCommand("banlist").setExecutor(this);
        this.userManager = userManager;
        this.localeManager = localeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Locale l = userManager.determineLocale(sender);
        if (!sender.hasPermission("toolsies.banlist")) {
            sender.sendMessage(l.getStringComponent("no_permission", Placeholder.unparsed("permission", "toolsies.banlist")));
            return true;
        }
        if (args.length <= 1) {
            int arg = 0;
            if (args.length == 1) {
                if (!StringUtils.isNumeric(args[0])) {
                    sender.sendMessage(l.getStringComponent("banlist.unknown_page"));
                    return true;
                }
                arg = Integer.parseInt(args[0]);
            }
            int finalArg = arg;
            Bukkit.getScheduler().runTaskAsynchronously(TBans.getInstance(), () -> {
                Connection connection = null;
                PreparedStatement p = null;
                String pages = "SELECT COUNT(*) AS 'pages' FROM `punishments` WHERE `type`='BAN' AND (`duration` IS NULL OR `duration`>=UNIX_TIMESTAMP()*1000);";
                String page = "SELECT `name`, `date`, `duration` FROM `punishments` WHERE `type`='BAN' AND (`duration` IS NULL OR `duration`>=UNIX_TIMESTAMP()*1000) ORDER BY `date` DESC, `duration` DESC LIMIT 5 OFFSET " + finalArg * 5 + ";";
                ResultSet rs = null;
                try {
                    connection = Hikari.getHikari().getConnection();
                    p = connection.prepareCall(pages);
                    p.execute();
                    rs = p.getResultSet();
                    int total = 0;
                    while (rs.next()) {
                        total = rs.getInt("pages");
                    }
                    rs.close();
                    int maxpages = total / 5;
                    if (maxpages < finalArg) {
                        sender.sendMessage(l.getStringComponent("banlist.unknown_page"));
                        return;
                    }
                    p = connection.prepareCall(page);
                    p.execute();
                    rs = p.getResultSet();
                    sender.sendMessage(l.getStringComponent("banlist.banlist_header", Placeholder.unparsed("page", String.valueOf(finalArg)), Placeholder.unparsed("maxpages", String.valueOf(maxpages)), Placeholder.unparsed("total", String.valueOf(total))));
                    long current = System.currentTimeMillis();
                    while (rs.next()) {
                        sender.sendMessage(l.getStringComponent("banlist.banlist_" + (rs.getLong("duration") != 0L ? "tempban" : "ban") + "_entry", Placeholder.unparsed("player_name", rs.getString("name")), Placeholder.unparsed("ban_date", localeManager.getFullDate(rs.getLong("date"))), Placeholder.unparsed("duration", localeManager.parseTimeWithTranslate((rs.getLong("duration") - current), l))));
                    }
                    sender.sendMessage(l.getStringComponent("banlist.banlist_footer"));
                } catch (SQLException e) {
                    sender.sendMessage(l.getStringComponent("banlist.failed"));
                    LogUtil.error("[tBans] banlistCommand(): " + e + ".");
                    e.printStackTrace();
                } finally {
                    Hikari.close(connection, p, rs);
                }
            });
        } else {
            localeManager.sendUsage(sender, cmd, l);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("toolsies.banlist")) {
            if (args.length == 1) {
                Locale l = userManager.determineLocale(sender);
                return Collections.singletonList(localeManager.formatArgument(l.getString("common.page"), false));
            }
        }
        return Collections.emptyList();
    }
}
