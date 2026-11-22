package com.example.hostbadminton;

public class Player {
    private String id;
    private String name;
    private PlayerSkill skill;
    private Gender gender;
    private int matchesPlayed;
    private boolean isResting;
    private String preferredTeammateId;
    private String avoidPlayerId; // ID người chơi KHÔNG muốn xếp cùng
    private double discountAmount;
    private PlayStyle playStyle;  // Chế độ chơi: BALANCED, CHILL, INTENSE

    public Player(String id, String name, PlayerSkill skill, Gender gender) {
        this.id = id;
        this.name = name;
        this.skill = skill;
        this.gender = gender;
        this.matchesPlayed = 0;
        this.isResting = false;
        this.preferredTeammateId = null;
        this.avoidPlayerId = null;
        this.discountAmount = 0;
        this.playStyle = PlayStyle.BALANCED;
    }

    public boolean canPlay() {
        return matchesPlayed < 6 && !isResting;
    }

    // Getters & Setters
    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PlayerSkill getSkill() { return skill; }
    public void setSkill(PlayerSkill skill) { this.skill = skill; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public int getMatchesPlayed() { return matchesPlayed; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }

    public boolean isResting() { return isResting; }
    public void setResting(boolean resting) { this.isResting = resting; }

    public String getPreferredTeammateId() { return preferredTeammateId; }
    public void setPreferredTeammateId(String preferredTeammateId) { this.preferredTeammateId = preferredTeammateId; }

    public String getAvoidPlayerId() { return avoidPlayerId; }
    public void setAvoidPlayerId(String avoidPlayerId) { this.avoidPlayerId = avoidPlayerId; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public PlayStyle getPlayStyle() { return playStyle; }
    public void setPlayStyle(PlayStyle playStyle) { this.playStyle = playStyle != null ? playStyle : PlayStyle.BALANCED; }

    @Override
    public String toString() {
        return name + " (" + gender.getDisplayName() + " - " + skill.getDisplayName() + ")";
    }
}