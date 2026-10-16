package com.example.hostbadminton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryManager {
    private final Map<String, Integer> teammateMap = new HashMap<>();
    private final Map<String, Integer> opponentMap = new HashMap<>();
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

    // Ghi nhận lịch sử sau khi kết thúc một trận đấu
    public void recordMatch(int courtNum, PlayerPair team1, PlayerPair team2) {
        // Ghi nhận đồng đội Đội 1
        String t1Key = getPairKey(team1.first.getId(), team1.second.getId());
        teammateMap.put(t1Key, teammateMap.getOrDefault(t1Key, 0) + 1);

        // Ghi nhận đồng đội Đội 2
        String t2Key = getPairKey(team2.first.getId(), team2.second.getId());
        teammateMap.put(t2Key, teammateMap.getOrDefault(t2Key, 0) + 1);

        // Ghi nhận đối thủ (chéo giữa 2 đội)
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

        // Lưu đối tượng SetRecord để tra cứu sau này
        SetRecord record = new SetRecord(currentSetRound, courtNum, team1.first, team1.second, team2.first, team2.second);
        setRecords.add(record);
    }

    public void nextRound() {
        currentSetRound++;
    }

    public List<SetRecord> getAllSetRecords() {
        return setRecords;
    }

    // Lấy danh sách các Set mà 1 người chơi cụ thể đã tham gia
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
        setRecords.clear();
        currentSetRound = 1;
    }

    // Lớp phụ trợ gói thông tin 1 Set đấu
    public static class SetRecord {
        private final int roundNumber;
        private final int courtNumber;
        private final Player p1, p2, p3, p4;

        public SetRecord(int roundNumber, int courtNumber, Player p1, Player p2, Player p3, Player p4) {
            this.roundNumber = roundNumber;
            this.courtNumber = courtNumber;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
            this.p4 = p4;
        }

        public boolean containsPlayer(String playerId) {
            return p1.getId().equals(playerId) || p2.getId().equals(playerId) ||
                    p3.getId().equals(playerId) || p4.getId().equals(playerId);
        }

        public String getFormattedText() {
            return "Lượt " + roundNumber + " - Sân " + courtNumber + ":\n" +
                    "  Đội 1: " + p1.getName() + " (" + p1.getGender().getDisplayName() + ") + " + p2.getName() + " (" + p2.getGender().getDisplayName() + ")\n" +
                    "  VS\n" +
                    "  Đội 2: " + p3.getName() + " (" + p3.getGender().getDisplayName() + ") + " + p4.getName() + " (" + p4.getGender().getDisplayName() + ")";
        }
    }

    // Lớp phụ trợ gói cặp 2 người chơi
    public static class PlayerPair {
        public Player first;
        public Player second;

        public PlayerPair(Player first, Player second) {
            this.first = first;
            this.second = second;
        }
    }
}