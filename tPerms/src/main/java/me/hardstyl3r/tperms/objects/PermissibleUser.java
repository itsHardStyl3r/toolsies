package me.hardstyl3r.tperms.objects;

import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class PermissibleUser {

    private final UUID uuid;
    private final String name;
    private List<Group> groups;
    private List<String> permissions;

    public PermissibleUser(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public PermissibleUser(Player p) {
        this.uuid = p.getUniqueId();
        this.name = p.getName();
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> l) {
        l.sort(Comparator.comparing(Group::getPriority));
        this.groups = l;
    }

    public List<String> listGroups(){
        return groups.stream().map(Group::getName).toList();
    }

    public Group getMainGroup() {
        return getGroups().get(getGroups().size() - 1);
    }

    public List<String> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public Boolean hasPermissions() {
        if (permissions == null) return false;
        return permissions.size() != 0;
    }
}
