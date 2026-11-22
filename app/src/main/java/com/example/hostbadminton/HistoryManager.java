package com.example.hostbadminton;

import org.json.JSONArray;
import org.json.JSONObject;

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
        SetRecord record = new SetRecord(currentSetRound, courtNum, team1.first, team1.second, team2.first, team2.second, skillDiff);
        setRecords.add(record);
        updateMapsForRecord(record);
        return record;
    }

    private void updateMapsForRecord(SetRecord record) {
        String t1Key = getPairKey(record.p1.getId(), record.p2.getId());
        teammateMap.put(t1Key, teammateMap.getOrDefault(t1Key, 0) + 1);

        String t2Key = getPairKey(record.p3.getId(), record.p4.getId());
        teammateMap.put(t2Key, teammateMap.getOrDefault(t2Key, 0) + 1);

        if (record.p1.getGender() == record.p2.getGender()) {
            sameGenderPairMap.put(record.p1.getId(), getSameGenderPairCount(record.p1.getId()) + 1);
            sameGenderPairMap.put(record.p2.getId(), getSameGenderPairCount(record.p2.getId()) + 1);
        }
        if (record.p3.getGender() == record.p4.getGender()) {
            sameGenderPairMap.put(record.p3.getId(), getSameGenderPairCount(record.p3.getId()) + 1);
            sameGenderPairMap.put(record.p4.getId(), getSameGenderPairCount(record.p4.getId()) + 1);
        }

        List<PlayerPair> crossPairs = Arrays.asList(
                new PlayerPair(record.p1, record.p3),
                new PlayerPair(record.p1, record.p4),
                new PlayerPair(record.p2, record.p3),
                new PlayerPair(record.p2, record.p4)
        );

        for (PlayerPair pair : crossPairs) {
            String key = getPairKey(pair.first.getId(), pair.second.getId());
            opponentMap.put(key, opponentMap.getOrDefault(key, 0) + 1);
        }
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

    public void setRecords(List<SetRecord> records) {
        clearHistory();
        if (records == null) return;
        int maxRound = 1;
        for (SetRecord rec : records) {
            setRecords.add(rec);
            updateMapsForRecord(rec);
            if (rec.getRoundNumber() >= maxRound) {
                maxRound = rec.getRoundNumber() + 1;
            }
        }
        currentSetRound = maxRound;
    }

    public void removeSetRecord(SetRecord record) {
        setRecords.remove(record);

        String t1Key = getPairKey(record.p1.getId(), record.p2.getId());
        teammateMap.put(t1Key, Math.max(0, teammateMap.getOrDefault(t1Key, 1) - 1));

        String t2Key = getPairKey(record.p3.getId(), record.p4.getId());
        teammateMap.put(t2Key, Math.max(0, teammateMap.getOrDefault(t2Key, 1) - 1));

        if (record.p1.getGender() == record.p2.getGender()) {
            sameGenderPairMap.put(record.p1.getId(), Math.max(0, getSameGenderPairCount(record.p1.getId()) - 1));
            sameGenderPairMap.put(record.p2.getId(), Math.max(0, getSameGenderPairCount(record.p2.getId()) - 1));
        }
        if (record.p3.getGender() == record.p4.getGender()) {
            sameGenderPairMap.put(record.p3.getId(), Math.max(0, getSameGenderPairCount(record.p3.getId()) - 1));
            sameGenderPairMap.put(record.p4.getId(), Math.max(0, getSameGenderPairCount(record.p4.getId()) - 1));
        }

        List<PlayerPair> crossPairs = Arrays.asList(
                new PlayerPair(record.p1, record.p3),
                new PlayerPair(record.p1, record.p4),
                new PlayerPair(record.p2, record.p3),
                new PlayerPair(record.p2, record.p4)
        );

        for (PlayerPair pair : crossPairs) {
            String key = getPairKey(pair.first.getId(), pair.second.getId());
            opponentMap.put(key, Math.max(0, opponentMap.getOrDefault(key, 1) - 1));
        }
    }

    public JSONArray toJson() {
        JSONArray array = new JSONArray();
        try {
            for (SetRecord rec : setRecords) {
                JSONObject obj = new JSONObject();
                obj.put("roundNumber", rec.roundNumber);
                obj.put("courtNumber", rec.courtNumber);
                obj.put("p1Id", rec.p1.getId());
                obj.put("p1Name", rec.p1.getName());
                obj.put("p1Skill", rec.p1.getSkill().name());
                obj.put("p1Gender", rec.p1.getGender().name());

                obj.put("p2Id", rec.p2.getId());
                obj.put("p2Name", rec.p2.getName());
                obj.put("p2Skill", rec.p2.getSkill().name());
                obj.put("p2Gender", rec.p2.getGender().name());

                obj.put("p3Id", rec.p3.getId());
                obj.put("p3Name", rec.p3.getName());
                obj.put("p3Skill", rec.p3.getSkill().name());
                obj.put("p3Gender", rec.p3.getGender().name());

                obj.put("p4Id", rec.p4.getId());
                obj.put("p4Name", rec.p4.getName());
                obj.put("p4Skill", rec.p4.getSkill().name());
                obj.put("p4Gender", rec.p4.getGender().name());

                obj.put("skillDiff", rec.skillDiff);
                if (rec.score1 != null) obj.put("score1", rec.score1);
                if (rec.score2 != null) obj.put("score2", rec.score2);

                array.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }

    public void fromJson(JSONArray array, List<Player> allPlayers) {
        clearHistory();
        if (array == null) return;
        try {
            int maxRound = 1;
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                int roundNum = obj.getInt("roundNumber");
                int courtNum = obj.getInt("courtNumber");
                double skillDiff = obj.getDouble("skillDiff");

                Player p1 = findPlayerById(allPlayers, obj.getString("p1Id"), obj.getString("p1Name"), PlayerSkill.valueOf(obj.getString("p1Skill")), Gender.valueOf(obj.getString("p1Gender")));
                Player p2 = findPlayerById(allPlayers, obj.getString("p2Id"), obj.getString("p2Name"), PlayerSkill.valueOf(obj.getString("p2Skill")), Gender.valueOf(obj.getString("p2Gender")));
                Player p3 = findPlayerById(allPlayers, obj.getString("p3Id"), obj.getString("p3Name"), PlayerSkill.valueOf(obj.getString("p3Skill")), Gender.valueOf(obj.getString("p3Gender")));
                Player p4 = findPlayerById(allPlayers, obj.getString("p4Id"), obj.getString("p4Name"), PlayerSkill.valueOf(obj.getString("p4Skill")), Gender.valueOf(obj.getString("p4Gender")));

                SetRecord rec = new SetRecord(roundNum, courtNum, p1, p2, p3, p4, skillDiff);
                if (obj.has("score1") && obj.has("score2")) {
                    rec.setScore(obj.getInt("score1"), obj.getInt("score2"));
                }

                setRecords.add(rec);
                updateMapsForRecord(rec);
                if (roundNum >= maxRound) {
                    maxRound = roundNum + 1;
                }
            }
            currentSetRound = maxRound;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Player findPlayerById(List<Player> allPlayers, String id, String name, PlayerSkill skill, Gender gender) {
        for (Player p : allPlayers) {
            if (p.getId().equals(id)) return p;
        }
        return new Player(id, name, skill, gender);
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

        public Player getP1() { return p1; }
        public Player getP2() { return p2; }
        public Player getP3() { return p3; }
        public Player getP4() { return p4; }

        public int getRoundNumber() { return roundNumber; }
        public int getCourtNumber() { return courtNumber; }
        public double getSkillDiff() { return skillDiff; }

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