package me.santio.dodgeball.api.models;

import lombok.Getter;

@Getter
public enum Team {
    RED("§c"),
    BLUE("§b"),
    DEAD("§7"),
    /**
     * Represents no team, this is used internally and no players should be on this team.
     */
    NONE("§7"),
    ;
    
    private final String color;
    Team(String color) {
        this.color = color;
    }
    
    public static Team fromName(String name) {
        try {
            return Team.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Team.NONE;
        }
    }
}
