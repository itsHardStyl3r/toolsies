package me.hardstyl3r.tbans.commands;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class banlistCommand implements CommandExecutor {

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
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    l.getConfig().getString("no_permission")).replace("<permission>", "toolsies.banlist"));
            return true;
        }
        if (args.length <= 1) {
            int arg = (args.length == 0 ? 0 : Integer.parseInt(args[0]));
            Bukkit.getScheduler().runTaskAsynchronously(TBans.getInstance(), () -> {
                Connection connection = null;
                PreparedStatement p = null;
                String pages = "SELECT COUNT(*) AS 'pages' FROM `punishments` WHERE `type`='BAN' AND (`duration` IS NULL OR `duration`>=UNIX_TIMESTAMP()*1000);";
                String page = "SELECT `name`, `date`, `duration` FROM `punishments` WHERE `type`='BAN' AND (`duration` IS NULL OR `duration`>=UNIX_TIMESTAMP()*1000) ORDER BY `date` DESC, `duration` DESC LIMIT 5 OFFSET " + arg * 5 + ";";
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
                    if (maxpages < arg) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("banlist.unknown_page")));
                        return;
                    }
                    p = connection.prepareCall(page);
                    p.execute();
                    rs = p.getResultSet();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("banlist.banlist_header"))
                            .replace("<page>", String.valueOf(arg))
                            .replace("<maxpages>", String.valueOf(maxpages))
                            .replace("<total>", String.valueOf(total)));
                    long current = System.currentTimeMillis();
                    while (rs.next()) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getConfig().getString("banlist.banlist_" + (rs.getLong("duration") != 0L ? "tempban" : "ban") + "_entry"))
                                .replace("<name>", rs.getString("name"))
                                .replace("<date>", localeManager.getFullDate(rs.getLong("date")))
                                .replace("<duration>", localeManager.parseTimeWithTranslate((rs.getLong("duration") - current), l)));
                    }
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("banlist.banlist_footer")));
                } catch (SQLException e) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            l.getConfig().getString("banlist.failed")));
                    LogUtil.error("banlistCommand(): " + e + ".");
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
}