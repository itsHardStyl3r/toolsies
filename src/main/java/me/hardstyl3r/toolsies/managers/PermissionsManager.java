package me.hardstyl3r.toolsies.managers;

import me.hardstyl3r.toolsies.Toolsies;
import me.hardstyl3r.toolsies.objects.Group;
import me.hardstyl3r.toolsies.objects.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PermissionsManager {

    private final FileConfiguration config;

    public PermissionsManager(ConfigManager configManager) {
        config = configManager.loadConfig("permissions");
        loadGroups();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    private final HashMap<UUID, PermissionAttachment> permissions = new HashMap<>();
    private final HashMap<String, Group> groups = new HashMap<>();

    private void loadGroups() {
        if (config.getConfigurationSection("groups").getKeys(false) == null) {
            System.out.println("Could not find any groups. This is bad.");
            return;
        }
        for (String name : config.getConfigurationSection("groups").getKeys(false)) {
            Group g = new Group(name);
            if (config.getStringList("groups." + name + ".permissions").isEmpty()) {
                System.out.println("Group " + name + ": Could not find any permissions.");
            }
            g.setPermissions(config.getStringList("groups." + name + ".permissions"));
            g.setPriority(config.getInt("groups." + name + ".priority"));
            g.setDefault(config.getBoolean("groups." + name + ".default"));
            groups.put(name, g);
            System.out.println("Found and put " + (g.isDefault() ? "default " : "") + "group " + g.getName() + " (priority: " + g.getPriority() + ") with " + g.getPermissions().size() + " permissions.");
        }
        System.out.println("Loaded groups, heading to inherits.");
        for (Group group : groups.values()) {
            String name = group.getName();
            ArrayList<Group> inherits = new ArrayList<>();
            if (config.getStringList("groups." + name + ".inherits").isEmpty()) {
                System.out.println("Group " + name + ": Could not find any inherits.");
                continue;
            }
            for (String inherit : config.getStringList("groups." + name + ".inherits")) {
                inherits.add(getGroup(inherit));
                System.out.println("Group " + name + " now inherits from " + inherit);
            }
            group.setInherits(inherits);
        }
    }

    public Group getGroup(String name) {
        return groups.get(name);
    }

    public List<String> getGroups() {
        ArrayList<String> current = new ArrayList<>();
        for (Group g : groups.values()) {
            current.add(g.getName());
        }
        return current;
    }

    public void startPermissions(Player p, User u) {
        if (permissions.containsKey(p.getUniqueId())) {
            System.out.println("Found " + u.getName() + " in permissions. Refreshing permissions.");
            p.removeAttachment(permissions.get(p.getUniqueId()));
            stopPermissions(p);
        }
        PermissionAttachment attachment = p.addAttachment(Toolsies.getInstance());
        System.out.println("Putting " + u.getName() + " to permissions.");
        permissions.put(p.getUniqueId(), attachment);
        System.out.println("Invoking setupPermissions for " + u.getName() + ".");
        setupPermissions(u);
    }

    public void stopPermissions(Player p) {
        System.out.println("Removing " + p.getName() + " from playerPermissions.");
        permissions.remove(p.getUniqueId());
    }

    public List<Group> getDefaultGroups() {
        ArrayList<Group> newgroups = new ArrayList<>();
        for (Group g : groups.values()) {
            if (g.isDefault()) newgroups.add(g);
        }
        return newgroups;
    }

    private void setupPermissions(User u) {
        PermissionAttachment attachment = permissions.get(u.getUUID());
        List<Group> groups = u.getGroups();
        if (groups.size() == 0) {
            groups = getDefaultGroups();
            System.out.println("Defaulted to default groups for " + u.getName() + "! User should have a group!");
        }
        System.out.println(u.getName() + " (" + u.getUUID() + ") is in groups: " + listGroups(u.getGroups()) + ".");
        for (Group g : groups) {
            if (g.getInherits() != null) {
                for (Group inherit : g.getInherits()) {
                    if (!u.getGroups().contains(inherit)) {
                        System.out.println("Group " + g.getName() + " inherits from: " + inherit.getName());
                        setPermissionsFromGroup(attachment, inherit);
                    } else {
                        System.out.println("Group " + g.getName() + ": Skipped inheritance of " + inherit.getName() + ", because User is in that group.");
                    }
                }
            }
            setPermissionsFromGroup(attachment, g);
        }
        if (u.hasPermissions()) {
            System.out.println("Found " + u.getPermissions().size() + " additional permissions.");
            for (String permission : u.getPermissions()) {
                setPermission(attachment, permission);
            }
        } else {
            System.out.println("User does not have any additional permissions, skipping.");
        }
        System.out.println("DUMP OF PERMISSIONS - START");
        for (String s : attachment.getPermissions().keySet()) {
            System.out.println(s + " = " + attachment.getPermissions().get(s).toString());
        }
        System.out.println("DUMP OF PERMISSIONS - END");
    }

    private void setPermissionsFromGroup(PermissionAttachment attachment, Group group) {
        String name = group.getName();
        System.out.println("Retrieving permissions from group " + name);
        if (!group.getPermissions().isEmpty()) {
            for (String permission : group.getPermissions()) {
                if (attachment.getPermissions().containsKey(permission)) {
                    System.out.println("Redundant permission: " + permission + ". Skipping.");
                    continue;
                }
                setPermission(attachment, permission);
            }
        }
    }

    private void setPermission(PermissionAttachment attachment, String permission) {
        if (permission.startsWith("-")) {
            System.out.println("unsetPermission(" + permission.replace("-", "") + ").");
            attachment.unsetPermission(permission.replace("-", ""));
        } else {
            System.out.println("setPermission(" + permission + ").");
            attachment.setPermission(permission, true);
        }
    }

    /*
    Not the nicest way, but quickest.
     */
    public String listGroups(List<Group> groups) {
        if (groups == null) return "";
        ArrayList<String> convert = new ArrayList<>();
        for (Group g : groups) {
            convert.add(g.getName());
        }
        return convert.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "");
    }
}
