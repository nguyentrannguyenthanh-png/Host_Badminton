package com.example.hostbadminton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BadmintonMatchScheduler {

    /**
     * Đánh giá và chọn cách chia 2 đội tối ưu nhất cho 4 người chơi
     */
    public static MatchOption evaluateMatch(Player p1, Player p2, Player p3, Player p4, HistoryManager historyManager) {
        OptionTriple[] options = new OptionTriple[] {
                createOption(p1, p2, p3, p4, historyManager),
                createOption(p1, p3, p2, p4, historyManager),
                createOption(p1, p4, p2, p3, historyManager)
        };

        MatchOption bestOption = null;

        for (OptionTriple opt : options) {
            HistoryManager.PlayerPair t1 = opt.team1;
            HistoryManager.PlayerPair t2 = opt.team2;

            boolean team1Repeat = historyManager.getTeammateCount(t1.first.getId(), t1.second.getId()) > 2;
            boolean team2Repeat = historyManager.getTeammateCount(t2.first.getId(), t2.second.getId()) > 2;

            boolean opp1 = historyManager.getOpponentCount(t1.first.getId(), t2.first.getId()) > 2;
            boolean opp2 = historyManager.getOpponentCount(t1.first.getId(), t2.second.getId()) > 2;
            boolean opp3 = historyManager.getOpponentCount(t1.second.getId(), t2.first.getId()) > 2;
            boolean opp4 = historyManager.getOpponentCount(t1.second.getId(), t2.second.getId()) > 2;

            boolean isValid = !team1Repeat && !team2Repeat && !opp1 && !opp2 && !opp3 && !opp4;
            MatchOption currentOption = new MatchOption(t1, t2, opt.skillDiff, opt.genderDiff, opt.historyPenalty, isValid);

            if (bestOption == null) {
                bestOption = currentOption;
            } else {
                if (currentOption.getTotalPenalty() < bestOption.getTotalPenalty()) {
                    bestOption = currentOption;
                }
            }
        }

        return bestOption;
    }

    private static OptionTriple createOption(Player a1, Player a2, Player b1, Player b2, HistoryManager historyManager) {
        HistoryManager.PlayerPair team1 = new HistoryManager.PlayerPair(a1, a2);
        HistoryManager.PlayerPair team2 = new HistoryManager.PlayerPair(b1, b2);

        // Chênh lệch điểm trình độ
        double skillDiff = Math.abs((a1.getSkill().getScore() + a2.getSkill().getScore()) - (b1.getSkill().getScore() + b2.getSkill().getScore()));

        // Cân bằng giới tính
        int team1Males = (a1.getGender() == Gender.MALE ? 1 : 0) + (a2.getGender() == Gender.MALE ? 1 : 0);
        int team2Males = (b1.getGender() == Gender.MALE ? 1 : 0) + (b2.getGender() == Gender.MALE ? 1 : 0);
        double genderDiff = Math.abs(team1Males - team2Males);

        // Phạt lặp lại đồng đội (+10 điểm / lần) & đối thủ (+2 điểm / lần)
        int t1Repeats = historyManager.getTeammateCount(a1.getId(), a2.getId());
        int t2Repeats = historyManager.getTeammateCount(b1.getId(), b2.getId());

        int opp1 = historyManager.getOpponentCount(a1.getId(), b1.getId());
        int opp2 = historyManager.getOpponentCount(a1.getId(), b2.getId());
        int opp3 = historyManager.getOpponentCount(a2.getId(), b1.getId());
        int opp4 = historyManager.getOpponentCount(a2.getId(), b2.getId());

        double historyPenalty = (t1Repeats + t2Repeats) * 10.0 + (opp1 + opp2 + opp3 + opp4) * 2.0;

        return new OptionTriple(team1, team2, skillDiff, genderDiff, historyPenalty);
    }

    public static List<MatchOption> suggestMatches(List<Player> availablePlayers, HistoryManager historyManager) {
        List<MatchOption> result = new ArrayList<>();
        int count = availablePlayers.size();

        if (count < 4) {
            return result; // Không đủ 4 người
        }

        int numCourts = 1;
        if (count >= 17) {
            numCourts = 3; // Nhóm từ 17 người trở lên -> Xếp 3 sân (12 người đánh)
        } else if (count >= 9) {
            numCourts = 2; // Nhóm từ 9 - 16 người -> Xếp 2 sân (8 người đánh)
        } else {
            numCourts = 1; // Nhóm từ 4 - 8 người -> Xếp 1 sân (4 người đánh)
        }

        int totalNeeded = numCourts * 4;
        List<Player> topPlayers = availablePlayers.subList(0, totalNeeded);

        if (numCourts == 1) {
            MatchOption match = evaluateMatch(topPlayers.get(0), topPlayers.get(1), topPlayers.get(2), topPlayers.get(3), historyManager);
            if (match != null) result.add(match);
        } else if (numCourts == 2) {
            result = partitionTwoCourts(topPlayers, historyManager);
        } else if (numCourts == 3) {
            result = partitionThreeCourts(topPlayers, historyManager);
        }

        return result;
    }

    private static List<MatchOption> partitionTwoCourts(List<Player> players, HistoryManager historyManager) {
        List<MatchOption> bestMatches = null;
        double bestPenalty = Double.MAX_VALUE;

        for (int i = 1; i < 8; i++) {
            for (int j = i + 1; j < 8; j++) {
                for (int k = j + 1; k < 8; k++) {
                    List<Player> group1 = Arrays.asList(players.get(0), players.get(i), players.get(j), players.get(k));
                    List<Player> group2 = new ArrayList<>();
                    for (int m = 0; m < 8; m++) {
                        if (m != 0 && m != i && m != j && m != k) {
                            group2.add(players.get(m));
                        }
                    }

                    MatchOption m1 = evaluateMatch(group1.get(0), group1.get(1), group1.get(2), group1.get(3), historyManager);
                    MatchOption m2 = evaluateMatch(group2.get(0), group2.get(1), group2.get(2), group2.get(3), historyManager);

                    if (m1 != null && m2 != null) {
                        double totalPenalty = m1.getTotalPenalty() + m2.getTotalPenalty();
                        if (totalPenalty < bestPenalty) {
                            bestPenalty = totalPenalty;
                            bestMatches = Arrays.asList(m1, m2);
                        }
                    }
                }
            }
        }
        return bestMatches != null ? bestMatches : new ArrayList<>();
    }

    private static List<MatchOption> partitionThreeCourts(List<Player> players, HistoryManager historyManager) {
        List<MatchOption> bestMatches = null;
        double bestPenalty = Double.MAX_VALUE;

        for (int i = 1; i < 12; i++) {
            for (int j = i + 1; j < 12; j++) {
                for (int k = j + 1; k < 12; k++) {
                    List<Player> g1 = Arrays.asList(players.get(0), players.get(i), players.get(j), players.get(k));
                    List<Player> rem8 = new ArrayList<>();
                    for (int m = 0; m < 12; m++) {
                        if (m != 0 && m != i && m != j && m != k) rem8.add(players.get(m));
                    }

                    for (int a = 1; a < 8; a++) {
                        for (int b = a + 1; b < 8; b++) {
                            for (int c = b + 1; c < 8; c++) {
                                List<Player> g2 = Arrays.asList(rem8.get(0), rem8.get(a), rem8.get(b), rem8.get(c));
                                List<Player> g3 = new ArrayList<>();
                                for (int x = 0; x < 8; x++) {
                                    if (x != 0 && x != a && x != b && x != c) g3.add(rem8.get(x));
                                }

                                MatchOption m1 = evaluateMatch(g1.get(0), g1.get(1), g1.get(2), g1.get(3), historyManager);
                                MatchOption m2 = evaluateMatch(g2.get(0), g2.get(1), g2.get(2), g2.get(3), historyManager);
                                MatchOption m3 = evaluateMatch(g3.get(0), g3.get(1), g3.get(2), g3.get(3), historyManager);

                                if (m1 != null && m2 != null && m3 != null) {
                                    double totalPenalty = m1.getTotalPenalty() + m2.getTotalPenalty() + m3.getTotalPenalty();
                                    if (totalPenalty < bestPenalty) {
                                        bestPenalty = totalPenalty;
                                        bestMatches = Arrays.asList(m1, m2, m3);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return bestMatches != null ? bestMatches : new ArrayList<>();
    }

    private static class OptionTriple {
        HistoryManager.PlayerPair team1;
        HistoryManager.PlayerPair team2;
        double skillDiff;
        double genderDiff;
        double historyPenalty;

        public OptionTriple(HistoryManager.PlayerPair team1, HistoryManager.PlayerPair team2, double skillDiff, double genderDiff, double historyPenalty) {
            this.team1 = team1;
            this.team2 = team2;
            this.skillDiff = skillDiff;
            this.genderDiff = genderDiff;
            this.historyPenalty = historyPenalty;
        }
    }
}