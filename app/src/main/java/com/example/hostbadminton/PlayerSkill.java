package com.example.hostbadminton;

public enum PlayerSkill {
    NEWBIE(0.0, "Newbie"),
    NEWBIE_RULES(0.25, "Newbie biết luật"),
    WEAK(0.5, "Yếu"),
    WEAK_PLUS(1.0, "Yếu +"),
    MID_WEAK(1.5, "Trung bình yếu"),
    MID_MINUS(2.0, "Trung bình -"),
    MID(2.5, "Trung bình"),
    MID_PLUS(3.0, "Trung bình +");

    private final double score;
    private final String displayName;

    PlayerSkill(double score, String displayName) {
        this.score = score;
        this.displayName = displayName;
    }

    public double getScore() {
        return score;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}