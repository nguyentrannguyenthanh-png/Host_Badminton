package com.example.hostbadminton;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etPlayerName;
    private Spinner spinnerGender, spinnerSkill;
    private Button btnAddPlayer, btnSuggestMatch, btnViewHistory, btnReset, btnReload;
    private TextView tvMatchResult;
    private ListView lvPlayers;

    private List<Player> playerList = new ArrayList<>();
    private HistoryManager historyManager = new HistoryManager();
    private List<HistoryManager.SetRecord> currentRoundRecords = new ArrayList<>();
    private ArrayAdapter<Player> playerAdapter;

    private static final String PREFS_NAME = "HostBadmintonPrefs";
    private static final String KEY_PLAYERS = "saved_players";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ View
        etPlayerName = findViewById(R.id.etPlayerName);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerSkill = findViewById(R.id.spinnerSkill);
        btnAddPlayer = findViewById(R.id.btnAddPlayer);
        btnSuggestMatch = findViewById(R.id.btnSuggestMatch);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnReset = findViewById(R.id.btnReset);
        btnReload = findViewById(R.id.btnReload);
        tvMatchResult = findViewById(R.id.tvMatchResult);
        lvPlayers = findViewById(R.id.lvPlayers);

        // Tải dữ liệu lưu trữ vĩnh viễn (Database)
        loadPlayersFromPrefs();

        // Nút Tải Lại
        btnReload.setOnClickListener(v -> {
            recreate();
            Toast.makeText(this, "Đã tải lại trang!", Toast.LENGTH_SHORT).show();
        });

        // Đổ dữ liệu Giới tính vào Spinner
        ArrayAdapter<Gender> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Gender.values());
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Đổ dữ liệu mức trình độ vào Spinner
        ArrayAdapter<PlayerSkill> skillAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, PlayerSkill.values());
        skillAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSkill.setAdapter(skillAdapter);

        // Khởi tạo Adapter hiển thị danh sách người chơi
        playerAdapter = new ArrayAdapter<Player>(this, R.layout.item_player, R.id.tvPlayerInfo, playerList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Player player = getItem(position);

                TextView tvInfo = view.findViewById(R.id.tvPlayerInfo);
                TextView tvCount = view.findViewById(R.id.tvMatchCount);

                if (player != null) {
                    String statusText = player.isResting() ? " [ĐANG NGHỈ]" : "";
                    String prefText = "";
                    if (player.getPreferredTeammateId() != null) {
                        Player partner = getPlayerById(player.getPreferredTeammateId());
                        if (partner != null) {
                            prefText = " [Cặp: " + partner.getName() + "]";
                        }
                    }
                    int stt = position + 1;
                    tvInfo.setText(stt + ". " + player.getName() + " (" + player.getGender().getDisplayName() + " - " + player.getSkill().getDisplayName() + ")" + prefText + statusText);
                    tvCount.setText(player.getMatchesPlayed() + "/6 set");
                }
                return view;
            }
        };
        lvPlayers.setAdapter(playerAdapter);

        // Sự kiện khi CHẠM vào 1 người chơi
        lvPlayers.setOnItemClickListener((parent, view, position, id) -> {
            Player player = playerList.get(position);
            showPlayerOptionsDialog(player, position);
        });

        // Sự kiện Thêm người chơi
        btnAddPlayer.setOnClickListener(v -> {
            String name = etPlayerName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên người chơi!", Toast.LENGTH_SHORT).show();
                return;
            }
            Gender selectedGender = (Gender) spinnerGender.getSelectedItem();
            PlayerSkill selectedSkill = (PlayerSkill) spinnerSkill.getSelectedItem();
            String id = String.valueOf(System.currentTimeMillis());

            Player newPlayer = new Player(id, name, selectedSkill, selectedGender);
            playerList.add(newPlayer);
            savePlayersToPrefs();
            playerAdapter.notifyDataSetChanged();

            etPlayerName.setText("");
            Toast.makeText(this, "Đã thêm: " + name + " (" + selectedGender.getDisplayName() + ")", Toast.LENGTH_SHORT).show();
        });

        // Sự kiện Xem Lịch Sử & Nhập Tỉ Số Trận
        btnViewHistory.setOnClickListener(v -> {
            List<HistoryManager.SetRecord> records = historyManager.getAllSetRecords();
            if (records.isEmpty()) {
                Toast.makeText(this, "Chưa có lịch sử set đấu nào!", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder historyText = new StringBuilder();
            for (HistoryManager.SetRecord record : records) {
                historyText.append(record.getFormattedText()).append("\n-------------------------------------\n");
            }

            new AlertDialog.Builder(this)
                    .setTitle("Lịch Sử Tất Cả Các Set (" + records.size() + " set)")
                    .setMessage(historyText.toString().trim())
                    .setPositiveButton("Đóng", null)
                    .setNeutralButton("Nhập Tỉ Số Trận", (dialog, which) -> {
                        showScoreInputDialog();
                    })
                    .show();
        });

        // Sự kiện Làm Mới (2 TÙY CHỌN MỚI)
        btnReset.setOnClickListener(v -> {
            String[] resetOptions = {
                    "1. Bắt đầu buổi mới (Reset các set về 0 & giữ danh sách)",
                    "2. Reset hết tất cả (Xóa danh sách người chơi & lịch sử)"
            };

            new AlertDialog.Builder(this)
                    .setTitle("Tùy Chọn Làm Mới Dữ Liệu")
                    .setItems(resetOptions, (dialog, which) -> {
                        if (which == 0) {
                            for (Player p : playerList) {
                                p.setMatchesPlayed(0);
                                p.setResting(false);
                                p.setPreferredTeammateId(null);
                            }
                            historyManager.clearHistory();
                            currentRoundRecords.clear();
                            tvMatchResult.setText("Đã reset các set về 0! Giữ nguyên danh sách.");
                            savePlayersToPrefs();
                            playerAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "Đã reset các set về 0!", Toast.LENGTH_SHORT).show();
                        } else if (which == 1) {
                            playerList.clear();
                            historyManager.clearHistory();
                            currentRoundRecords.clear();
                            tvMatchResult.setText("Trận đấu tiếp theo sẽ hiển thị ở đây...");
                            savePlayersToPrefs();
                            playerAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "Đã xóa toàn bộ người chơi!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // Sự kiện Xếp Trận Tự Động
        btnSuggestMatch.setOnClickListener(v -> {
            List<Player> availablePlayers = new ArrayList<>();
            for (Player p : playerList) {
                if (p.canPlay()) {
                    availablePlayers.add(p);
                }
            }

            if (availablePlayers.size() < 4) {
                Toast.makeText(this, "Không đủ 4 người chơi hợp lệ (dưới 6 set và không tạm nghỉ)!", Toast.LENGTH_SHORT).show();
                return;
            }

            Collections.sort(availablePlayers, (p1, p2) -> Integer.compare(p1.getMatchesPlayed(), p2.getMatchesPlayed()));

            List<MatchOption> matches = BadmintonMatchScheduler.suggestMatches(availablePlayers, historyManager);

            if (matches != null && !matches.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                int totalPlaying = matches.size() * 4;
                int restingCount = availablePlayers.size() - totalPlaying;

                sb.append("Tổng khả dụng: ").append(availablePlayers.size())
                        .append(" người -> Xếp ").append(matches.size()).append(" sân (")
                        .append(totalPlaying).append(" người đánh, ").append(restingCount).append(" người nghỉ xoay vòng)\n\n");

                currentRoundRecords.clear();
                for (int i = 0; i < matches.size(); i++) {
                    MatchOption match = matches.get(i);
                    int courtNum = i + 1;

                    sb.append("=== SÂN ").append(courtNum).append(" ===\n")
                            .append("Đội 1: ").append(match.getTeam1().first.getName()).append(" (").append(match.getTeam1().first.getGender().getDisplayName()).append(" - ").append(match.getTeam1().first.getSkill().getDisplayName()).append(") + ")
                            .append(match.getTeam1().second.getName()).append(" (").append(match.getTeam1().second.getGender().getDisplayName()).append(" - ").append(match.getTeam1().second.getSkill().getDisplayName()).append(")\n")
                            .append("VS\n")
                            .append("Đội 2: ").append(match.getTeam2().first.getName()).append(" (").append(match.getTeam2().first.getGender().getDisplayName()).append(" - ").append(match.getTeam2().first.getSkill().getDisplayName()).append(") + ")
                            .append(match.getTeam2().second.getName()).append(" (").append(match.getTeam2().second.getGender().getDisplayName()).append(" - ").append(match.getTeam2().second.getSkill().getDisplayName()).append(")\n")
                            .append("(Lệch trình: ").append(String.format("%.2f", match.getSkillDiff()))
                            .append(" | Nam/Nữ: ").append(match.getGenderDiff() == 0 ? "Tốt" : "Lệch " + (int)match.getGenderDiff())
                            .append(")\n\n");

                    match.getTeam1().first.setMatchesPlayed(match.getTeam1().first.getMatchesPlayed() + 1);
                    match.getTeam1().second.setMatchesPlayed(match.getTeam1().second.getMatchesPlayed() + 1);
                    match.getTeam2().first.setMatchesPlayed(match.getTeam2().first.getMatchesPlayed() + 1);
                    match.getTeam2().second.setMatchesPlayed(match.getTeam2().second.getMatchesPlayed() + 1);

                    HistoryManager.SetRecord rec = historyManager.recordMatch(courtNum, match.getTeam1(), match.getTeam2(), match.getSkillDiff());
                    currentRoundRecords.add(rec);
                }

                historyManager.nextRound();
                tvMatchResult.setText(sb.toString().trim());
                savePlayersToPrefs();
                playerAdapter.notifyDataSetChanged();

                // Hộp thoại gợi ý nhập tỉ số ngay cho các sân
                new AlertDialog.Builder(this)
                        .setTitle("Xếp Trận Thành Công!")
                        .setMessage("Bạn có muốn nhập tỉ số cho các sân ngay bây giờ?")
                        .setPositiveButton("Nhập Tỉ Số", (d, w) -> showScoreInputDialog())
                        .setNegativeButton("Để Sau", null)
                        .show();

            } else {
                Toast.makeText(this, "Không tìm được phương án xếp trận phù hợp!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Player getPlayerById(String id) {
        for (Player p : playerList) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    private void savePlayersToPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray array = new JSONArray();

        for (Player p : playerList) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", p.getId());
                obj.put("name", p.getName());
                obj.put("gender", p.getGender().name());
                obj.put("skill", p.getSkill().name());
                obj.put("matchesPlayed", p.getMatchesPlayed());
                obj.put("isResting", p.isResting());
                obj.put("preferredTeammateId", p.getPreferredTeammateId() != null ? p.getPreferredTeammateId() : "");
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        editor.putString(KEY_PLAYERS, array.toString());
        editor.apply();
    }

    private void loadPlayersFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_PLAYERS, null);
        playerList.clear();

        if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
                JSONArray array = new JSONArray(jsonStr);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String id = obj.getString("id");
                    String name = obj.getString("name");
                    Gender gender = Gender.valueOf(obj.getString("gender"));
                    PlayerSkill skill = PlayerSkill.valueOf(obj.getString("skill"));
                    int matchesPlayed = obj.getInt("matchesPlayed");
                    boolean isResting = obj.getBoolean("isResting");
                    String prefId = obj.optString("preferredTeammateId", "");

                    Player p = new Player(id, name, skill, gender);
                    p.setMatchesPlayed(matchesPlayed);
                    p.setResting(isResting);
                    if (!prefId.isEmpty()) p.setPreferredTeammateId(prefId);

                    playerList.add(p);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // 🟢 HỘP THOẠI NHẬP TỈ SỐ CHO CÁC TRẬN ĐANG ĐÁNH
    private void showScoreInputDialog() {
        List<HistoryManager.SetRecord> allRecords = historyManager.getAllSetRecords();
        if (allRecords.isEmpty()) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        List<EditText> inputs1 = new ArrayList<>();
        List<EditText> inputs2 = new ArrayList<>();

        for (int i = 0; i < allRecords.size(); i++) {
            HistoryManager.SetRecord rec = allRecords.get(i);

            TextView tvTitle = new TextView(this);
            tvTitle.setText("Trận " + (i + 1) + " (Sân " + rec.getCourtNumber() + " - Lượt " + rec.getRoundNumber() + "):");
            tvTitle.setPadding(0, 20, 0, 10);
            tvTitle.setTextSize(14);
            layout.addView(tvTitle);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            EditText et1 = new EditText(this);
            et1.setHint("Điểm Đội 1");
            et1.setInputType(InputType.TYPE_CLASS_NUMBER);
            if (rec.getScore1() != null) et1.setText(String.valueOf(rec.getScore1()));
            LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            et1.setLayoutParams(p1);
            row.addView(et1);
            inputs1.add(et1);

            TextView tvVs = new TextView(this);
            tvVs.setText(" - ");
            tvVs.setTextSize(16);
            row.addView(tvVs);

            EditText et2 = new EditText(this);
            et2.setHint("Điểm Đội 2");
            et2.setInputType(InputType.TYPE_CLASS_NUMBER);
            if (rec.getScore2() != null) et2.setText(String.valueOf(rec.getScore2()));
            LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            et2.setLayoutParams(p2);
            row.addView(et2);
            inputs2.add(et2);

            layout.addView(row);
        }

        new AlertDialog.Builder(this)
                .setTitle("Nhập Tỉ Số Các Set")
                .setView(layout)
                .setPositiveButton("Lưu Tỉ Số", (dialog, which) -> {
                    for (int i = 0; i < allRecords.size(); i++) {
                        String s1 = inputs1.get(i).getText().toString().trim();
                        String s2 = inputs2.get(i).getText().toString().trim();
                        if (!s1.isEmpty() && !s2.isEmpty()) {
                            try {
                                int sc1 = Integer.parseInt(s1);
                                int sc2 = Integer.parseInt(s2);
                                allRecords.get(i).setScore(sc1, sc2);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    Toast.makeText(this, "Đã lưu tỉ số thành công!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showPlayerOptionsDialog(Player player, int position) {
        String reqOptionText = (player.getPreferredTeammateId() != null) ?
                "Hủy yêu cầu ghép cặp đồng đội" : "Yêu cầu đánh cùng đồng đội...";

        String[] options = {
                "Sửa thông tin",
                reqOptionText,
                "Xem lịch sử các set của " + player.getName(),
                player.isResting() ? "Tiếp tục chơi" : "Xin tạm nghỉ",
                "Xóa người chơi"
        };

        new AlertDialog.Builder(this)
                .setTitle("Quản lý: " + player.getName() + " (" + player.getGender().getDisplayName() + ")")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditPlayerDialog(player);
                    } else if (which == 1) {
                        if (player.getPreferredTeammateId() != null) {
                            Player partner = getPlayerById(player.getPreferredTeammateId());
                            if (partner != null) partner.setPreferredTeammateId(null);
                            player.setPreferredTeammateId(null);
                            savePlayersToPrefs();
                            playerAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "Đã hủy yêu cầu ghép cặp.", Toast.LENGTH_SHORT).show();
                        } else {
                            showSelectPartnerDialog(player);
                        }
                    } else if (which == 2) {
                        showPlayerSetHistoryDialog(player);
                    } else if (which == 3) {
                        player.setResting(!player.isResting());
                        savePlayersToPrefs();
                        playerAdapter.notifyDataSetChanged();
                        Toast.makeText(this, player.getName() + (player.isResting() ? " đã tạm nghỉ." : " đã tiếp tục chơi."), Toast.LENGTH_SHORT).show();
                    } else if (which == 4) {
                        new AlertDialog.Builder(this)
                                .setTitle("Xác nhận xóa")
                                .setMessage("Bạn có chắc chắn muốn xóa " + player.getName() + " khỏi danh sách?")
                                .setPositiveButton("Xóa", (d, w) -> {
                                    playerList.remove(position);
                                    savePlayersToPrefs();
                                    playerAdapter.notifyDataSetChanged();
                                    Toast.makeText(this, "Đã xóa thành công.", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }
                })
                .show();
    }

    private void showSelectPartnerDialog(Player player) {
        List<Player> otherPlayers = new ArrayList<>();
        List<String> names = new ArrayList<>();

        for (Player p : playerList) {
            if (!p.getId().equals(player.getId())) {
                otherPlayers.add(p);
                names.add(p.getName() + " (" + p.getGender().getDisplayName() + ")");
            }
        }

        if (otherPlayers.isEmpty()) {
            Toast.makeText(this, "Chưa có người chơi khác để ghép cặp!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn đồng đội muốn ghép cùng " + player.getName())
                .setItems(names.toArray(new String[0]), (dialog, which) -> {
                    Player partner = otherPlayers.get(which);
                    player.setPreferredTeammateId(partner.getId());
                    partner.setPreferredTeammateId(player.getId());
                    savePlayersToPrefs();
                    playerAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Đã ghi nhận yêu cầu ghép cặp: " + player.getName() + " & " + partner.getName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showPlayerSetHistoryDialog(Player player) {
        List<HistoryManager.SetRecord> playerSets = historyManager.getSetRecordsForPlayer(player.getId());
        if (playerSets.isEmpty()) {
            Toast.makeText(this, player.getName() + " chưa tham gia set đấu nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Đã tham gia: ").append(playerSets.size()).append("/6 set\n\n");
        for (HistoryManager.SetRecord record : playerSets) {
            sb.append(record.getFormattedText()).append("\n-------------------------------------\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Lịch sử Set: " + player.getName())
                .setMessage(sb.toString().trim())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showEditPlayerDialog(Player player) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        TextView tvNameLabel = new TextView(this);
        tvNameLabel.setText("Tên người chơi:");
        layout.addView(tvNameLabel);

        EditText etName = new EditText(this);
        etName.setText(player.getName());
        layout.addView(etName);

        TextView tvGenderLabel = new TextView(this);
        tvGenderLabel.setText("Giới tính:");
        tvGenderLabel.setPadding(0, 20, 0, 0);
        layout.addView(tvGenderLabel);

        Spinner spGender = new Spinner(this);
        ArrayAdapter<Gender> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Gender.values());
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);
        spGender.setSelection(player.getGender().ordinal());
        layout.addView(spGender);

        TextView tvSkillLabel = new TextView(this);
        tvSkillLabel.setText("Trình độ:");
        tvSkillLabel.setPadding(0, 20, 0, 0);
        layout.addView(tvSkillLabel);

        Spinner spSkill = new Spinner(this);
        ArrayAdapter<PlayerSkill> skillAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, PlayerSkill.values());
        skillAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSkill.setAdapter(skillAdapter);
        spSkill.setSelection(player.getSkill().ordinal());
        layout.addView(spSkill);

        TextView tvMatchesLabel = new TextView(this);
        tvMatchesLabel.setText("Số set đã đánh:");
        tvMatchesLabel.setPadding(0, 20, 0, 0);
        layout.addView(tvMatchesLabel);

        EditText etMatches = new EditText(this);
        etMatches.setInputType(InputType.TYPE_CLASS_NUMBER);
        etMatches.setText(String.valueOf(player.getMatchesPlayed()));
        layout.addView(etMatches);

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa thông tin")
                .setView(layout)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Tên không được để trống!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int matches = player.getMatchesPlayed();
                    try {
                        matches = Integer.parseInt(etMatches.getText().toString().trim());
                    } catch (NumberFormatException ignored) {}

                    player.setName(newName);
                    player.setGender((Gender) spGender.getSelectedItem());
                    player.setSkill((PlayerSkill) spSkill.getSelectedItem());
                    player.setMatchesPlayed(matches);

                    savePlayersToPrefs();
                    playerAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Đã cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}