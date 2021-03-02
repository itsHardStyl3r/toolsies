package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

public class PermissionsManager {

    private final FileConfiguration config;

    public PermissionsManager(ConfigManager configManager, UserManager userManager) {
        config = configManager.loadConfig("permissions");
    }

    public FileConfiguration getConfig() {
        return config;
    }

    private final HashMap<UUID, PermissionAttachment> permissions = new HashMap<>();

    public boolean isGroup(String group) {
        return getGroups().contains(group);
    }

    public Set<String> getGroups() {
        if (config.getConfigurationSection("groups") == null) {
            return Collections.emptySet();
        } else {
            return config.getConfigurationSection("groups").getKeys(false);
        }
    }

    public void startPermissions(Player p, User u) {
        if (permissions.containsKey(p.getUniqueId())) {
            System.out.println("Found " + u.getName() + " (" + u.getUUID() + ") in `permissions`. Refreshing permissions.");
            p.removeAttachment(permissions.get(p.getUniqueId()));
            stopPermissions(p);
        }
        PermissionAttachment attachment = p.addAttachment(Toolsies.getInstance());
        permissions.put(p.getUniqueId(), attachment);
        System.out.println("Put " + u.getName() + " (" + u.getUUID() + ") to `permissions`. Result: Is there? " + permissions.containsKey(p.getUniqueId()) + ". (expected: true)");
        System.out.println("Invoking setupPermissions for " + u.getName() + " (" + u.getUUID() + ").");
        setupPermissions(u);
    }

    public void stopPermissions(Player p) {
        permissions.remove(p.getUniqueId());
        System.out.println("Removed " + p.getName() + " (" + p.getUniqueId() + ") from playerPermissions. Result: Is there? " + permissions.containsKey(p.getUniqueId()) + ". (expected: false)");
    }

    public void setupPermissions(User u) {
        PermissionAttachment attachment = permissions.get(u.getUUID());
        List<String> groups = u.getGroups();
        System.out.println(u.getName() + " (" + u.getUUID() + ") is in groups: " + u.getGroups().toString() + ".");
        for (String group : groups) {
            if (config.getStringList("groups." + group + ".inherits") != null) {
                for (String inherit : config.getStringList("groups." + group + ".inherits")) {
                    System.out.println("Group " + group + " inherits from: " + inherit);
                    setPermissionsFromGroup(attachment, inherit);
                }
            }
            setPermissionsFromGroup(attachment, group);
        }
        System.out.println("DUMP OF PERMISSIONS - START");
        for (String s : attachment.getPermissions().keySet()) {
            System.out.println(s + " = " + attachment.getPermissions().get(s).toString());
        }
        System.out.println("DUMP OF PERMISSIONS - END");
    }

    private void setPermissionsFromGroup(PermissionAttachment attachment, String group) {
        System.out.println("Retrieving permissions from group " + group);
        if (config.getStringList("groups." + group + ".permissions") != null) {
            for (String permission : config.getStringList("groups." + group + ".permissions")) {
                if (attachment.getPermissions().containsKey(permission)) {
                    System.out.println("Redundant permission: " + permission + ". Skipping.");
                    continue;
                }
                System.out.println("setPermission(" + permission + ").");
                if (permission.startsWith("-")) {
                    System.out.println("unsetPermission(" + permission.replace("-", "") + ").");
                    attachment.unsetPermission(permission.replace("-", ""));
                } else {
                    attachment.setPermission(permission, true);
                }
            }
        }
    }
}
