package com.example.hostbadminton;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseStatsActivity extends AppCompatActivity {

    private EditText etCourtFee, etShuttleFee, etOtherFee, etExpenseNote;
    private TextView tvTotalExpense, tvPlayerCountSummary, tvCostPerPlayer;
    private TextView tvStatsOverview, tvTopPlayers;
    private Button btnSaveAndCalculate, btnShareExpenseReport;

    private DatabaseHelper dbHelper;
    private List<Player> playerList;
    private List<HistoryManager.SetRecord> historyRecords;
    private ExpenseData currentExpense;

    private static final DecimalFormat currencyFormatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_stats);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi Phí & Thống Kê Buổi Đánh");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        playerList = dbHelper.loadPlayers();
        historyRecords = dbHelper.loadHistory(playerList);
        currentExpense = dbHelper.loadExpense();

        etCourtFee = findViewById(R.id.etCourtFee);
        etShuttleFee = findViewById(R.id.etShuttleFee);
        etOtherFee = findViewById(R.id.etOtherFee);
        etExpenseNote = findViewById(R.id.etExpenseNote);

        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvPlayerCountSummary = findViewById(R.id.tvPlayerCountSummary);
        tvCostPerPlayer = findViewById(R.id.tvCostPerPlayer);

        tvStatsOverview = findViewById(R.id.tvStatsOverview);
        tvTopPlayers = findViewById(R.id.tvTopPlayers);

        btnSaveAndCalculate = findViewById(R.id.btnSaveAndCalculate);
        btnShareExpenseReport = findViewById(R.id.btnShareExpenseReport);

        if (currentExpense.getCourtFee() > 0) etCourtFee.setText(String.valueOf((long) currentExpense.getCourtFee()));
        if (currentExpense.getShuttleFee() > 0) etShuttleFee.setText(String.valueOf((long) currentExpense.getShuttleFee()));
        if (currentExpense.getOtherFee() > 0) etOtherFee.setText(String.valueOf((long) currentExpense.getOtherFee()));
        if (!currentExpense.getNote().isEmpty()) etExpenseNote.setText(currentExpense.getNote());

        calculateAndDisplayExpenses();
        calculateAndDisplayStats();

        btnSaveAndCalculate.setOnClickListener(v -> {
            saveExpensesFromInput();
            calculateAndDisplayExpenses();
            Toast.makeText(this, "Đã lưu và tính toán chi phí thành công!", Toast.LENGTH_SHORT).show();
        });

        btnShareExpenseReport.setOnClickListener(v -> {
            saveExpensesFromInput();
            String reportText = generateExpenseReportText();

            String[] options = {"Chia sẻ qua ứng dụng (Zalo, Messenger...)", "Sao chép vào khay nhớ tạm"};
            new AlertDialog.Builder(this)
                    .setTitle("Xuất Báo Cáo Thu Tiền Sân")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, reportText);
                            startActivity(Intent.createChooser(shareIntent, "Chia sẻ báo cáo thu tiền:"));
                        } else {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Báo Cáo Chi Phí Cầu Lông", reportText);
                            if (clipboard != null) {
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(this, "Đã sao chép báo cáo vào bộ nhớ tạm!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
        });
    }

    private void saveExpensesFromInput() {
        double court = parseDoubleSafe(etCourtFee.getText().toString());
        double shuttle = parseDoubleSafe(etShuttleFee.getText().toString());
        double other = parseDoubleSafe(etOtherFee.getText().toString());
        String note = etExpenseNote.getText().toString().trim();

        currentExpense = new ExpenseData(court, shuttle, other, note);
        dbHelper.saveExpense(court, shuttle, other, note);
    }

    private void calculateAndDisplayExpenses() {
        double total = currentExpense.getTotalExpense();
        int playerCount = playerList.size();
        double perPlayer = playerCount > 0 ? (total / playerCount) : 0;

        tvTotalExpense.setText("Tổng chi phí: " + formatMoney(total) + " VNĐ");
        tvPlayerCountSummary.setText("Số người tham gia chia: " + playerCount + " người");
        tvCostPerPlayer.setText("Mỗi người cần đóng: " + formatMoney(perPlayer) + " VNĐ");
    }

    private void calculateAndDisplayStats() {
        int totalPlayers = playerList.size();
        int maleCount = 0;
        int femaleCount = 0;

        Map<String, Integer> playerSetsMap = new HashMap<>();
        Map<String, Integer> playerWinsMap = new HashMap<>();

        for (Player p : playerList) {
            if (p.getGender() == Gender.MALE) maleCount++;
            else femaleCount++;
            playerSetsMap.put(p.getName(), p.getMatchesPlayed());
        }

        int totalSets = historyRecords.size();
        int scoredSetsCount = 0;

        for (HistoryManager.SetRecord rec : historyRecords) {
            if (rec.getScore1() != null && rec.getScore2() != null) {
                scoredSetsCount++;
                int s1 = rec.getScore1();
                int s2 = rec.getScore2();
                if (s1 > s2) {
                    incrementWin(playerWinsMap, rec.getP1().getName());
                    incrementWin(playerWinsMap, rec.getP2().getName());
                } else if (s2 > s1) {
                    incrementWin(playerWinsMap, rec.getP3().getName());
                    incrementWin(playerWinsMap, rec.getP4().getName());
                }
            }
        }

        StringBuilder statsSb = new StringBuilder();
        statsSb.append("• Tổng số cầu thủ: ").append(totalPlayers).append(" người (Nam: ").append(maleCount).append(", Nữ: ").append(femaleCount).append(")\n");
        statsSb.append("• Tổng số set đấu đã xếp: ").append(totalSets).append(" set\n");
        statsSb.append("• Số set đã hoàn thành (nhập tỉ số): ").append(scoredSetsCount).append("/").append(totalSets).append(" set");

        tvStatsOverview.setText(statsSb.toString());

        String topActivePlayer = "Chưa có dữ liệu";
        int maxSets = 0;
        for (Map.Entry<String, Integer> entry : playerSetsMap.entrySet()) {
            if (entry.getValue() > maxSets) {
                maxSets = entry.getValue();
                topActivePlayer = entry.getKey() + " (" + maxSets + " set)";
            }
        }

        String topWinner = "Chưa cập nhật tỉ số";
        int maxWins = 0;
        for (Map.Entry<String, Integer> entry : playerWinsMap.entrySet()) {
            if (entry.getValue() > maxWins) {
                maxWins = entry.getValue();
                topWinner = entry.getKey() + " (" + maxWins + " trận thắng)";
            }
        }

        StringBuilder topSb = new StringBuilder();
        topSb.append("🔥 Tích cực nhất: ").append(topActivePlayer).append("\n");
        topSb.append("🏆 Thắng nhiều nhất: ").append(topWinner);

        tvTopPlayers.setText(topSb.toString());
    }

    private void incrementWin(Map<String, Integer> winsMap, String playerName) {
        winsMap.put(playerName, winsMap.getOrDefault(playerName, 0) + 1);
    }

    private String generateExpenseReportText() {
        double total = currentExpense.getTotalExpense();
        int playerCount = playerList.size();
        double perPlayer = playerCount > 0 ? (total / playerCount) : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("📢 BÁO CÁO THU TIỀN SÂN CẦU LÔNG 📢\n");
        sb.append("------------------------------------\n");
        if (currentExpense.getCourtFee() > 0) {
            sb.append("• Tiền sân: ").append(formatMoney(currentExpense.getCourtFee())).append(" VNĐ\n");
        }
        if (currentExpense.getShuttleFee() > 0) {
            sb.append("• Tiền cầu: ").append(formatMoney(currentExpense.getShuttleFee())).append(" VNĐ\n");
        }
        if (currentExpense.getOtherFee() > 0) {
            sb.append("• Chi phí khác: ").append(formatMoney(currentExpense.getOtherFee())).append(" VNĐ\n");
        }
        sb.append("------------------------------------\n");
        sb.append("👉 TỔNG CHI PHÍ: ").append(formatMoney(total)).append(" VNĐ\n");
        sb.append("👥 Tổng số người tham gia: ").append(playerCount).append(" người\n");
        sb.append("💵 MỖI NGƯỜI CẦN ĐÓNG: ").append(formatMoney(perPlayer)).append(" VNĐ\n");

        if (!currentExpense.getNote().isEmpty()) {
            sb.append("------------------------------------\n");
            sb.append("💳 Thông tin chuyển khoản / Ghi chú:\n");
            sb.append(currentExpense.getNote()).append("\n");
        }

        sb.append("\nCảm ơn mọi người đã tham gia buổi đánh!");
        return sb.toString();
    }

    private double parseDoubleSafe(String val) {
        if (val == null || val.trim().isEmpty()) return 0;
        try {
            return Double.parseDouble(val.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatMoney(double amount) {
        return currencyFormatter.format(amount);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}