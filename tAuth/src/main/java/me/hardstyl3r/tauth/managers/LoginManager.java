package me.hardstyl3r.tauth.managers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import me.hardstyl3r.tauth.TAuth;
import me.hardstyl3r.tauth.enums.AuthSource;
import me.hardstyl3r.tauth.enums.AuthType;
import me.hardstyl3r.tauth.objects.AuthUser;
import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.managers.UserManager;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.objects.User;
import me.hardstyl3r.toolsies.utils.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class LoginManager {

    private final FileConfiguration config;
    private final HashMap<UUID, AuthUser> auths = new HashMap<>();
    private final HashMap<UUID, Integer> kickTasks = new HashMap<>();
    private final TAuth plugin;
    private final UserManager userManager;

    public LoginManager(TAuth plugin, FileConfiguration config, UserManager userManager) {
        this.plugin = plugin;
        this.config = config;
        this.userManager = userManager;
        loadAuths();
    }

    public boolean register(Player player, String password) {
        AuthUser user = getAuth(player);
        long current = System.currentTimeMillis();
        byte[] bcryptPassword = BCrypt.withDefaults().hash(12, password.getBytes(StandardCharsets.UTF_8));
        String ip = player.getAddress().getAddress().getHostAddress();
        user.setName(player.getName());
        user.setPassword(bcryptPassword);
        user.setIp(ip);
        user.setRegisterIp(ip);
        user.setRegisterDate(current);
        user.setLastLoginDate(current);
        user.setLastLocation(player.getLocation());
        user.setRegistered(true);
        user.setLoggedIn(true);
        user.setPlaytime(0L);
        auths.put(user.getUUID(), user);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = null;
            PreparedStatement p = null;
            String update = "INSERT INTO `logins` " +
                    "(`uuid`, `name`, `password`, `ip`, `regip`, `regdate`, `lastlogin`, `loggedin`, `hassession`, `x`, `y`, `z`, `yaw`, `pitch`, `world`, `playtime`) " +
                    "VALUES(?, ?, ?, INET_ATON(?), INET_ATON(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareStatement(update);
                p.setString(1, user.getUUID().toString());
                p.setString(2, user.getName());
                p.setBytes(3, user.getPassword());
                p.setString(4, user.getIp());
                p.setString(5, user.getRegisterIp());
                p.setLong(6, user.getRegisterDate());
                p.setLong(7, user.getLastLoginDate());
                p.setBoolean(8, true);
                p.setBoolean(9, true);
                p.setDouble(10, user.getLastLocation().getX());
                p.setDouble(11, user.getLastLocation().getY());
                p.setDouble(12, user.getLastLocation().getZ());
                p.setFloat(13, user.getLastLocation().getYaw());
                p.setFloat(14, user.getLastLocation().getPitch());
                p.setString(15, user.getLastLocation().getWorld().getName());
                p.setLong(16, user.getPlaytime());
                p.execute();
                LogUtil.info("[tAuth] register(): Registered " + user.getUUID() + " (" + user.getName() + ").");
            } catch (SQLException e) {
                LogUtil.error("[tAuth] register(): " + e + ".");
            } finally {
                Hikari.close(connection, p, null);
            }
        });
        return true;
    }

    public AuthUser getAuth(Player p) {
        if (p == null) return null;
        return getAuth(p.getUniqueId());
    }

    public AuthUser getAuth(User u) {
        return getAuth(u.getUUID());
    }

    public AuthUser getAuth(UUID uuid) {
        if (auths.get(uuid) == null) return new AuthUser(uuid);
        return auths.get(uuid);
    }

    public AuthUser getAuth(String name) {
        /*
        27.12.2021: Changed .equals to .equalsIgnoreCase to fix hardstyl3r and HardStyl3r being different accounts.
        With current setup, it's impossible for hardstyl3r to join, because HardStyl3r would be an already existing account.
        But if HardStyl3r haven't join the server earlier, hardstyl3r would've been allowed
        See: AuthPlayerListeners@onPreLoginHighest
         */
        return auths.values().stream().filter(au -> au.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    private void loadAuths() {
        auths.clear();
        Connection connection = null;
        PreparedStatement p = null;

        String call = "SELECT `uuid`, `name`, `password`, INET_NTOA(`ip`) AS `ip`, INET_NTOA(`regip`) AS `regip`, " +
                "`regdate`, `lastlogin`, `email`, `loggedin`, `hassession`, `x`, `y`, `z`, `yaw`, `pitch`, `world`, `playtime` FROM `logins`";
        try {
            connection = Hikari.getHikari().getConnection();
            p = connection.prepareCall(call);
            p.execute();
            ResultSet rs = p.getResultSet();
            while (rs.next()) {
                AuthUser authUser = new AuthUser(UUID.fromString(rs.getString("uuid")));
                authUser.setRegistered(true);
                authUser.setName(rs.getString("name"));
                authUser.setPassword(rs.getBytes("password"));
                authUser.setIp(rs.getString("ip"));
                authUser.setRegisterIp(rs.getString("regip"));
                authUser.setRegisterDate(rs.getLong("regdate"));
                authUser.setLastLoginDate(rs.getLong("lastlogin"));
                authUser.setEmail(rs.getString("email"));
                authUser.setLoggedIn(false);
                authUser.setHasSession(rs.getBoolean("hassession"));
                authUser.setLastLocation(new Location(
                        Bukkit.getWorld(rs.getString("world")),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getFloat("yaw"),
                        rs.getFloat("pitch")));
                authUser.setPlaytime(rs.getLong("playtime"));
                Player online = Bukkit.getPlayer(UUID.fromString(rs.getString("uuid")));
                if (online != null && rs.getBoolean("loggedin")) {
                    if (config.getBoolean("login.logoutOnReload", true)) {
                        Locale l = userManager.determineLocale(UUID.fromString(rs.getString("uuid")));
                        online.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                l.getString("login.reload_login")));
                        online.setWalkSpeed(0F); //0.2
                        online.setFlySpeed(0F); //0.1
                    } else authUser.setLoggedIn(true);
                }
                auths.put(authUser.getUUID(), authUser);
            }
            LogUtil.info("[tAuth] loadAuths(): Loaded " + auths.size() + " users.");
        } catch (SQLException e) {
            LogUtil.error("[tAuth] loadAuths(): " + e + ".");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (p != null) {
                try {
                    p.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void savePlayers() {
        Connection connection = null;
        PreparedStatement p = null;
        String update = "UPDATE `logins` SET `loggedin`=?, `hassession`=?, `x`=?, `y`=?, `z`=?, `yaw`=?, `pitch`=?, `world`=?, `playtime`=? WHERE `uuid`=?";
        try {
            connection = Hikari.getHikari().getConnection();
            for (Player player : Bukkit.getOnlinePlayers()) {
                AuthUser authUser = getAuth(player);
                p = connection.prepareStatement(update);
                p.setBoolean(1, authUser.isLoggedIn());
                p.setBoolean(2, authUser.hasSession());
                p.setDouble(3, player.getLocation().getX());
                p.setDouble(4, player.getLocation().getY());
                p.setDouble(5, player.getLocation().getZ());
                p.setFloat(6, player.getLocation().getYaw());
                p.setFloat(7, player.getLocation().getPitch());
                p.setString(8, player.getLocation().getWorld().getName());
                p.setLong(9, authUser.getPlaytime());
                p.setString(10, authUser.getUUID().toString());
                p.execute();
            }
            LogUtil.info("[tAuth] savePlayers(): Saved " + Bukkit.getOnlinePlayers().size() + " players.");
        } catch (SQLException e) {
            LogUtil.error("[tAuth] savePlayers(): " + e + ".");
        } finally {
            Hikari.close(connection, p, null);
        }
    }

    public void updateAuth(AuthUser user) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = null;
            PreparedStatement p = null;
            String update = "UPDATE `logins` SET `name`=?, `password`=?, `ip`=INET_ATON(?), `regip`=INET_ATON(?), `regdate`=?, `lastlogin`=?, `loggedin`=?, `hassession`=?, `x`=?, `y`=?, `z`=?, `yaw`=?, `pitch`=?, `world`=?, `email`=?, `playtime`=? WHERE `uuid`=?";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareStatement(update);
                p.setString(1, user.getName());
                p.setBytes(2, user.getPassword());
                p.setString(3, user.getIp());
                p.setString(4, user.getRegisterIp());
                p.setLong(5, user.getRegisterDate());
                p.setLong(6, user.getLastLoginDate());
                p.setBoolean(7, user.isLoggedIn());
                p.setBoolean(8, user.hasSession());
                p.setDouble(9, user.getLastLocation().getX());
                p.setDouble(10, user.getLastLocation().getY());
                p.setDouble(11, user.getLastLocation().getZ());
                p.setFloat(12, user.getLastLocation().getYaw());
                p.setFloat(13, user.getLastLocation().getPitch());
                p.setString(14, user.getLastLocation().getWorld().getName());
                p.setString(15, user.getEmail());
                p.setLong(16, user.getPlaytime());

                p.setString(17, user.getUUID().toString());
                p.execute();
                LogUtil.info("[tAuth] updateAuth(): Updated " + user.getUUID() + " (" + user.getName() + ").");
            } catch (SQLException e) {
                LogUtil.error("[tAuth] updateAuth(): " + e + ".");
            } finally {
                Hikari.close(connection, p, null);
            }
        });
    }

    public void pushAuthHistory(AuthUser authUser, AuthType type, boolean success, String comment) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = null;
            PreparedStatement p = null;
            String update = "INSERT INTO `logins_history` " +
                    "(`uuid`, `ip`, `date`, `success`, `action`, `comment`) " +
                    "VALUES(?, INET_ATON(?), ?, ?, ?, ?)";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareStatement(update);
                p.setString(1, authUser.getUUID().toString());
                p.setString(2, authUser.getIp());
                p.setLong(3, System.currentTimeMillis());
                p.setBoolean(4, success);
                p.setString(5, type.name().toLowerCase() + "_" + AuthSource.SERVER.getName());
                p.setString(6, comment);
                p.execute();
                LogUtil.info("[tAuth] pushAuthHistory(): New " + type.name() + "(" + success + ") for " + authUser.getUUID() + " (" + authUser.getName() + "). Comment: " + comment + ".");
            } catch (SQLException e) {
                LogUtil.error("[tAuth] pushAuthHistory(): " + e + ".");
            } finally {
                Hikari.close(connection, p, null);
            }
        });
    }

    public void refreshAuth(AuthUser authUser) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = null;
            PreparedStatement p = null;
            ResultSet rs = null;

            String call = "SELECT `password`, `email` FROM `logins` WHERE `uuid`=?";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareCall(call);
                p.setString(1, authUser.getUUID().toString());
                p.execute();
                rs = p.getResultSet();
                while (rs.next()) {
                    authUser.setPassword(rs.getBytes("password"));
                    authUser.setEmail(rs.getString("email"));
                }
                LogUtil.info("[tAuth] refreshAuth(): Refreshed " + authUser.getUUID() + " (" + authUser.getName() + ").");
            } catch (SQLException e) {
                LogUtil.error("[tAuth] refreshAuth(): " + e + ".");
            } finally {
                Hikari.close(connection, p, rs);
            }
        });
    }

    private byte[] hashPassword(String password) {
        return BCrypt.withDefaults().hash(12, password.getBytes(StandardCharsets.UTF_8));
    }

    public boolean changePassword(AuthUser user, String password) {
        if (passwordMatches(user, password))
            return true;
        user.setPassword(hashPassword(password));
        LogUtil.info("[tAuth] changePassword(): Password change for " + user.getUUID() + " (" + user.getName() + ").");
        updateAuth(user);
        return true;
    }

    public boolean passwordMatches(AuthUser user, String password) {
        return (BCrypt.verifyer().verify(password.getBytes(StandardCharsets.UTF_8), user.getPassword()).verified);
    }

    public boolean validatePassword(CommandSender sender, String password, Locale l) {
        int minLength = config.getInt("login.minPasswordLength");
        int maxLength = config.getInt("login.maxPasswordLength");
        if (config.getStringList("login.illegalPasswords").contains(password.toLowerCase())) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getString("register.illegal_password")));
            return true;
        }
        if (password.length() <= minLength) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getString("register.password_too_short")).replace("<length>", String.valueOf(minLength)));
            return true;
        }
        if (password.length() >= maxLength) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getString("register.password_too_long")).replace("<length>", String.valueOf(maxLength)));
            return true;
        }
        return false;
    }

    public boolean validatePassword(CommandSender sender, String password, String passwordConfirm, Locale l) {
        if (validatePassword(sender, password, l)) {
            if (!password.equals(passwordConfirm)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', l.getString("register.passwords_do_not_match")));
                return true;
            }
        }
        return false;
    }

    public List<Player> getOnlineUnauthed() {
        ArrayList<Player> online = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            AuthUser user = getAuth(p);
            if (user != null && !user.isLoggedIn()) {
                online.add(p);
            }
        }
        return online;
    }

    public void setKickTask(Player p, AuthType type) {
        Locale l = userManager.determineLocale(p);
        int task = getServer().getScheduler().scheduleSyncDelayedTask(plugin, () ->
                p.kickPlayer(ChatColor.translateAlternateColorCodes('&',
                        l.getString(type.name().toLowerCase() + ".timeout"))), config.getInt("login." + type.name().toLowerCase() + "Timeout") * 20L);
        kickTasks.put(p.getUniqueId(), task);
    }

    public void stopKickTask(Player p) {
        if (kickTasks.containsKey(p.getUniqueId()))
            getServer().getScheduler().cancelTask(kickTasks.get(p.getUniqueId()));
    }

    public boolean isUUID(String toCheck) {
        return toCheck.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    public ArrayList<AuthUser> getMultiAccounts(AuthUser authUser) {
        ArrayList<AuthUser> multiacc = new ArrayList<>();
        for (AuthUser user : auths.values()) {
            if ((user.getRegisterIp().equals(authUser.getRegisterIp()) || user.getIp().equals(authUser.getIp())) && user.getUUID() != authUser.getUUID()) {
                multiacc.add(user);
            }
        }
        return multiacc;
    }

    public List<String> getAllowedCommands(AuthType type) {
        return config.getStringList("login.commands." + type.name().toLowerCase() + "AllowedCommands");
    }
}
