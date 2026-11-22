package com.example.hostbadminton;

public enum PlayStyle {
    BALANCED("Bình thường"),
    CHILL("Chill (Cùng/dưới trình)"),
    INTENSE("Căng (Cùng/hơn trình)");

    private final String displayName;

    PlayStyle(String displayName) {
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