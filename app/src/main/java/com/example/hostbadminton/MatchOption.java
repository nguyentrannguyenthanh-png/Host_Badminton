package com.example.hostbadminton;

public class MatchOption {
    private HistoryManager.PlayerPair team1;
    private HistoryManager.PlayerPair team2;
    private double skillDiff;
    private double genderDiff;
    private double historyPenalty;
    private double totalPenalty;
    private boolean isValidByHistory;

    public MatchOption(HistoryManager.PlayerPair team1, HistoryManager.PlayerPair team2, double skillDiff, double genderDiff, double historyPenalty, boolean isValidByHistory) {
        this.team1 = team1;
        this.team2 = team2;
        this.skillDiff = skillDiff;
        this.genderDiff = genderDiff;
        this.historyPenalty = historyPenalty;
        this.totalPenalty = skillDiff + (genderDiff * 2.0) + historyPenalty;
        this.isValidByHistory = isValidByHistory;
    }

    public HistoryManager.PlayerPair getTeam1() { return team1; }
    public HistoryManager.PlayerPair getTeam2() { return team2; }
    public double getSkillDiff() { return skillDiff; }
    public double getGenderDiff() { return genderDiff; }
    public double getHistoryPenalty() { return historyPenalty; }
    public double getTotalPenalty() { return totalPenalty; }
    public boolean isValidByHistory() { return isValidByHistory; }
}