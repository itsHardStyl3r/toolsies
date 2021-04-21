package me.hardstyl3r.tperms.managers;

import me.hardstyl3r.toolsies.Hikari;
import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.tperms.objects.Group;
import me.hardstyl3r.tperms.objects.PermissibleUser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PermissibleUserManager {

    private final PermissionsManager permissionsManager;

    public PermissibleUserManager(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
        loadPermissibleUsers();
    }

    private final HashMap<UUID, PermissibleUser> users = new HashMap<>();

    public PermissibleUser getUser(String name) {
        return users.values().stream().filter(u -> u.getName().equals(name)).findFirst().orElse(null);
    }

    public PermissibleUser getUser(UUID uuid) {
        return users.get(uuid);
    }

    public PermissibleUser getUser(Player player) {
        return getUser(player.getUniqueId());
    }

    public PermissibleUser getUser(CommandSender sender) {
        return getUser(sender.getName());
    }

    public boolean hasPlayedBefore(Player player) {
        return getUser(player) != null;
    }

    public void loadPermissibleUsers() {
        Bukkit.getScheduler().runTaskAsynchronously(Toolsies.getInstance(), () -> {
            users.clear();
            Connection connection = null;
            PreparedStatement p = null;
            ResultSet rs = null;

            String call = "SELECT `uuid`, `name`, `groups`, `permissions` FROM `users`;";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareCall(call);
                p.execute();
                rs = p.getResultSet();
                while (rs.next()) {
                    PermissibleUser user = new PermissibleUser(rs.getString("name"), UUID.fromString(rs.getString("uuid")));
                    ArrayList<Group> groups = new ArrayList<>();
                    if(rs.getString("groups") != null) {
                        for (String s : rs.getString("groups").split(",")) {
                            if (permissionsManager.getGroup(s) != null) {
                                groups.add(permissionsManager.getGroup(s));
                            }
                        }
                    }
                    if (groups.isEmpty()) {
                        System.out.println("loadPermissibleUsers(): User " + user.getName() + " had no groups or they were all incorrect.");
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
                System.out.println("loadPermissibleUsers(): Loaded " + users.size() + " users.");
                Hikari.close(connection, p, rs);
            }
        });
    }

    public void createPermissibleUser(Player player) {
        PermissibleUser user = new PermissibleUser(player);
        user.setGroups(permissionsManager.getDefaultGroups());
        user.setPermissions(Collections.emptyList());
        users.put(player.getUniqueId(), user);
        updatePermissibleUser(user);
    }

    public void updatePermissibleUser(PermissibleUser user) {
        Bukkit.getScheduler().runTaskAsynchronously(Toolsies.getInstance(), () -> {
            Connection connection = null;
            PreparedStatement p = null;

            String update = "UPDATE `users` SET `groups`=?, `permissions`=? WHERE `uuid`=?";
            try {
                connection = Hikari.getHikari().getConnection();
                p = connection.prepareStatement(update);
                p.setString(1, permissionsManager.listGroups(user.getGroups()));
                p.setString(2, serialize(user.getPermissions()));
                p.setString(3, user.getUUID().toString());
                p.execute();
                System.out.println("updatePermissibleUser(): Updated " + user.getName() + ".");
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
