package com.example.template.todo.entity;

public enum ImportanceLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    /** Natural sort weight (higher = more important). Used for in-memory sorting. */
    public int weight() {
        return switch (this) {
            case LOW      -> 1;
            case MEDIUM   -> 2;
            case HIGH     -> 3;
            case CRITICAL -> 4;
        };
    }
}
