package me.santio.dodgeball.api.models;

import lombok.Getter;

@Getter
public enum EliminationReason {
    DIED("{victim} died to {attacker}!"),
    DISCONNECT("{victim} disconnected and was eliminated."),
    LEFT("{victim} left and was eliminated."),
    UNKNOWN(""),
    ;
    
    private final String message;
    EliminationReason(String message) {
        this.message = message;
    }
}
