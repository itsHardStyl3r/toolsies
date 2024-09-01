package me.hardstyl3r.tbans.enums;

/**
 * Types of available punishments.
 */
public enum PunishmentType {
    BAN,
    WARN,
    IP,
    MUTE,
    KICK;

    /**
     * Quick method to return lowercased name of PunishmentType.
     *
     * @return lowercased name of PunishmentType.
     */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
