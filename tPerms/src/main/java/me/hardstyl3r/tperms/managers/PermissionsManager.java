package me.hardstyl3r.tperms.managers;

import me.hardstyl3r.toolsies.managers.ConfigManager;
import me.hardstyl3r.tperms.TPerms;
import me.hardstyl3r.tperms.objects.Group;
import me.hardstyl3r.tperms.objects.PermissibleUser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PermissionsManager {

    private final FileConfiguration config;

    public PermissionsManager(TPerms plugin, ConfigManager configManager) {
        config = configManager.loadConfig(plugin, "permissions");
        loadGroups();
    }

    private final HashMap<UUID, PermissionAttachment> permissions = new HashMap<>();
    private final HashMap<String, Group> groups = new HashMap<>();

    private void loadGroups() {
        if (config.getConfigurationSection("groups").getKeys(false) == null) {
            System.out.println("Could not find any groups.");
            return;
        }
        for (String name : config.getConfigurationSection("groups").getKeys(false)) {
            Group g = new Group(name);
            g.setPermissions(config.getStringList("groups." + name + ".permissions"));
            g.setPriority(config.getInt("groups." + name + ".priority"));
            g.setDefault(config.getBoolean("groups." + name + ".default"));
            groups.put(name, g);
        }
        for (Group group : groups.values()) {
            String name = group.getName();
            ArrayList<Group> inherits = new ArrayList<>();
            if (config.getStringList("groups." + name + ".inherits").isEmpty()) continue;
            for (String inherit : config.getStringList("groups." + name + ".inherits")) {
                inherits.add(getGroup(inherit));
            }
            group.setInherits(inherits);
        }
        System.out.println("loadGroups(): Found " + groups.size() + " groups.");
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

    public ArrayList<Group> getDefaultGroups() {
        ArrayList<Group> newgroups = new ArrayList<>();
        for (Group g : groups.values()) {
            if (g.isDefault()) newgroups.add(g);
        }
        return newgroups;
    }

    public void startPermissions(Player p, PermissibleUser u) {
        stopPermissions(p);
        PermissionAttachment attachment = p.addAttachment(TPerms.getInstance());
        ArrayList<String> permsToAdd = new ArrayList<>();
        ArrayList<Group> toConsider;
        System.out.println("startPermissions(): Setup player " + p.getName() + " with" + (u == null ? "out" : "") + " user.");
        if (u == null) {
            toConsider = getDefaultGroups();
        } else {
            toConsider = new ArrayList<>(u.getGroups());
        }
        for (Group group : toConsider) {
            if (group.getInherits() != null) {
                for (Group inherit : group.getInherits()) {
                    permsToAdd.addAll(inherit.getPermissions());
                }
            }
            permsToAdd.addAll(group.getPermissions());
        }
        if (u != null) {
            for (String s : u.getPermissions()) {
                if (!permsToAdd.contains(s)) {
                    permsToAdd.add(s);
                }
            }
        }
        for (String permission : permsToAdd) {
            if (permission.startsWith("-")) {
                attachment.unsetPermission(permission.replace("-", ""));
            } else {
                attachment.setPermission(permission, true);
            }
        }
        permissions.put(p.getUniqueId(), attachment);
    }

    public void stopPermissions(Player p) {
        if (permissions.get(p.getUniqueId()) != null) {
            System.out.println("stopPermissions(): Removed attachment for " + p.getName());
            p.removeAttachment(permissions.get(p.getUniqueId()));
            permissions.remove(p.getUniqueId());
        }
    }

    /*
    Not the nicest way, but quickest.
     */
    public String listGroups(List<Group> groups) {
        if (groups == null) return "";
        ArrayList<String> convert = new ArrayList<>();
        for (Group g : groups) convert.add(g.getName());
        return convert.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "");
    }
}
