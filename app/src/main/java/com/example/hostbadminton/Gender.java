package com.example.hostbadminton;

public enum Gender {
    MALE("Nam"),
    FEMALE("Nữ");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}