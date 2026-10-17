package com.example.hostbadminton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BadmintonMatchScheduler {

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

        // 1. Chênh lệch điểm trình độ (trọng số 5.0)
        double skillDiff = Math.abs((a1.getSkill().getScore() + a2.getSkill().getScore()) - (b1.getSkill().getScore() + b2.getSkill().getScore())) * 5.0;

        // 2. Xoay vòng đa dạng thể loại trận
        int team1Males = (a1.getGender() == Gender.MALE ? 1 : 0) + (a2.getGender() == Gender.MALE ? 1 : 0);
        int team2Males = (b1.getGender() == Gender.MALE ? 1 : 0) + (b2.getGender() == Gender.MALE ? 1 : 0);

        int totalMales = team1Males + team2Males;
        double genderDiff = 0.0;

        if (totalMales == 2) {
            if (team1Males == 1 && team2Males == 1) {
                genderDiff = 0.0;
            } else {
                genderDiff = 35.0;
            }
        } else if (totalMales == 0 || totalMales == 4) {
            int sameGenderRepeats = historyManager.getSameGenderPairCount(a1.getId()) +
                    historyManager.getSameGenderPairCount(a2.getId()) +
                    historyManager.getSameGenderPairCount(b1.getId()) +
                    historyManager.getSameGenderPairCount(b2.getId());
            genderDiff = sameGenderRepeats * 4.0;
        } else {
            genderDiff = 8.0;
        }

        // 3. Ưu tiên ghép cặp theo Yêu cầu
        double preferenceBonus = 0.0;
        if ((a1.getPreferredTeammateId() != null && a1.getPreferredTeammateId().equals(a2.getId())) ||
                (a2.getPreferredTeammateId() != null && a2.getPreferredTeammateId().equals(a1.getId()))) {
            preferenceBonus += 150.0;
        }
        if ((b1.getPreferredTeammateId() != null && b1.getPreferredTeammateId().equals(b2.getId())) ||
                (b2.getPreferredTeammateId() != null && b2.getPreferredTeammateId().equals(b1.getId()))) {
            preferenceBonus += 150.0;
        }

        // 4. Phạt lặp lại
        int t1Repeats = historyManager.getTeammateCount(a1.getId(), a2.getId());
        int t2Repeats = historyManager.getTeammateCount(b1.getId(), b2.getId());

        int opp1 = historyManager.getOpponentCount(a1.getId(), b1.getId());
        int opp2 = historyManager.getOpponentCount(a1.getId(), b2.getId());
        int opp3 = historyManager.getOpponentCount(a2.getId(), b1.getId());
        int opp4 = historyManager.getOpponentCount(a2.getId(), b2.getId());

        double historyPenalty = (t1Repeats + t2Repeats) * 15.0 + (opp1 + opp2 + opp3 + opp4) * 3.0 - preferenceBonus;

        return new OptionTriple(team1, team2, skillDiff, genderDiff, historyPenalty);
    }

    public static List<MatchOption> suggestMatches(List<Player> availablePlayers, HistoryManager historyManager) {
        List<MatchOption> result = new ArrayList<>();
        int count = availablePlayers.size();

        if (count < 4) {
            return result;
        }

        int numCourts = 1;
        if (count >= 17) {
            numCourts = 3;
        } else if (count >= 9) {
            numCourts = 2;
        } else {
            numCourts = 1;
        }

        int totalNeeded = numCourts * 4;

        if (count > totalNeeded) {
            return findBestPlayerCombination(availablePlayers, numCourts, historyManager);
        }

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

    private static List<MatchOption> findBestPlayerCombination(List<Player> availablePlayers, int numCourts, HistoryManager historyManager) {
        int totalNeeded = numCourts * 4;
        List<MatchOption> bestMatches = null;
        double bestPenalty = Double.MAX_VALUE;

        int minMatches = availablePlayers.get(0).getMatchesPlayed();
        List<Player> candidatePool = new ArrayList<>();
        for (Player p : availablePlayers) {
            if (p.getMatchesPlayed() <= minMatches + 1) {
                candidatePool.add(p);
            }
        }
        if (candidatePool.size() < totalNeeded) {
            candidatePool = availablePlayers.subList(0, Math.min(availablePlayers.size(), totalNeeded + 4));
        }

        if (numCourts == 1 && candidatePool.size() >= 4) {
            for (int i = 0; i < candidatePool.size(); i++) {
                for (int j = i + 1; j < candidatePool.size(); j++) {
                    for (int k = j + 1; k < candidatePool.size(); k++) {
                        for (int m = k + 1; m < candidatePool.size(); m++) {
                            MatchOption match = evaluateMatch(candidatePool.get(i), candidatePool.get(j), candidatePool.get(k), candidatePool.get(m), historyManager);
                            if (match != null && match.getTotalPenalty() < bestPenalty) {
                                bestPenalty = match.getTotalPenalty();
                                bestMatches = Arrays.asList(match);
                            }
                        }
                    }
                }
            }
        } else if (numCourts == 2 && candidatePool.size() >= 8) {
            bestMatches = partitionTwoCourts(candidatePool.subList(0, 8), historyManager);
        } else if (numCourts == 3 && candidatePool.size() >= 12) {
            bestMatches = partitionThreeCourts(candidatePool.subList(0, 12), historyManager);
        }

        return bestMatches != null ? bestMatches : new ArrayList<>();
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