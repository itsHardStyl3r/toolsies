package me.hardstyl3r.tbans.managers;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.events.PlayerPunishedEvent;
import me.hardstyl3r.tbans.events.PlayerUnpunishedEvent;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.utils.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PunishmentManager {

    private final LocaleManager localeManager;
    private final FileConfiguration config;

    public PunishmentManager(LocaleManager localeManager, FileConfiguration config) {
        this.localeManager = localeManager;
        this.config = config;
        loadPunishments();
    }

    private final Set<Punishment> punishments = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void loadPunishments() {
        punishments.clear();
        Connection connection = null;
        PreparedStatement p = null;
        ResultSet rs = null;

        String call = "SELECT `id`, `uuid`, `name`, IF(IS_IPV6(UNHEX(`name`)), INET6_NTOA(UNHEX(`name`)), INET_NTOA(UNHEX(`name`))) AS 'ipname', `type`, `admin`, `reason`, `date`, `duration` FROM `punishments` WHERE (`duration` IS NULL OR `duration`>=UNIX_TIMESTAMP()*1000);";
        try {
            connection = Hikari.getHikari().getConnection();
            p = connection.prepareCall(call);
            p.execute();
            rs = p.getResultSet();
            while (rs.next()) {
                Punishment punishment;
                if (PunishmentType.valueOf(rs.getString("type")) == PunishmentType.IP) {
                    punishment = new Punishment(rs.getString("ipname"), PunishmentType.IP);
                } else if (PunishmentType.valueOf(rs.getString("type")) != null) {
                    punishment = new Punishment(rs.getString("name"), PunishmentType.valueOf(rs.getString("type")));
                } else {
                    LogUtil.warn("loadPunishments(): Unknown type " + rs.getString("type") + " for " + rs.getString("name") + ". Skipping.");
                    continue;
                }
                punishment.setId(rs.getInt("id"));
                if (rs.getString("uuid") != null) {
                    punishment.setUUID(UUID.fromString(rs.getString("uuid")));
                }
                punishment.setAdmin(rs.getString("admin"));
                punishment.setDate(rs.getLong("date"));
                punishment.setDuration((rs.getLong("duration") == 0L ? null : rs.getLong("duration")));
                punishment.setReason(rs.getString("reason"));
                punishments.add(punishment);
            }
        } catch (SQLException e) {
            LogUtil.error("loadPunishments(): " + e + ".");
        } finally {
            LogUtil.info("loadPunishments(): Loaded " + punishments.size() + " punishments.");
            Hikari.close(connection, p, rs);
        }
    }

    public Punishment createPunishment(PunishmentType type, UUID uuid, String name, String sender, String reason, Long duration) {
        Punishment punishment = new Punishment(name, type);
        if (uuid != null) {
            punishment.setUUID(uuid);
        }
        punishment.setAdmin(sender);
        punishment.setReason(reason);
        if (duration == null) {
            punishment.setDuration(duration);
        } else {
            punishment.setDuration(System.currentTimeMillis() + duration);
        }
        punishments.add(punishment);
        Bukkit.getPluginManager().callEvent(new PlayerPunishedEvent(punishment, sender));
        pushPunishment(punishment);
        pushPunishmentHistory(punishment);
        return punishment;
    }

    public Punishment createPunishment(InetAddress address, String sender, String reason, Long duration) {
        Punishment punishment = new Punishment(address.getHostAddress(), PunishmentType.IP);
        punishment.setAdmin(sender);
        punishment.setReason(reason);
        if (duration == null) {
            punishment.setDuration(duration);
        } else {
            punishment.setDuration(System.currentTimeMillis() + duration);
        }
        punishments.add(punishment);
        Bukkit.getPluginManager().callEvent(new PlayerPunishedEvent(punishment, sender));
        pushPunishment(punishment);
        pushPunishmentHistory(punishment);
        return punishment;
    }

    public Set<Punishment> getPunishments() {
        return punishments;
    }

    public ArrayList<Punishment> getPunishments(PunishmentType type, String name) {
        ArrayList<Punishment> current = new ArrayList<>();
        for (Punishment punishment : punishments) {
            if (punishment.getType().equals(type) && punishment.getName().equalsIgnoreCase(name)) {
                current.add(punishment);
            }
        }
        return current;
    }

    public ArrayList<Punishment> getPunishments(PunishmentType type, UUID uuid) {
        ArrayList<Punishment> current = new ArrayList<>();
        for (Punishment punishment : punishments) {
            if (punishment.getType().equals(type)) {
                if (punishment.getUUID() != null && punishment.getUUID().equals(uuid)) {
                    current.add(punishment);
                }
            }
        }
        return current;
    }

    public Punishment getPunishmentById(PunishmentType type, Integer i) {
        for (Punishment punishment : punishments) {
            if (punishment.getType().equals(type)) {
                if (punishment.getId() == i) return punishment;
            }
        }
        return null;
    }

    public Punishment getPunishment(PunishmentType type, String name) {
        if (type == PunishmentType.ANY || type == PunishmentType.WARN) {
            //You should not use this method to retrieve ANY or WARN, there can be more than one.
            return null;
        }
        ArrayList<Punishment> list = getPunishments(type, name);
        return list.isEmpty() ? null : list.get(0);
    }

    public Punishment getPunishment(PunishmentType type, UUID uuid) {
        if (type == PunishmentType.ANY || type == PunishmentType.WARN) {
            return null;
        }
        ArrayList<Punishment> list = getPunishments(type, uuid);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean isPunished(PunishmentType type, String name) {
        return getPunishment(type, name) != null;
    }

    public boolean isPunished(PunishmentType type, UUID uuid) {
        return getPunishment(type, uuid) != null;
    }

    public Punishment getBan(InetAddress address) {
        ArrayList<Punishment> list = getPunishments(PunishmentType.IP, address.getHostAddress());
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean isBanned(InetAddress address) {
        return getBan(address) != null;
    }

    public void deletePunishment(Punishment punishment, String sender) {
        if (punishments.contains(punishment)) {
            if (sender != null) {
                Bukkit.getPluginManager().callEvent(new PlayerUnpunishedEvent(punishment, sender));
            }
            punishments.remove(punishment);
            pushDeletePunishment(punishment);
        }
    }

    public boolean deleteIfExpired(Punishment punishment) {
        if (punishment != null) {
            if (punishment.getDuration() != null) {
                if (punishment.getDuration() <= System.currentTimeMillis()) {
                    deletePunishment(punishment, null);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteIfExpired(PunishmentType type, String name) {
        Punishment punishment = getPunishment(type, name);
        if (punishment != null) {
            return deleteIfExpired(punishment);
        } else {
            return false;
        }
    }

    public boolean deleteIfExpired(PunishmentType type, UUID uuid) {
        Punishment punishment = getPunishment(type, uuid);
        if (punishment != null) {
            return deleteIfExpired(punishment);
        } else {
            return false;
        }
    }

    public boolean deleteIfExpired(InetAddress address) {
        Punishment punishment = getBan(address);
        if (punishment != null) {
            return deleteIfExpired(punishment);
        } else {
            return false;
        }
    }

    private void pushPunishment(Punishment punishment) {
        Bukkit.getScheduler().runTaskAsynchronously(TBans.getInstance(), () -> {
            Connection connection = null;
            PreparedStatement p = null;
            String update = "INSERT INTO `punishments` (`uuid`, `name`, `type`, `admin`, `reason`, `date`, `duration`) VALUES(?, " + (punishment.getType().equals(PunishmentType.IP) ? "HEX(IF(IS_IPV6(?), INET6_ATON(?), INET_ATON(?)))" : "?") + ", ?, ?, ?, ?, ?);";
            ResultSet key = null;
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareStatement(update, Statement.RETURN_GENERATED_KEYS);
                p.setString(1, punishment.getUUID() == null ? null : punishment.getUUID().toString());
                p.setString(2, punishment.getName());
                if (punishment.getType().equals(PunishmentType.IP)) {
                    p.setString(3, punishment.getName());
                    p.setString(4, punishment.getName());
                    p.setString(5, punishment.getType().name());
                    p.setString(6, punishment.getAdmin());
                    p.setString(7, punishment.getReason());
                    p.setLong(8, punishment.getDate());
                    if (punishment.getDuration() != null) {
                        p.setLong(9, punishment.getDuration());
                    } else {
                        p.setNull(9, Types.BIGINT);
                    }
                } else {
                    p.setString(3, punishment.getType().name());
                    p.setString(4, punishment.getAdmin());
                    p.setString(5, punishment.getReason());
                    p.setLong(6, punishment.getDate());
                    if (punishment.getDuration() != null) {
                        p.setLong(7, punishment.getDuration());
                    } else {
                        p.setNull(7, Types.BIGINT);
                    }
                }
                p.execute();
                key = p.getGeneratedKeys();
                key.next();
                punishment.setId(key.getInt(1));
                LogUtil.info("pushPunishment(): Created new " + punishment.getType().toString() + " " + punishment.getName() + ".");
            } catch (SQLException e) {
                LogUtil.error("pushPunishment(): " + e + ".");
            } finally {
                Hikari.close(connection, p, key);
            }
        });
    }

    private void pushPunishmentHistory(Punishment punishment) {
        Bukkit.getScheduler().runTaskAsynchronously(TBans.getInstance(), () -> {
            Connection connection = null;
            PreparedStatement p = null;
            String update = "INSERT INTO `punishments_history` (`uuid`, `name`, `type`, `admin`, `reason`, `date`, `duration`) VALUES(?, " + (punishment.getType().equals(PunishmentType.IP) ? "HEX(IF(IS_IPV6(?),INET6_ATON(?), INET_ATON(?)))" : "?") + ", ?, ?, ?, ?, ?)";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareStatement(update);
                p.setString(1, punishment.getUUID() == null ? null : punishment.getUUID().toString());
                p.setString(2, punishment.getName());
                if (punishment.getType().equals(PunishmentType.IP)) {
                    p.setString(3, punishment.getName());
                    p.setString(4, punishment.getName());
                    p.setString(5, punishment.getType().name());
                    p.setString(6, punishment.getAdmin());
                    p.setString(7, punishment.getReason());
                    p.setLong(8, punishment.getDate());
                    if (punishment.getDuration() != null) {
                        p.setLong(9, punishment.getDuration());
                    } else {
                        p.setNull(9, Types.BIGINT);
                    }
                } else {
                    p.setString(3, punishment.getType().name());
                    p.setString(4, punishment.getAdmin());
                    p.setString(5, punishment.getReason());
                    p.setLong(6, punishment.getDate());
                    if (punishment.getDuration() != null) {
                        p.setLong(7, punishment.getDuration());
                    } else {
                        p.setNull(7, Types.BIGINT);
                    }
                }
                p.execute();
                LogUtil.info("pushPunishmentHistory(): Created new " + punishment.getType().toString() + " " + punishment.getName() + ".");
            } catch (SQLException e) {
                LogUtil.error("pushPunishmentHistory(): " + e + ".");
            } finally {
                Hikari.close(connection, p, null);
            }
        });
    }

    private void pushDeletePunishment(Punishment punishment) {
        Bukkit.getScheduler().runTaskAsynchronously(TBans.getInstance(), () -> {
            Connection connection = null;
            PreparedStatement p = null;
            String update = "DELETE FROM `punishments` WHERE `id`=?;";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareStatement(update);
                p.setInt(1, punishment.getId());
                p.execute();
                LogUtil.info("deletePunishment(): Removed " + punishment.getType().toString() + " " + punishment.getName() + ".");
            } catch (SQLException e) {
                LogUtil.error("deletePunishment(): " + e + ".");
            } finally {
                Hikari.close(connection, p, null);
            }
        });
    }

    public String formatMessage(Punishment punishment, Locale l, String action) {
        String message = l.getString("ban.messages." + action + ".header");
        message += "\n" + l.getString("ban.messages.date").replace("<date>", localeManager.getFullDate(punishment.getDate()));
        message += "\n" + l.getString("ban.messages.admin").replace("<admin>", punishment.getAdmin());
        if (punishment.getReason() != null) {
            message += "\n" + l.getString("ban.messages.reason").replace("<reason>", punishment.getReason());
        }
        if (punishment.getDuration() != null) {
            message += "\n" + l.getString("ban.messages.duration").replace("<duration>", localeManager.parseTimeWithTranslate(punishment.getRemaining(), l));
        }
        message += "\n" + l.getString("ban.messages." + action + ".footer");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public Long getMinimumDuration(PunishmentType type) {
        String duration = config.getString("minimumDuration." + type.toString());
        if (duration == null || duration.equals("0")) {
            return localeManager.parseTimeFromString("5s");
        }
        return localeManager.parseTimeFromString(duration);
    }

    public Integer getMaximumNickLength() {
        return config.getInt("maximumNameLength");
    }
}
