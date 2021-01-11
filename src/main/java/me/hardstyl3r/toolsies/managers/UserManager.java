package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {

    public UserManager() {
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
        String update = "INSERT INTO `users` VALUES(?, ?)";
        try {
            connection = Hikari.getHikari().getConnection();
            User user = new User(player);
            p = connection.prepareStatement(update);
            p.setString(1, player.getUniqueId().toString());
            p.setString(2, player.getName());
            p.execute();
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

        String update = "UPDATE `users` SET `name`=? WHERE `uuid`=?";
        try {
            connection = Hikari.getHikari().getConnection();
            p = connection.prepareStatement(update);
            p.setString(1, u.getName());
            p.setString(2, u.getUUID().toString());
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
