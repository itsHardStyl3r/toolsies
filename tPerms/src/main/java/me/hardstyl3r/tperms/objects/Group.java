package me.hardstyl3r.tperms.objects;

import java.util.List;

public class Group {

    private final String name;
    private List<String> permissions;
    private boolean def;
    private int priority;
    private List<Group> inherits;

    public Group(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public Boolean isDefault() {
        return this.def;
    }

    public void setDefault(boolean def) {
        this.def = def;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<Group> getInherits() {
        return this.inherits;
    }

    public void setInherits(List<Group> inherits) {
        this.inherits = inherits;
    }
}
