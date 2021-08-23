package me.hardstyl3r.tbans.enums;

public enum PunishmentType {
    BAN,
    WARN,
    IP,
    MUTE,
    ANY;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    public boolean equals(PunishmentType other) {
        return this == other || other == ANY;
    }
}
