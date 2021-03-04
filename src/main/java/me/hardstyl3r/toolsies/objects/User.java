package me.hardstyl3r.toolsies.objects;

import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class User {

    private final UUID uuid;
    private String name;
    private Locale locale;
    private List<Group> groups;

    public User(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public User(Player p) {
        this.name = p.getName();
        this.uuid = p.getUniqueId();
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String s) {
        this.name = s;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale l) {
        this.locale = l;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> l) {
        l.sort(Comparator.comparing(Group::getPriority));
        this.groups = l;
    }

    public Group getMainGroup() {
        return getGroups().get(getGroups().size() - 1);
    }
}