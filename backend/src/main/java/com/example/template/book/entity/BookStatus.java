package com.example.template.book.entity;

public enum BookStatus {
    TO_READ,
    IN_PROGRESS,
    READ;

    /** Sort weight for the main books list: in-progress first, then to-read, then read. */
    public int sortWeight() {
        return switch (this) {
            case IN_PROGRESS -> 0;
            case TO_READ -> 1;
            case READ -> 2;
        };
    }
}
