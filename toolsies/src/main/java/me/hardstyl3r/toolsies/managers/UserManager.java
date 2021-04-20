package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.objects.Group;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

    private final LocaleManager localeManager;
    private final PermissionsManager permissionsManager;

    public UserManager(LocaleManager localeManager, PermissionsManager permissionsManager) {
        this.localeManager = localeManager;
        this.permissionsManager = permissionsManager;
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

    public boolean hasPlayedBefore(Player p) {
        return getUser(p.getUniqueId()) != null;
    }

    public void loadUsers() {
        Bukkit.getScheduler().runTaskAsynchronously(Toolsies.getInstance(), () -> {
            users.clear();
            Connection connection = null;
            PreparedStatement p = null;
            ResultSet rs = null;

            String call = "SELECT `uuid`, `name`, `locale`, `groups`, `permissions` FROM `users`;";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareCall(call);
                p.execute();
                rs = p.getResultSet();
                while (rs.next()) {
                    User user = new User(rs.getString("name"), UUID.fromString(rs.getString("uuid")));
                    if (localeManager.getLocale(rs.getString("locale")) == null) {
                        System.out.println("loadUsers(): User " + user.getName() + " had unknown locale.");
                        user.setLocale(localeManager.getDefault());
                    } else {
                        user.setLocale(localeManager.getLocale(rs.getString("locale")));
                    }
                    ArrayList<Group> groups = new ArrayList<>();
                    for (String s : rs.getString("groups").split(",")) {
                        if (permissionsManager.getGroup(s) != null) {
                            groups.add(permissionsManager.getGroup(s));
                        }
                    }
                    if (groups.isEmpty()) {
                        System.out.println("loadUsers(): User " + user.getName() + " had no groups or they were incorrect.");
                        user.setGroups(permissionsManager.getDefaultGroups());
                    } else {
                        user.setGroups(groups);
                    }
                    if (rs.getString("permissions") == null || rs.getString("permissions").equals("")) {
                        user.setPermissions(Collections.emptyList());
                    } else {
                        user.setPermissions(Arrays.asList(rs.getString("permissions").split(",")));
                    }
                    users.put(user.getUUID(), user);
                    /*
                    This should be fine.
                     */
                    Player player = Bukkit.getPlayer(user.getUUID());
                    if (player != null) {
                        permissionsManager.startPermissions(player, user);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                System.out.println("loadUsers(): Loaded " + users.size() + " users.");
                Hikari.close(connection, p, rs);
            }
        });
    }

    public void createUser(Player player) {
        User user = new User(player);
        user.setLocale(localeManager.getDefault());
        user.setGroups(permissionsManager.getDefaultGroups());
        user.setPermissions(Collections.emptyList());
        users.put(player.getUniqueId(), user);
        Bukkit.getScheduler().runTaskAsynchronously(Toolsies.getInstance(), () -> {
            Connection connection = null;
            PreparedStatement p = null;
            String update = "INSERT INTO `users` VALUES(?, ?, ?, ?, ?)";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareStatement(update);
                p.setString(1, player.getUniqueId().toString());
                p.setString(2, player.getName());
                p.setString(3, localeManager.getDefault().getId());
                p.setString(4, permissionsManager.listGroups(permissionsManager.getDefaultGroups()));
                p.setString(5, null);
                p.execute();
                System.out.println("createUser(): Created new " + user.getName());
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                Hikari.close(connection, p, null);
            }
        });
    }

    public void updateUser(User u) {
        Bukkit.getScheduler().runTaskAsynchronously(Toolsies.getInstance(), () -> {
            Connection connection = null;
            PreparedStatement p = null;

            String update = "UPDATE `users` SET `name`=?, `locale`=?, `groups`=?, `permissions`=? WHERE `uuid`=?";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareStatement(update);
                p.setString(1, u.getName());
                p.setString(2, u.getLocale().getId());
                p.setString(3, permissionsManager.listGroups(u.getGroups()));
                p.setString(4, serialize(u.getPermissions()));
                p.setString(5, u.getUUID().toString());
                p.execute();
                System.out.println("loadUsers(): Updated " + u.getName() + ".");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                Hikari.close(connection, p, null);
            }
        });
    }

    public String serialize(List<String> strings) {
        if (strings == null) return null;
        if (strings.isEmpty() || strings.size() == 0) return null;
        for (String s : strings) {
            if (s.contains(",")) s.replace(",", ".");
        }
        return strings.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "");
    }
}
