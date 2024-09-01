package me.hardstyl3r.tbans.managers;

import me.hardstyl3r.tbans.TBans;
import me.hardstyl3r.tbans.enums.PunishmentType;
import me.hardstyl3r.tbans.objects.Punishment;
import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.managers.LocaleManager;
import me.hardstyl3r.toolsies.utils.LogUtil;
import me.hardstyl3r.tperms.managers.PermissibleUserManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PunishmentManager {

    private final LocaleManager localeManager;
    private final FileConfiguration config;
    private final PermissibleUserManager permissibleUserManager;
    private final Set<Punishment> punishments = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public PunishmentManager(LocaleManager localeManager, FileConfiguration config, PermissibleUserManager permissibleUserManager) {
        this.localeManager = localeManager;
        this.config = config;
        loadPunishments();
        this.permissibleUserManager = permissibleUserManager;
    }

    /**
     * A method to load all the Punishments from the database.
     */
    public void loadPunishments() {
        punishments.clear();
        Connection connection = null;
        PreparedStatement p = null;
        ResultSet rs = null;

        String call = "SELECT `id`, `uuid`, `name`, `type`, `admin`, `reason`, `date`, `duration` FROM `punishments` WHERE (`duration` IS NULL OR `duration`>=UNIX_TIMESTAMP()*1000) AND (`active` = 1);";
        try {
            connection = Hikari.getHikari().getConnection();
            p = connection.prepareCall(call);
            p.execute();
            rs = p.getResultSet();
            while (rs.next()) {
                Punishment punishment;
                try {
                    PunishmentType.valueOf(rs.getString("type"));
                } catch (Exception e) {
                    LogUtil.warn("[tBans] loadPunishments(): Unknown type " + rs.getString("type") + " for " + rs.getString("name") + ". Skipping.");
                    continue;
                }
                punishment = new Punishment(rs.getString("name"), PunishmentType.valueOf(rs.getString("type")));
                punishment.setId(rs.getInt("id"));
                if (rs.getString("uuid") != null) punishment.setUUID(UUID.fromString(rs.getString("uuid")));
                punishment.setSender(rs.getString("admin"));
                punishment.setDate(rs.getLong("date"));
                punishment.setDuration((rs.getLong("duration") == 0L ? null : rs.getLong("duration")));
                punishment.setReason(rs.getString("reason"));
                punishments.add(punishment);
            }
        } catch (SQLException e) {
            LogUtil.error("[tBans] loadPunishments(): " + e + ".");
        } finally {
            LogUtil.info("[tBans] loadPunishments(): Loaded " + punishments.size() + " punishments.");
            Hikari.close(connection, p, rs);
        }
    }

    /**
     * Creates, pushes to the database and saves a Punishment of desired type.
     *
     * @param type     Type of punishment
     * @param uuid     UUID of victim
     * @param name     Name of victim
     * @param sender   Name of sender of the command
     * @param reason   Reason for punishment
     * @param duration Duration of punishment
     * @return A complete Punishment object.
     */
    public Punishment createPunishment(PunishmentType type, UUID uuid, String name, String sender, String reason, Long duration) {
        Punishment punishment = new Punishment(name, type);
        if (uuid != null) punishment.setUUID(uuid);
        punishment.setSender(sender);
        punishment.setReason(reason);
        if (duration == null || duration <= 0) punishment.setDuration(null);
        else punishment.setDuration(System.currentTimeMillis() + duration);

        punishments.add(punishment);
        pushPunishment(punishment);
        return punishment;
    }

    /**
     * A method to return the entire list of all Punishments. Methods such as {@link #getPunishmentsByTypes(PunishmentType...)}
     * or {@link #getPunishmentsByName(String, PunishmentType...)} are preferred to this.
     *
     * @return A set of all punishments in memory.
     */
    public Set<Punishment> getPunishments() {
        return punishments;
    }

    /**
     * Deletes from memory and __deactivates__ a punishment in the database.
     *
     * @param punishment A certain Punishment to be deleted and deactivated.
     */
    public void deletePunishment(Punishment punishment) {
        if (!punishments.contains(punishment)) return;
        punishments.remove(punishment);
        pushDeletePunishment(punishment);
    }

    /**
     * A method to return all Punishments of certain type.
     *
     * @param types A type of Punishment to be returned
     * @return An ArrayList of desired Punishments or empty if none found
     */
    public ArrayList<Punishment> getPunishmentsByTypes(PunishmentType... types) {
        ArrayList<Punishment> current = new ArrayList<>();
        for (Punishment punishment : getPunishments())
            for (PunishmentType type : types)
                if (punishment.getType() == type && !punishment.isExpired()) current.add(punishment);
        return current;
    }

    /**
     * A method to return all Punishments of certain player/IP address and type.
     *
     * @param types A type of Punishment to be returned
     * @return An ArrayList of desired Punishments or empty if none found
     */
    public ArrayList<Punishment> getPunishmentsByName(String name, PunishmentType... types) {
        ArrayList<Punishment> current = new ArrayList<>();
        for (Punishment punishment : getPunishments())
            for (PunishmentType type : types)
                if (punishment.getType() == type && punishment.getName().equalsIgnoreCase(name) && !punishment.isExpired())
                    current.add(punishment);
        return current;
    }

    /**
     * A method to return all Punishments of certain player and type.
     *
     * @param types A type of Punishment to be returned
     * @return An ArrayList of desired Punishments or empty if none found
     */
    public ArrayList<Punishment> getPunishmentsByUuid(UUID uuid, PunishmentType... types) {
        ArrayList<Punishment> current = new ArrayList<>();
        for (Punishment punishment : getPunishments())
            for (PunishmentType type : types)
                if (punishment.getType().equals(type) && (punishment.getUUID() != null && punishment.getUUID().equals(uuid)) && !punishment.isExpired())
                    current.add(punishment);
        return current;
    }

    /**
     * A method to return a Punishment strictly by its ID.
     *
     * @param i ID of a Punishment
     * @return A Punishment, or null if not found.
     */
    public Punishment getPunishmentById(Integer i) {
        for (Punishment punishment : getPunishments())
            if (punishment.getId() == i && !punishment.isExpired()) return punishment;
        return null;
    }

    /**
     * A method to retrieve a singular Punishment of type.
     * This is not the preferred method for WARN. Instead, use {@link #getPunishmentsByName(String, PunishmentType...)};
     *
     * @param name Name of player
     * @param type A type of punishment
     * @return Punishment of preferred properties or null if not found
     */
    public Punishment getPunishment(String name, PunishmentType type) {
        ArrayList<Punishment> list = getPunishmentsByName(name, type);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * A method to retrieve a singular Punishment of type.
     * This is not the preferred method for WARN. Instead, use getPunishmentsByTypes(uuid, type);
     *
     * @param type A type of punishment
     * @param uuid UUID of player
     * @return Punishment of preferred properties or null if not found
     */
    public Punishment getPunishment(UUID uuid, PunishmentType type) {
        ArrayList<Punishment> list = getPunishmentsByUuid(uuid, type);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * A method to determine whether username has one punishment of type.
     *
     * @param name Name of user
     * @param type Type of punishment
     * @return True if punished, false otherwise
     */
    public boolean isPunished(String name, PunishmentType type) {
        return !getPunishmentsByName(name, type).isEmpty();
    }

    /**
     * A method to determine whether username has one punishment of type.
     *
     * @param uuid UUID of user
     * @param type Type of punishment
     * @return True if punished, false otherwise
     */
    public boolean isPunished(UUID uuid, PunishmentType type) {
        return !getPunishmentsByUuid(uuid, type).isEmpty();
    }

    private void pushPunishment(Punishment punishment) {
        Bukkit.getScheduler().runTaskAsynchronously(TBans.getInstance(), () -> {
            Connection connection = null;
            PreparedStatement p = null;
            String update = "INSERT INTO `punishments` (`uuid`, `name`, `type`, `admin`, `reason`, `date`, `duration`, `active`) VALUES(?, ?, ?, ?, ?, ?, ?, ?);";
            ResultSet key = null;
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareStatement(update, Statement.RETURN_GENERATED_KEYS);
                p.setString(1, punishment.getUUID() == null ? null : punishment.getUUID().toString());
                p.setString(2, punishment.getName());
                p.setString(3, punishment.getType().name());
                p.setString(4, punishment.getSender());
                p.setString(5, punishment.getReason());
                p.setLong(6, punishment.getDate());
                if (!punishment.isPermanent()) p.setLong(7, punishment.getDuration());
                else p.setNull(7, Types.NULL);
                p.setBoolean(8, true);
                p.execute();
                key = p.getGeneratedKeys();
                key.next();
                punishment.setId(key.getInt(1));
                LogUtil.info("[tBans] pushPunishment(): Created new " + punishment.getType().toString() + " for " + punishment.getName() + ".");
            } catch (SQLException e) {
                LogUtil.error("[tBans] pushPunishment(): " + e + ".");
            } finally {
                Hikari.close(connection, p, key);
            }
        });
    }

    private void pushDeletePunishment(Punishment punishment) {
        Bukkit.getScheduler().runTaskAsynchronously(TBans.getInstance(), () -> {
            Connection connection = null;
            PreparedStatement p = null;
            String update = "UPDATE `punishments` SET `active`=0  WHERE `id`=?;";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareStatement(update);
                p.setInt(1, punishment.getId());
                p.execute();
                LogUtil.info("[tBans] pushDeletePunishment(): Deactivated " + punishment.getType().toString() + " #" + punishment.getId() + " for " + punishment.getName() + ".");
            } catch (SQLException e) {
                LogUtil.error("[tBans] pushDeletePunishment(): " + e + ".");
            } finally {
                Hikari.close(connection, p, null);
            }
        });
    }

    /**
     * Retrieves a minimal duration of a certain PunishmentType from config file.
     *
     * @param type A type of Punishment
     * @return A minimal duration of Punishment or default 5s if not specified in config
     */
    public Long getMinimumDuration(PunishmentType type) {
        String duration = config.getString("minimumDuration." + type.toString());
        if (duration == null || duration.equals("0")) {
            return localeManager.parseTimeFromString("5s");
        }
        return localeManager.parseTimeFromString(duration);
    }

    /**
     * An overcomplicated method to determine priorities while giving a punishment. Basically, a sender with higher
     * priority can punish targets with lower ones, but a lower priority sender can't punish a higher one.
     *
     * @param sender Sender that executes command
     * @param target Name of punished party
     * @param type   A type of punishment
     * @return true if priority conditions met, false otherwise
     */
    public boolean canSenderPunishTarget(CommandSender sender, String target, PunishmentType type) {
        if (sender.getName().equalsIgnoreCase(target)) return true;
        if (!(sender instanceof Player) || sender.isOp() || sender.hasPermission("toolsies." + type + ".bypasspriority"))
            return true;
        if (permissibleUserManager == null) return true;
        if (!config.getBoolean("higherPriorityPunishment." + type)) return true;
        if (permissibleUserManager.getUser(target) == null) return true;
        return permissibleUserManager.getUser(sender).getMainGroup().getPriority() < permissibleUserManager.getUser(target).getMainGroup().getPriority();
    }
}
