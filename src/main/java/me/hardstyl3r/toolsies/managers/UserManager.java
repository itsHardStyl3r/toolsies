package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.objects.Group;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class UserManager {

    private final LocaleManager localeManager;
    private final PermissionsManager permissionsManager;

    public UserManager(LocaleManager localeManager, PermissionsManager permissionsManager) {
        this.localeManager = localeManager;
        this.permissionsManager = permissionsManager;
        loadUsers();
    }

    private final HashMap<UUID, User> users = new HashMap<>();

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

    public boolean hasPlayedBefore(Player p) {
        return getUser(p.getUniqueId()) != null;
    }

    public HashMap<UUID, User> getUsers() {
        return users;
    }

    public void loadUsers() {
        users.clear();
        Connection connection = null;
        PreparedStatement p = null;

        String call = "SELECT * FROM `users`";
        try {
            connection = Hikari.getHikari().getConnection();
            p = connection.prepareCall(call);
            p.execute();
            ResultSet rs = p.getResultSet();
            while (rs.next()) {
                User user = new User(rs.getString("name"), UUID.fromString(rs.getString("uuid")));
                user.setLocale(localeManager.getLocale(rs.getString("locale")));
                ArrayList<Group> groups = new ArrayList<>();
                for (String s : rs.getString("groups").split(",")) {
                    if (permissionsManager.getGroup(s) != null) {
                        groups.add(permissionsManager.getGroup(s));
                    }
                }
                user.setGroups(groups);
                users.put(user.getUUID(), user);
                System.out.println("UserManager.loadUsers(): Found and set up user " + user.getName() + " (" + user.getUUID() + "). Result? " + users.containsKey(user.getUUID()) + " (expected: true)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

    public void createUser(Player player) {
        Connection connection = null;
        PreparedStatement p = null;
        String update = "INSERT INTO `users` VALUES(?, ?, ?, ?)";
        try {
            connection = Hikari.getHikari().getConnection();
            User user = new User(player);
            p = connection.prepareStatement(update);
            p.setString(1, player.getUniqueId().toString());
            p.setString(2, player.getName());
            p.setString(3, localeManager.getDefault().getId());
            p.setString(4, permissionsManager.listGroups(permissionsManager.getDefaultGroups()));
            p.execute();
            user.setLocale(localeManager.getDefault());
            user.setGroups(permissionsManager.getDefaultGroups());
            users.put(player.getUniqueId(), user);
        } catch (SQLException e) {
            e.printStackTrace();
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

    public void updateUser(User u) {
        Connection connection = null;
        PreparedStatement p = null;

        String update = "UPDATE `users` SET `name`=?, `locale`=?, `groups`=? WHERE `uuid`=?";
        try {
            connection = Hikari.getHikari().getConnection();
            p = connection.prepareStatement(update);
            p.setString(1, u.getName());
            p.setString(2, u.getLocale().getId());
            p.setString(3, permissionsManager.listGroups(u.getGroups()));
            p.setString(4, u.getUUID().toString());
            p.execute();
            System.out.println("UserManager.updateUser(): Updating user " + u.getName() + " (" + u.getUUID() + ").");
        } catch (SQLException e) {
            e.printStackTrace();
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
}
