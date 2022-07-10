package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.objects.Locale;
import me.hardstyl3r.toolsies.objects.User;
import me.hardstyl3r.toolsies.utils.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

    private final LocaleManager localeManager;

    public UserManager(LocaleManager localeManager) {
        this.localeManager = localeManager;
        loadUsers();
    }

    private final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();

    public User getUser(String name) {
        return users.values().stream().filter(u -> u.getName().equals(name)).findFirst().orElse(null);
    }

    public User getUser(UUID uuid) {
        return users.get(uuid);
    }

    public User getUser(Player player) {
        return getUser(player.getUniqueId());
    }

    public User getUser(CommandSender sender) {
        return getUser(sender.getName());
    }

    public User getUserIgnoreCase(String name) {
        return users.values().stream().filter(u -> u.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public boolean hasPlayedBefore(Player p) {
        return getUser(p.getUniqueId()) != null;
    }

    public void loadUsers() {
        users.clear();
        Connection connection = null;
        PreparedStatement p = null;
        ResultSet rs = null;

        String call = "SELECT `uuid`, `name`, `locale` FROM `users`;";
        try {
            connection = Hikari.getHikari().getConnection();
            p = connection.prepareCall(call);
            p.execute();
            rs = p.getResultSet();
            while (rs.next()) {
                User user = new User(rs.getString("name"), UUID.fromString(rs.getString("uuid")));
                if (localeManager.getLocale(rs.getString("locale")) == null) {
                    LogUtil.warn("[toolsies] loadUsers(): User " + user.getName() + " had unknown locale.");
                    user.setLocale(localeManager.getDefault());
                } else {
                    user.setLocale(localeManager.getLocale(rs.getString("locale")));
                }
                users.put(user.getUUID(), user);
            }
        } catch (SQLException e) {
            LogUtil.error("[toolsies] loadUsers(): " + e + ".");
        } finally {
            LogUtil.info("[toolsies] loadUsers(): Loaded " + users.size() + " users.");
            Hikari.close(connection, p, rs);
        }
    }

    public void createUser(Player player) {
        User user = new User(player);
        user.setLocale(localeManager.getDefault());
        users.put(player.getUniqueId(), user);
        Bukkit.getScheduler().runTaskAsynchronously(Toolsies.getInstance(), () -> {
            Connection connection = null;
            PreparedStatement p = null;
            String update = "INSERT INTO `users` (`uuid`, `name`, `locale`) VALUES(?, ?, ?)";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareStatement(update);
                p.setString(1, player.getUniqueId().toString());
                p.setString(2, player.getName());
                p.setString(3, localeManager.getDefault().getId());
                p.execute();
                LogUtil.info("[toolsies] createUser(): Created new " + user.getName() + ".");
            } catch (SQLException e) {
                LogUtil.error("[toolsies] createUser(): " + e + ".");
            } finally {
                Hikari.close(connection, p, null);
            }
        });
    }

    public void updateUser(User u) {
        Bukkit.getScheduler().runTaskAsynchronously(Toolsies.getInstance(), () -> {
            Connection connection = null;
            PreparedStatement p = null;

            String update = "UPDATE `users` SET `name`=?, `locale`=? WHERE `uuid`=?";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareStatement(update);
                p.setString(1, u.getName());
                p.setString(2, u.getLocale().getId());
                p.setString(3, u.getUUID().toString());
                p.execute();
                LogUtil.info("[toolsies] updateUser(): Updated " + u.getName() + ".");
            } catch (SQLException e) {
                LogUtil.error("[toolsies] updateUsers(): " + e + ".");
            } finally {
                Hikari.close(connection, p, null);
            }
        });
    }

    public Locale determineLocale(String sender) {
        Locale l = localeManager.getDefault();
        if (getUser(sender) != null) {
            l = getUser(sender).getLocale();
        }
        return l;
    }

    public Locale determineLocale(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return localeManager.getDefault();
        }
        return determineLocale(sender.getName());
    }

    public Locale determineLocale(UUID uuid) {
        Locale l = localeManager.getDefault();
        if (getUser(uuid) != null) {
            l = getUser(uuid).getLocale();
        }
        return l;
    }
}
