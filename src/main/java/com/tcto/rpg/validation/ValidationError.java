package com.tcto.rpg.validation;

public record ValidationError(Severity severity, String file, String message) {
    public enum Severity {
        WARN,
        ERROR
    }
}
