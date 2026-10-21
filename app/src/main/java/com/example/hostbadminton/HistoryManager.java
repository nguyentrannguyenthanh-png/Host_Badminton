package com.example.hostbadminton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryManager {
    private final Map<String, Integer> teammateMap = new HashMap<>();
    private final Map<String, Integer> opponentMap = new HashMap<>();
    private final Map<String, Integer> sameGenderPairMap = new HashMap<>();
    private final List<SetRecord> setRecords = new ArrayList<>();
    private int currentSetRound = 1;

    private String getPairKey(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "-" + id2 : id2 + "-" + id1;
    }

    public int getTeammateCount(String id1, String id2) {
        return teammateMap.getOrDefault(getPairKey(id1, id2), 0);
    }

    public int getOpponentCount(String id1, String id2) {
        return opponentMap.getOrDefault(getPairKey(id1, id2), 0);
    }

    public int getSameGenderPairCount(String playerId) {
        return sameGenderPairMap.getOrDefault(playerId, 0);
    }

    public SetRecord recordMatch(int courtNum, PlayerPair team1, PlayerPair team2, double skillDiff) {
        String t1Key = getPairKey(team1.first.getId(), team1.second.getId());
        teammateMap.put(t1Key, teammateMap.getOrDefault(t1Key, 0) + 1);

        String t2Key = getPairKey(team2.first.getId(), team2.second.getId());
        teammateMap.put(t2Key, teammateMap.getOrDefault(t2Key, 0) + 1);

        if (team1.first.getGender() == team1.second.getGender()) {
            sameGenderPairMap.put(team1.first.getId(), getSameGenderPairCount(team1.first.getId()) + 1);
            sameGenderPairMap.put(team1.second.getId(), getSameGenderPairCount(team1.second.getId()) + 1);
        }
        if (team2.first.getGender() == team2.second.getGender()) {
            sameGenderPairMap.put(team2.first.getId(), getSameGenderPairCount(team2.first.getId()) + 1);
            sameGenderPairMap.put(team2.second.getId(), getSameGenderPairCount(team2.second.getId()) + 1);
        }

        List<PlayerPair> crossPairs = Arrays.asList(
                new PlayerPair(team1.first, team2.first),
                new PlayerPair(team1.first, team2.second),
                new PlayerPair(team1.second, team2.first),
                new PlayerPair(team1.second, team2.second)
        );

        for (PlayerPair pair : crossPairs) {
            String key = getPairKey(pair.first.getId(), pair.second.getId());
            opponentMap.put(key, opponentMap.getOrDefault(key, 0) + 1);
        }

        SetRecord record = new SetRecord(currentSetRound, courtNum, team1.first, team1.second, team2.first, team2.second, skillDiff);
        setRecords.add(record);
        return record;
    }

    public void nextRound() {
        currentSetRound++;
    }

    public List<SetRecord> getAllSetRecords() {
        return setRecords;
    }

    public List<SetRecord> getSetRecordsForPlayer(String playerId) {
        List<SetRecord> result = new ArrayList<>();
        for (SetRecord record : setRecords) {
            if (record.containsPlayer(playerId)) {
                result.add(record);
            }
        }
        return result;
    }

    public void clearHistory() {
        teammateMap.clear();
        opponentMap.clear();
        sameGenderPairMap.clear();
        setRecords.clear();
        currentSetRound = 1;
    }

    public static class SetRecord {
        private final int roundNumber;
        private final int courtNumber;
        private final Player p1, p2, p3, p4;
        private final double skillDiff;
        private Integer score1 = null;
        private Integer score2 = null;

        public SetRecord(int roundNumber, int courtNumber, Player p1, Player p2, Player p3, Player p4, double skillDiff) {
            this.roundNumber = roundNumber;
            this.courtNumber = courtNumber;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
            this.p4 = p4;
            this.skillDiff = skillDiff;
        }

        public void setScore(int score1, int score2) {
            this.score1 = score1;
            this.score2 = score2;
        }

        public Integer getScore1() { return score1; }
        public Integer getScore2() { return score2; }

        // 🟢 THÊM 2 HÀM NÀY VÀO LÀ HẾT LỖI
        public int getRoundNumber() { return roundNumber; }
        public int getCourtNumber() { return courtNumber; }

        public boolean containsPlayer(String playerId) {
            return p1.getId().equals(playerId) || p2.getId().equals(playerId) ||
                    p3.getId().equals(playerId) || p4.getId().equals(playerId);
        }

        public String getFormattedText() {
            String scoreText = (score1 != null && score2 != null) ? " [Tỉ số: " + score1 + " - " + score2 + "]" : "";
            return "Lượt " + roundNumber + " - Sân " + courtNumber + scoreText + ":\n" +
                    "  Đội 1: " + p1.getName() + " (" + p1.getGender().getDisplayName() + " - " + p1.getSkill().getDisplayName() + ") + " + p2.getName() + " (" + p2.getGender().getDisplayName() + " - " + p2.getSkill().getDisplayName() + ")\n" +
                    "  VS\n" +
                    "  Đội 2: " + p3.getName() + " (" + p3.getGender().getDisplayName() + " - " + p3.getSkill().getDisplayName() + ") + " + p4.getName() + " (" + p4.getGender().getDisplayName() + " - " + p4.getSkill().getDisplayName() + ")\n" +
                    "  (Lệch trình: " + String.format("%.2f", skillDiff) + ")";
        }
    }

    public static class PlayerPair {
        public Player first;
        public Player second;

        public PlayerPair(Player first, Player second) {
            this.first = first;
            this.second = second;
        }
    }
}