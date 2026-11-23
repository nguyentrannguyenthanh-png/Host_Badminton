package com.example.hostbadminton;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import androidx.core.content.FileProvider;

public class MainActivity extends AppCompatActivity {

    private EditText etPlayerName;
    private Spinner spinnerGender, spinnerSkill;
    private Button btnAddPlayer, btnSuggestMatch, btnManualMatch, btnUndoMatch, btnViewHistory, btnReset, btnLogout, btnReload;
    private TextView tvMatchResult;
    private LinearLayout layoutPlayerListContainer;

    // Các thành phần Tab Bottom Navigation
    private View layoutHome, layoutExpenseStats, layoutMembers, layoutProfile;
    private LinearLayout btnTabHome, btnTabExpense, btnTabMembers, btnTabProfile;
    private TextView tvTabHomeText, tvTabExpenseText;

    // Các thành phần màn hình Chi Phí & Thống Kê
    private EditText etCourtFee, etShuttleFee, etOtherFee, etExpenseNote, etFixedFeePerPlayer, etMaleFee, etFemaleFee, etShuttlesUsed;
    private CheckBox cbFixedFeeMode, cbSeparateGenderFee, cbUseInventoryShuttle;
    private LinearLayout layoutFixedFeeInput, layoutSeparateGenderInput, layoutInventoryShuttleCalc;
    private TextView tvTotalExpense, tvPlayerCountSummary, tvCostPerPlayer, tvGenderBreakdown, tvHostBalance, tvStockStatus;
    private TextView tvStatsOverview, tvTopPlayers;
    private Button btnSaveAndCalculate, btnShareExpenseReport, btnSaveSessionReport, btnViewRevenueReport, btnManageShuttleStock, btnSelectQrCode;
    private ImageView ivQrCodePreview;
    private Uri qrCodeUri = null;
    private static final int PICK_QR_IMAGE_REQUEST = 1001;

    private ExpenseData currentExpense;
    private DatabaseHelper.ShuttleStockData currentShuttleStock;

    private List<Player> playerList = new ArrayList<>();
    private HistoryManager historyManager = new HistoryManager();
    private DatabaseHelper dbHelper;
    private List<HistoryManager.SetRecord> currentRoundRecords = new ArrayList<>();

    private static final DecimalFormat currencyFormatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ View Tab
        layoutHome = findViewById(R.id.layoutHome);
        layoutExpenseStats = findViewById(R.id.layoutExpenseStats);
        layoutMembers = findViewById(R.id.layoutMembers);
        layoutProfile = findViewById(R.id.layoutProfile);
        btnTabMembers = findViewById(R.id.btnTabMembers);
        btnTabProfile = findViewById(R.id.btnTabProfile);
        btnTabHome = findViewById(R.id.btnTabHome);
        btnTabExpense = findViewById(R.id.btnTabExpense);
        tvTabHomeText = findViewById(R.id.tvTabHomeText);
        tvTabExpenseText = findViewById(R.id.tvTabExpenseText);

        // Ánh xạ View Trang Chủ
        etPlayerName = findViewById(R.id.etPlayerName);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerSkill = findViewById(R.id.spinnerSkill);
        btnAddPlayer = findViewById(R.id.btnAddPlayer);
        btnSuggestMatch = findViewById(R.id.btnSuggestMatch);
        btnManualMatch = findViewById(R.id.btnManualMatch);
        btnUndoMatch = findViewById(R.id.btnUndoMatch);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnReset = findViewById(R.id.btnReset);
        btnLogout = findViewById(R.id.btnLogout);
        btnReload = findViewById(R.id.btnReload);
        tvMatchResult = findViewById(R.id.tvMatchResult);
        layoutPlayerListContainer = findViewById(R.id.layoutPlayerListContainer);

        // Ánh xạ View Chi Phí & Thống Kê
        etCourtFee = findViewById(R.id.etCourtFee);
        etShuttleFee = findViewById(R.id.etShuttleFee);
        etOtherFee = findViewById(R.id.etOtherFee);
        etExpenseNote = findViewById(R.id.etExpenseNote);
        btnSelectQrCode = findViewById(R.id.btnSelectQrCode);
        ivQrCodePreview = findViewById(R.id.ivQrCodePreview);

        String savedUriStr = getSharedPreferences("HostBadmintonPrefs", MODE_PRIVATE).getString("qr_code_uri", null);
        if (savedUriStr != null) {
            try {
                qrCodeUri = Uri.parse(savedUriStr);
                if (ivQrCodePreview != null) {
                    ivQrCodePreview.setImageURI(qrCodeUri);
                    ivQrCodePreview.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {}
        }

        if (btnSelectQrCode != null) {
            btnSelectQrCode.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_QR_IMAGE_REQUEST);
            });
        }
        cbFixedFeeMode = findViewById(R.id.cbFixedFeeMode);
        layoutFixedFeeInput = findViewById(R.id.layoutFixedFeeInput);
        etFixedFeePerPlayer = findViewById(R.id.etFixedFeePerPlayer);

        cbSeparateGenderFee = findViewById(R.id.cbSeparateGenderFee);
        layoutSeparateGenderInput = findViewById(R.id.layoutSeparateGenderInput);
        etMaleFee = findViewById(R.id.etMaleFee);
        etFemaleFee = findViewById(R.id.etFemaleFee);

        btnManageShuttleStock = findViewById(R.id.btnManageShuttleStock);
        cbUseInventoryShuttle = findViewById(R.id.cbUseInventoryShuttle);
        layoutInventoryShuttleCalc = findViewById(R.id.layoutInventoryShuttleCalc);
        tvStockStatus = findViewById(R.id.tvStockStatus);
        etShuttlesUsed = findViewById(R.id.etShuttlesUsed);

        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvPlayerCountSummary = findViewById(R.id.tvPlayerCountSummary);
        tvCostPerPlayer = findViewById(R.id.tvCostPerPlayer);
        tvGenderBreakdown = findViewById(R.id.tvGenderBreakdown);
        tvHostBalance = findViewById(R.id.tvHostBalance);
        tvStatsOverview = findViewById(R.id.tvStatsOverview);
        tvTopPlayers = findViewById(R.id.tvTopPlayers);
        btnSaveAndCalculate = findViewById(R.id.btnSaveAndCalculate);
        btnShareExpenseReport = findViewById(R.id.btnShareExpenseReport);
        btnSaveSessionReport = findViewById(R.id.btnSaveSessionReport);
        btnViewRevenueReport = findViewById(R.id.btnViewRevenueReport);

        // Nạp dữ liệu lưu trữ vĩnh viễn (Database)
        loadPlayersFromPrefs();
        loadExpensesData();

        // Sự kiện chuyển Tab Bottom Navigation
        btnTabHome.setOnClickListener(v -> switchTab(true));
        btnTabExpense.setOnClickListener(v -> switchTab(false));

        // Sự kiện Quản lý Nhập Kho Cầu (Thêm / Sửa / Xóa)
        if (btnManageShuttleStock != null) {
            btnManageShuttleStock.setOnClickListener(v -> showManageShuttleStockDialog());
        }

        // Sự kiện tích chọn Dùng cầu trong kho
        if (cbUseInventoryShuttle != null) {
            cbUseInventoryShuttle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (layoutInventoryShuttleCalc != null) {
                    layoutInventoryShuttleCalc.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
                etShuttleFee.setEnabled(!isChecked);
                updateShuttleFeeFromUsage();
            });
        }

        if (etShuttlesUsed != null) {
            etShuttlesUsed.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateShuttleFeeFromUsage();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Sự kiện tích chọn Mức thu cố định chung
        if (cbFixedFeeMode != null) {
            cbFixedFeeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && cbSeparateGenderFee != null) cbSeparateGenderFee.setChecked(false);
                if (layoutFixedFeeInput != null) layoutFixedFeeInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                calculateAndDisplayExpenses();
            });
        }

        // Sự kiện tích chọn Thu phí riêng Nam/Nữ
        if (cbSeparateGenderFee != null) {
            cbSeparateGenderFee.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && cbFixedFeeMode != null) cbFixedFeeMode.setChecked(false);
                if (layoutSeparateGenderInput != null) layoutSeparateGenderInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                calculateAndDisplayExpenses();
            });
        }

        // Sự kiện Chốt & Lưu Doanh Thu Buổi Đánh
        if (btnSaveSessionReport != null) {
            btnSaveSessionReport.setOnClickListener(v -> {
                saveExpensesFromInput();
                double totalExp = currentExpense != null ? currentExpense.getTotalExpense() : 0;
                double totalColl = calculateTotalCollected();

                int maleCount = 0, femaleCount = 0;
                for (Player p : playerList) {
                    if (p.getGender() == Gender.MALE) maleCount++;
                    else femaleCount++;
                }

                dbHelper.saveSessionReport(totalExp, totalColl, playerList.size(), maleCount, femaleCount);

                // Trừ số trái cầu sử dụng khỏi kho
                if (cbUseInventoryShuttle != null && cbUseInventoryShuttle.isChecked()) {
                    int used = (int) parseDoubleSafe(etShuttlesUsed.getText().toString());
                    if (used > 0) {
                        dbHelper.deductShuttleStock(used);
                        loadExpensesData();
                    }
                }

                showTopToast("Đã chốt và lưu doanh thu buổi đánh thành công!");
            });
        }

        // Sự kiện Xem Báo Cáo Doanh Thu (Ngày / Tháng / Năm)
        if (btnViewRevenueReport != null) {
            btnViewRevenueReport.setOnClickListener(v -> showRevenueReportDialog());
        }

        // Sự kiện Xếp Trận Thủ Công
        btnManualMatch.setOnClickListener(v -> showManualMatchDialog());

        // Sự kiện Hủy Trận Vừa Xếp (Undo)
        btnUndoMatch.setOnClickListener(v -> {
            if (currentRoundRecords.isEmpty()) {
                showTopToast("Không có trận nào vừa được xếp để hủy!");
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Xác Nhận Hủy Trận Vừa Xếp")
                    .setMessage("Bạn có chắc chắn muốn hủy (reset) các trận đấu vừa được xếp gần nhất này? Số set đã đánh của các cầu thủ sẽ được hoàn lại.")
                    .setPositiveButton("Hủy Trận", (dialog, which) -> {
                        for (HistoryManager.SetRecord rec : currentRoundRecords) {
                            rec.getP1().setMatchesPlayed(Math.max(0, rec.getP1().getMatchesPlayed() - 1));
                            rec.getP2().setMatchesPlayed(Math.max(0, rec.getP2().getMatchesPlayed() - 1));
                            rec.getP3().setMatchesPlayed(Math.max(0, rec.getP3().getMatchesPlayed() - 1));
                            rec.getP4().setMatchesPlayed(Math.max(0, rec.getP4().getMatchesPlayed() - 1));

                            historyManager.removeSetRecord(rec);
                        }
                        currentRoundRecords.clear();
                        tvMatchResult.setText("Trận đấu vừa xếp đã bị hủy/reset.");
                        savePlayersToPrefs();
                        updatePlayerListUI();
                        showTopToast("Đã reset/hủy trận vừa xếp thành công!");
                    })
                    .setNegativeButton("Không", null)
                    .show();
        });

        // Sự kiện Đăng Xuất
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác Nhận Đăng Xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất tài khoản chủ sân?")
                    .setPositiveButton("Đăng Xuất", (dialog, which) -> {
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

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

        // Cập nhật danh sách cầu thủ hiển thị động
        updatePlayerListUI();

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
            updatePlayerListUI();

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

        // Sự kiện Làm Mới
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
                            dbHelper.savePlayers(playerList);
                            dbHelper.saveHistory(historyManager.getAllSetRecords());
                            tvMatchResult.setText("Đã reset các set về 0! Giữ nguyên danh sách.");
                            updatePlayerListUI();
                            showTopToast("Đã reset các set về 0!");
                        } else if (which == 1) {
                            playerList.clear();
                            historyManager.clearHistory();
                            currentRoundRecords.clear();
                            dbHelper.clearAllData();
                            tvMatchResult.setText("Trận đấu tiếp theo sẽ hiển thị ở đây...");
                            updatePlayerListUI();
                            showTopToast("Đã xóa toàn bộ người chơi!");
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
                updatePlayerListUI();

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

        // Xử lý Sự kiện Lưu & Tính Tiền trong Tab Chi Phí
        if (btnSaveAndCalculate != null) {
            btnSaveAndCalculate.setOnClickListener(v -> {
                saveExpensesFromInput();
                calculateAndDisplayExpenses();
                Toast.makeText(this, "Đã lưu và tính toán chi phí thành công!", Toast.LENGTH_SHORT).show();
            });
        }

        // Xử lý Sự kiện Chia Sẻ Báo Cáo Zalo trong Tab Chi Phí
        if (btnShareExpenseReport != null) {
            btnShareExpenseReport.setOnClickListener(v -> {
                saveExpensesFromInput();
                String reportText = generateExpenseReportText();

                String[] options = {"Chia sẻ qua ứng dụng (Zalo, Messenger...)", "Sao chép vào khay nhớ tạm"};
                new AlertDialog.Builder(this)
                        .setTitle("Xuất Báo Cáo Thu Tiền Sân")
                        .setItems(options, (dialog, which) -> {
                            if (which == 0) {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Báo Cáo Chi Phí Cầu Lông", reportText);
                                if (clipboard != null) {
                                    clipboard.setPrimaryClip(clip);
                                }
                                
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                if (qrCodeUri != null) {
                                    try {
                                        // Create a temporary file in cache to share securely
                                        File cachePath = new File(getCacheDir(), "images");
                                        cachePath.mkdirs(); // don't forget to make the directory
                                        File tempImage = new File(cachePath, "qr_code.png");
                                        
                                        InputStream in = getContentResolver().openInputStream(qrCodeUri);
                                        OutputStream out = new FileOutputStream(tempImage);
                                        byte[] buf = new byte[1024];
                                        int len;
                                        while ((len = in.read(buf)) > 0) {
                                            out.write(buf, 0, len);
                                        }
                                        out.close();
                                        in.close();
                                        
                                        // Use FileProvider to get a shareable URI
                                        Uri contentUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", tempImage);
                                        
                                        shareIntent.setType("image/*");
                                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, reportText);
                                        // CRITICAL FOR ANDROID 11+ (API 30+): Explicitly setting ClipData with the content URI
                                        // We use Intent.ACTION_SEND to send both image and text.
                                        // NOTE: Messenger ignores EXTRA_TEXT if type is image/*. It forces users to paste text.
                                        // To ensure the user can paste easily, we auto-copy text to clipboard.
                                        ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData cd = ClipData.newPlainText("Báo Cáo", reportText);
                                        if (cb != null) {
                                            cb.setPrimaryClip(cd);
                                        }
                                        
                                        shareIntent.setClipData(ClipData.newRawUri("QR Code", contentUri));
                                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        
                                        Toast.makeText(MainActivity.this, "Đã tự động sao chép văn bản. Vui lòng DÁN (Paste) để gửi kèm ảnh!", Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        shareIntent.setType("text/plain");
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, reportText);
                                        Toast.makeText(MainActivity.this, "Lỗi lấy ảnh QR, chỉ gửi chữ.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, reportText);
                                }
                                
                                Intent chooserIntent = Intent.createChooser(shareIntent, "Chia sẻ báo cáo thu tiền:");
                                // CRITICAL FOR ANDROID 11+ (API 30+): Grant permission on the chooser intent as well
                                chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(chooserIntent);
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
    }

    // --- CẬP NHẬT GIAO DIỆN DANH SÁCH CẦU THỦ ĐỘNG SIÊU MƯỢT ---
    private void updatePlayerListUI() {
        if (layoutPlayerListContainer == null) return;
        layoutPlayerListContainer.removeAllViews();

        for (int i = 0; i < playerList.size(); i++) {
            Player player = playerList.get(i);
            int position = i;

            View view = getLayoutInflater().inflate(R.layout.item_player, layoutPlayerListContainer, false);
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
                String avoidText = "";
                if (player.getAvoidPlayerId() != null) {
                    Player avoidP = getPlayerById(player.getAvoidPlayerId());
                    if (avoidP != null) {
                        avoidText = " [Né: " + avoidP.getName() + "]";
                    }
                }
                String styleText = player.getPlayStyle() != PlayStyle.BALANCED ? " [" + player.getPlayStyle().getDisplayName() + "]" : "";
                String discountText = player.getDiscountAmount() > 0 ? " [Giảm " + formatMoney(player.getDiscountAmount()) + " VNĐ]" : "";
                int stt = position + 1;
                tvInfo.setText(stt + ". " + player.getName() + " (" + player.getGender().getDisplayName() + " - " + player.getSkill().getDisplayName() + ")" + prefText + avoidText + styleText + discountText + statusText);
                tvCount.setText(player.getMatchesPlayed() + "/6 set");
            }

            view.setOnClickListener(v -> showPlayerOptionsDialog(player, position));
            layoutPlayerListContainer.addView(view);
        }
    }

    // --- DIALOG THIẾT LẬP NÉ NGƯỜI CHƠI ---
    private void showAvoidPlayerDialog(Player player) {
        List<Player> otherPlayers = new ArrayList<>();
        List<String> names = new ArrayList<>();

        for (Player p : playerList) {
            if (!p.getId().equals(player.getId())) {
                otherPlayers.add(p);
                names.add(p.getName() + " (" + p.getGender().getDisplayName() + ")");
            }
        }

        if (otherPlayers.isEmpty()) {
            Toast.makeText(this, "Chưa có người chơi khác!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn người KHÔNG muốn xếp cùng " + player.getName())
                .setItems(names.toArray(new String[0]), (dialog, which) -> {
                    Player avoidPartner = otherPlayers.get(which);
                    player.setAvoidPlayerId(avoidPartner.getId());
                    savePlayersToPrefs();
                    updatePlayerListUI();
                    Toast.makeText(this, "Đã ghi nhận né: " + player.getName() + " & " + avoidPartner.getName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- DIALOG CHỌN CHẾ ĐỘ CHƠI CĂNG / CHILL ---
    private void showPlayStyleDialog(Player player) {
        String[] styles = {
                "1. Bình thường (Xoay vòng cân bằng)",
                "2. Chill (Ưu tiên chơi cùng/dưới trình)",
                "3. Căng (Ưu tiên chơi cùng/hơn trình)"
        };

        new AlertDialog.Builder(this)
                .setTitle("⚡ Chế độ chơi: " + player.getName())
                .setItems(styles, (dialog, which) -> {
                    if (which == 0) player.setPlayStyle(PlayStyle.BALANCED);
                    else if (which == 1) player.setPlayStyle(PlayStyle.CHILL);
                    else if (which == 2) player.setPlayStyle(PlayStyle.INTENSE);

                    savePlayersToPrefs();
                    updatePlayerListUI();
                    Toast.makeText(this, "Đã chuyển chế độ: " + player.getPlayStyle().getDisplayName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- DIALOG THIẾT LẬP GIẢM GIÁ CHO NGƯỜI CHƠI ---
    private void showDiscountDialog(Player player) {
        String[] options = {
                "1. Miễn phí hoàn toàn (100%)",
                "2. Giảm 10.000 VNĐ",
                "3. Giảm 20.000 VNĐ",
                "4. Giảm 50.000 VNĐ",
                "5. Nhập mức giảm giá tùy chỉnh...",
                "6. Bỏ giảm giá (Thu 100%)"
        };

        new AlertDialog.Builder(this)
                .setTitle("🏷️ Giảm Giá Cho: " + player.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        double baseFee = getBaseFeeForPlayer(player);
                        player.setDiscountAmount(baseFee > 0 ? baseFee : 1000000);
                        savePlayersToPrefs();
                        updatePlayerListUI();
                        calculateAndDisplayExpenses();
                        Toast.makeText(this, "Đã miễn phí cho " + player.getName(), Toast.LENGTH_SHORT).show();
                    } else if (which == 1) {
                        player.setDiscountAmount(10000);
                        savePlayersToPrefs();
                        updatePlayerListUI();
                        calculateAndDisplayExpenses();
                        Toast.makeText(this, "Đã giảm 10.000 VNĐ cho " + player.getName(), Toast.LENGTH_SHORT).show();
                    } else if (which == 2) {
                        player.setDiscountAmount(20000);
                        savePlayersToPrefs();
                        updatePlayerListUI();
                        calculateAndDisplayExpenses();
                        Toast.makeText(this, "Đã giảm 20.000 VNĐ cho " + player.getName(), Toast.LENGTH_SHORT).show();
                    } else if (which == 3) {
                        player.setDiscountAmount(50000);
                        savePlayersToPrefs();
                        updatePlayerListUI();
                        calculateAndDisplayExpenses();
                        Toast.makeText(this, "Đã giảm 50.000 VNĐ cho " + player.getName(), Toast.LENGTH_SHORT).show();
                    } else if (which == 4) {
                        showCustomDiscountInputDialog(player);
                    } else if (which == 5) {
                        player.setDiscountAmount(0);
                        savePlayersToPrefs();
                        updatePlayerListUI();
                        calculateAndDisplayExpenses();
                        Toast.makeText(this, "Đã bỏ giảm giá cho " + player.getName(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showCustomDiscountInputDialog(Player player) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView tvInfo = new TextView(this);
        tvInfo.setText("Nhập số tiền giảm cho " + player.getName() + " (VNĐ):");
        tvInfo.setTextColor(Color.BLACK);
        layout.addView(tvInfo);

        EditText etDiscount = new EditText(this);
        etDiscount.setHint("Ví dụ: 30000");
        etDiscount.setInputType(InputType.TYPE_CLASS_NUMBER);
        etDiscount.setTextColor(Color.BLACK);
        if (player.getDiscountAmount() > 0) {
            etDiscount.setText(String.valueOf((long) player.getDiscountAmount()));
        }
        layout.addView(etDiscount);

        new AlertDialog.Builder(this)
                .setTitle("Tùy Chỉnh Giảm Giá")
                .setView(layout)
                .setPositiveButton("Áp Dụng", (dialog, which) -> {
                    double disc = parseDoubleSafe(etDiscount.getText().toString());
                    player.setDiscountAmount(disc);
                    savePlayersToPrefs();
                    updatePlayerListUI();
                    calculateAndDisplayExpenses();
                    Toast.makeText(this, "Đã cập nhật giảm giá cho " + player.getName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private double getBaseFeeForPlayer(Player player) {
        if (currentExpense == null) return 0;
        int playerCount = playerList.size();
        boolean isFixed = cbFixedFeeMode != null && cbFixedFeeMode.isChecked();
        boolean isSepGender = cbSeparateGenderFee != null && cbSeparateGenderFee.isChecked();

        if (isSepGender) {
            double maleFee = etMaleFee != null ? parseDoubleSafe(etMaleFee.getText().toString()) : 0;
            double femaleFee = etFemaleFee != null ? parseDoubleSafe(etFemaleFee.getText().toString()) : 0;
            return player.getGender() == Gender.MALE ? maleFee : femaleFee;
        } else if (isFixed) {
            return etFixedFeePerPlayer != null ? parseDoubleSafe(etFixedFeePerPlayer.getText().toString()) : 0;
        } else {
            return playerCount > 0 ? (currentExpense.getTotalExpense() / playerCount) : 0;
        }
    }

    // --- DIALOG NHẬP KHO CẦU ---
    private void showManageShuttleStockDialog() {
        List<DatabaseHelper.ShuttleStockData> stocks = dbHelper.getAllShuttleStocks();
        List<String> displayItems = new ArrayList<>();

        displayItems.add("➕ Nhập Thêm Đợt Cầu Mới Vào Kho...");

        for (DatabaseHelper.ShuttleStockData s : stocks) {
            displayItems.add("📦 " + s.brandName + ": Còn " + s.remainingShuttles + "/" + (s.tubeCount * s.shuttlesPerTube) + " trái (Giá: " + formatMoney(s.pricePerShuttle) + "đ/trái)");
        }

        new AlertDialog.Builder(this)
                .setTitle("📦 Quản Lý Kho Cầu")
                .setItems(displayItems.toArray(new String[0]), (dialog, which) -> {
                    if (which == 0) {
                        showAddShuttleStockDialog();
                    } else {
                        DatabaseHelper.ShuttleStockData selectedStock = stocks.get(which - 1);
                        showStockOptionsDialog(selectedStock);
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showStockOptionsDialog(DatabaseHelper.ShuttleStockData stock) {
        String[] options = {
                "✏️ Chỉnh Sửa Thông Tin (Tên, Số ống, Giá)",
                "🔄 Cập Nhật Số Trái Còn Lại",
                "🗑️ Xóa Đợt Cầu Này Khỏi Kho"
        };

        new AlertDialog.Builder(this)
                .setTitle("Quản lý: " + stock.brandName)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditShuttleStockDialog(stock);
                    } else if (which == 1) {
                        showUpdateRemainingShuttlesDialog(stock);
                    } else if (which == 2) {
                        new AlertDialog.Builder(this)
                                .setTitle("Xác Nhận Xóa")
                                .setMessage("Bạn có chắc chắn muốn xóa " + stock.brandName + " khỏi kho?")
                                .setPositiveButton("Xóa", (d, w) -> {
                                    dbHelper.deleteShuttleStock(stock.id);
                                    loadExpensesData();
                                    Toast.makeText(this, "Đã xóa đợt cầu thành công!", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAddShuttleStockDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText etBrand = new EditText(this);
        etBrand.setHint("Tên thương hiệu (VD: VinaStar, Ba Sao, Kumpoo...)");
        etBrand.setTextColor(Color.BLACK);

        EditText etTubeCount = new EditText(this);
        etTubeCount.setHint("Số lượng ống cầu mua (VD: 5)");
        etTubeCount.setInputType(InputType.TYPE_CLASS_NUMBER);
        etTubeCount.setTextColor(Color.BLACK);

        EditText etPrice = new EditText(this);
        etPrice.setHint("Tổng số tiền mua (VD: 1250000)");
        etPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
        etPrice.setTextColor(Color.BLACK);

        layout.addView(etBrand);
        layout.addView(etTubeCount);
        layout.addView(etPrice);

        new AlertDialog.Builder(this)
                .setTitle("➕ Nhập Kho Cầu Mới")
                .setView(layout)
                .setPositiveButton("Lưu Kho Cầu", (dialog, which) -> {
                    String brand = etBrand.getText().toString().trim();
                    int tube = (int) parseDoubleSafe(etTubeCount.getText().toString());
                    double price = parseDoubleSafe(etPrice.getText().toString());

                    if (brand.isEmpty() || tube <= 0 || price <= 0) {
                        Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin hợp lệ!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    dbHelper.addShuttleStock(brand, tube, 12, price);
                    loadExpensesData();
                    Toast.makeText(this, "Đã nhập thêm kho cầu thành công!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditShuttleStockDialog(DatabaseHelper.ShuttleStockData stock) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText etBrand = new EditText(this);
        etBrand.setHint("Tên thương hiệu cầu");
        etBrand.setText(stock.brandName);
        etBrand.setTextColor(Color.BLACK);

        EditText etTubeCount = new EditText(this);
        etTubeCount.setHint("Số lượng ống cầu mua");
        etTubeCount.setInputType(InputType.TYPE_CLASS_NUMBER);
        etTubeCount.setText(String.valueOf(stock.tubeCount));
        etTubeCount.setTextColor(Color.BLACK);

        EditText etPrice = new EditText(this);
        etPrice.setHint("Tổng số tiền mua");
        etPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
        etPrice.setText(String.valueOf((long) stock.totalPrice));
        etPrice.setTextColor(Color.BLACK);

        EditText etRemaining = new EditText(this);
        etRemaining.setHint("Số trái còn lại trong kho");
        etRemaining.setInputType(InputType.TYPE_CLASS_NUMBER);
        etRemaining.setText(String.valueOf(stock.remainingShuttles));
        etRemaining.setTextColor(Color.BLACK);

        layout.addView(etBrand);
        layout.addView(etTubeCount);
        layout.addView(etPrice);
        layout.addView(etRemaining);

        new AlertDialog.Builder(this)
                .setTitle("✏️ Chỉnh Sửa Kho Cầu")
                .setView(layout)
                .setPositiveButton("Lưu Cập Nhật", (dialog, which) -> {
                    String brand = etBrand.getText().toString().trim();
                    int tube = (int) parseDoubleSafe(etTubeCount.getText().toString());
                    double price = parseDoubleSafe(etPrice.getText().toString());
                    int remaining = (int) parseDoubleSafe(etRemaining.getText().toString());

                    dbHelper.updateShuttleStock(stock.id, brand, tube, 12, price, remaining);
                    loadExpensesData();
                    Toast.makeText(this, "Đã cập nhật kho cầu thành công!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showUpdateRemainingShuttlesDialog(DatabaseHelper.ShuttleStockData stock) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView tvInfo = new TextView(this);
        tvInfo.setText("Cập nhật số trái cầu còn lại cho " + stock.brandName + ":");
        tvInfo.setTextColor(Color.BLACK);
        layout.addView(tvInfo);

        EditText etRemaining = new EditText(this);
        etRemaining.setInputType(InputType.TYPE_CLASS_NUMBER);
        etRemaining.setText(String.valueOf(stock.remainingShuttles));
        etRemaining.setTextColor(Color.BLACK);
        layout.addView(etRemaining);

        new AlertDialog.Builder(this)
                .setTitle("🔄 Cập Nhật Số Trái Còn Lại")
                .setView(layout)
                .setPositiveButton("Cập Nhật", (dialog, which) -> {
                    int remaining = (int) parseDoubleSafe(etRemaining.getText().toString());
                    dbHelper.updateShuttleStock(stock.id, stock.brandName, stock.tubeCount, stock.shuttlesPerTube, stock.totalPrice, remaining);
                    loadExpensesData();
                    Toast.makeText(this, "Đã cập nhật số trái cầu tồn kho!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateShuttleFeeFromUsage() {
        if (cbUseInventoryShuttle != null && cbUseInventoryShuttle.isChecked()) {
            if (currentShuttleStock != null && currentShuttleStock.pricePerShuttle > 0) {
                int count = (int) parseDoubleSafe(etShuttlesUsed.getText().toString());
                double calculatedFee = count * currentShuttleStock.pricePerShuttle;
                etShuttleFee.setText(String.valueOf((long) calculatedFee));
            } else {
                tvStockStatus.setText("Kho cầu: Chưa nhập kho cầu nào!");
            }
        }
    }

    // --- TAB SWITCHING LOGIC (MÀU HỒNG SỮA & CHỮ ĐEN) ---
    private void switchTab(boolean isHomeSelected) {
        if (isHomeSelected) {
            layoutHome.setVisibility(View.VISIBLE);
            layoutExpenseStats.setVisibility(View.GONE);
            tvTabHomeText.setTextColor(Color.parseColor("#E65C82"));
            tvTabHomeText.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTabExpenseText.setTextColor(Color.parseColor("#757575"));
            tvTabExpenseText.setTypeface(null, android.graphics.Typeface.NORMAL);
        } else {
            layoutHome.setVisibility(View.GONE);
            layoutExpenseStats.setVisibility(View.VISIBLE);
            tvTabExpenseText.setTextColor(Color.parseColor("#E65C82"));
            tvTabExpenseText.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTabHomeText.setTextColor(Color.parseColor("#757575"));
            tvTabHomeText.setTypeface(null, android.graphics.Typeface.NORMAL);

            loadExpensesData();
            calculateAndDisplayExpenses();
            calculateAndDisplayStats();
        }
    }

    // --- LOGIC CHI PHÍ & THỐNG KÊ TRONG TAB ---
    private void loadExpensesData() {
        if (dbHelper != null) {
            currentExpense = dbHelper.loadExpense();
            currentShuttleStock = dbHelper.loadShuttleStock();

            if (etCourtFee != null && currentExpense.getCourtFee() > 0)
                etCourtFee.setText(String.valueOf((long) currentExpense.getCourtFee()));
            if (etShuttleFee != null && currentExpense.getShuttleFee() > 0)
                etShuttleFee.setText(String.valueOf((long) currentExpense.getShuttleFee()));
            if (etOtherFee != null && currentExpense.getOtherFee() > 0)
                etOtherFee.setText(String.valueOf((long) currentExpense.getOtherFee()));
            if (etExpenseNote != null && !currentExpense.getNote().isEmpty())
                etExpenseNote.setText(currentExpense.getNote());

            if (cbFixedFeeMode != null) {
                cbFixedFeeMode.setChecked(currentExpense.isFixedFeeMode());
                if (layoutFixedFeeInput != null) {
                    layoutFixedFeeInput.setVisibility(currentExpense.isFixedFeeMode() ? View.VISIBLE : View.GONE);
                }
            }
            if (etFixedFeePerPlayer != null && currentExpense.getFixedFeePerPlayer() > 0) {
                etFixedFeePerPlayer.setText(String.valueOf((long) currentExpense.getFixedFeePerPlayer()));
            }

            if (cbSeparateGenderFee != null) {
                cbSeparateGenderFee.setChecked(currentExpense.isSeparateGenderFee());
                if (layoutSeparateGenderInput != null) {
                    layoutSeparateGenderInput.setVisibility(currentExpense.isSeparateGenderFee() ? View.VISIBLE : View.GONE);
                }
            }
            if (etMaleFee != null && currentExpense.getMaleFee() > 0) {
                etMaleFee.setText(String.valueOf((long) currentExpense.getMaleFee()));
            }
            if (etFemaleFee != null && currentExpense.getFemaleFee() > 0) {
                etFemaleFee.setText(String.valueOf((long) currentExpense.getFemaleFee()));
            }

            if (cbUseInventoryShuttle != null) {
                cbUseInventoryShuttle.setChecked(currentExpense.isUseInventoryShuttle());
                if (layoutInventoryShuttleCalc != null) {
                    layoutInventoryShuttleCalc.setVisibility(currentExpense.isUseInventoryShuttle() ? View.VISIBLE : View.GONE);
                }
                etShuttleFee.setEnabled(!currentExpense.isUseInventoryShuttle());
            }

            if (etShuttlesUsed != null && currentExpense.getShuttlesUsedCount() > 0) {
                etShuttlesUsed.setText(String.valueOf(currentExpense.getShuttlesUsedCount()));
            }

            if (tvStockStatus != null) {
                if (currentShuttleStock != null && currentShuttleStock.id != -1) {
                    tvStockStatus.setText("Kho cầu: " + currentShuttleStock.brandName + " (Còn " + currentShuttleStock.remainingShuttles + " trái - Giá: " + formatMoney(currentShuttleStock.pricePerShuttle) + " VNĐ/trái)");
                } else {
                    tvStockStatus.setText("Kho cầu: Chưa nhập kho cầu nào!");
                }
            }
        }
    }

    private void saveExpensesFromInput() {
        if (etCourtFee == null || etShuttleFee == null || etOtherFee == null || etExpenseNote == null) return;
        double court = parseDoubleSafe(etCourtFee.getText().toString());
        double shuttle = parseDoubleSafe(etShuttleFee.getText().toString());
        double other = parseDoubleSafe(etOtherFee.getText().toString());
        String note = etExpenseNote.getText().toString().trim();

        boolean isFixed = cbFixedFeeMode != null && cbFixedFeeMode.isChecked();
        double fixedFee = etFixedFeePerPlayer != null ? parseDoubleSafe(etFixedFeePerPlayer.getText().toString()) : 0;

        boolean isSepGender = cbSeparateGenderFee != null && cbSeparateGenderFee.isChecked();
        double maleFee = etMaleFee != null ? parseDoubleSafe(etMaleFee.getText().toString()) : 0;
        double femaleFee = etFemaleFee != null ? parseDoubleSafe(etFemaleFee.getText().toString()) : 0;

        int shuttlesUsed = etShuttlesUsed != null ? (int) parseDoubleSafe(etShuttlesUsed.getText().toString()) : 0;
        boolean useInvShuttle = cbUseInventoryShuttle != null && cbUseInventoryShuttle.isChecked();

        currentExpense = new ExpenseData(court, shuttle, other, note, isFixed, fixedFee, isSepGender, maleFee, femaleFee, shuttlesUsed, useInvShuttle);
        dbHelper.saveExpense(court, shuttle, other, note, isFixed, fixedFee, isSepGender, maleFee, femaleFee, shuttlesUsed, useInvShuttle);
    }

    private double calculateTotalCollected() {
        int maleCount = 0;
        int femaleCount = 0;
        for (Player p : playerList) {
            if (p.getGender() == Gender.MALE) maleCount++;
            else femaleCount++;
        }

        boolean isFixed = cbFixedFeeMode != null && cbFixedFeeMode.isChecked();
        boolean isSepGender = cbSeparateGenderFee != null && cbSeparateGenderFee.isChecked();
        int playerCount = playerList.size();
        double totalExp = currentExpense != null ? currentExpense.getTotalExpense() : 0;
        double perPlayerFee = playerCount > 0 ? (totalExp / playerCount) : 0;

        double totalCollected = 0;
        for (Player p : playerList) {
            double baseFee = isSepGender ? (p.getGender() == Gender.MALE ? parseDoubleSafe(etMaleFee.getText().toString()) : parseDoubleSafe(etFemaleFee.getText().toString()))
                    : (isFixed ? parseDoubleSafe(etFixedFeePerPlayer.getText().toString()) : perPlayerFee);
            double actualPayable = Math.max(0, baseFee - p.getDiscountAmount());
            totalCollected += actualPayable;
        }
        return totalCollected;
    }

    private void calculateAndDisplayExpenses() {
        if (tvTotalExpense == null || tvPlayerCountSummary == null || tvCostPerPlayer == null) return;
        double totalExp = currentExpense != null ? currentExpense.getTotalExpense() : 0;
        int playerCount = playerList.size();

        int maleCount = 0;
        int femaleCount = 0;
        for (Player p : playerList) {
            if (p.getGender() == Gender.MALE) maleCount++;
            else femaleCount++;
        }

        boolean isFixed = cbFixedFeeMode != null && cbFixedFeeMode.isChecked();
        boolean isSepGender = cbSeparateGenderFee != null && cbSeparateGenderFee.isChecked();

        tvTotalExpense.setText("Tổng chi phí: " + formatMoney(totalExp) + " VNĐ");

        double totalCollected = calculateTotalCollected();
        double balance = totalCollected - totalExp;

        StringBuilder discountBreakdownSb = new StringBuilder();
        int discountedCount = 0;
        for (Player p : playerList) {
            if (p.getDiscountAmount() > 0) {
                discountedCount++;
                double baseFee = getBaseFeeForPlayer(p);
                double payable = Math.max(0, baseFee - p.getDiscountAmount());
                discountBreakdownSb.append("• ").append(p.getName()).append(": ").append(formatMoney(payable)).append(" VNĐ (Giảm ").append(formatMoney(p.getDiscountAmount())).append(" VNĐ)\n");
            }
        }

        if (isSepGender) {
            double maleFee = etMaleFee != null ? parseDoubleSafe(etMaleFee.getText().toString()) : 0;
            double femaleFee = etFemaleFee != null ? parseDoubleSafe(etFemaleFee.getText().toString()) : 0;

            tvPlayerCountSummary.setText("Tham gia: " + playerCount + " người (Nam: " + maleCount + ", Nữ: " + femaleCount + ")");
            tvCostPerPlayer.setText("Thu phí: Nam " + formatMoney(maleFee) + "k | Nữ " + formatMoney(femaleFee) + "k");

            if (tvGenderBreakdown != null) {
                tvGenderBreakdown.setVisibility(View.VISIBLE);
                String details = "• Nam (" + maleCount + " người): " + formatMoney(maleCount * maleFee) + " VNĐ\n• Nữ (" + femaleCount + " người): " + formatMoney(femaleCount * femaleFee) + " VNĐ";
                if (discountedCount > 0) {
                    details += "\n🏷️ Giảm giá riêng:\n" + discountBreakdownSb.toString().trim();
                }
                tvGenderBreakdown.setText(details);
            }

        } else if (isFixed) {
            double fixedFee = etFixedFeePerPlayer != null ? parseDoubleSafe(etFixedFeePerPlayer.getText().toString()) : 0;

            tvPlayerCountSummary.setText("Số người tham gia chia: " + playerCount + " người (Thu cố định)");
            tvCostPerPlayer.setText("Mỗi người cần đóng: " + formatMoney(fixedFee) + " VNĐ (Thu cố định)");

            if (tvGenderBreakdown != null) {
                if (discountedCount > 0) {
                    tvGenderBreakdown.setVisibility(View.VISIBLE);
                    tvGenderBreakdown.setText("🏷️ Giảm giá riêng:\n" + discountBreakdownSb.toString().trim());
                } else {
                    tvGenderBreakdown.setVisibility(View.GONE);
                }
            }
        } else {
            double perPlayer = playerCount > 0 ? (totalExp / playerCount) : 0;
            tvPlayerCountSummary.setText("Số người tham gia chia: " + playerCount + " người (Chia đều)");
            tvCostPerPlayer.setText("Mỗi người cần đóng: " + formatMoney(perPlayer) + " VNĐ (Chia đều)");

            if (tvGenderBreakdown != null) {
                if (discountedCount > 0) {
                    tvGenderBreakdown.setVisibility(View.VISIBLE);
                    tvGenderBreakdown.setText("🏷️ Giảm giá riêng:\n" + discountBreakdownSb.toString().trim());
                } else {
                    tvGenderBreakdown.setVisibility(View.GONE);
                }
            }
        }

        if (tvHostBalance != null) {
            tvHostBalance.setVisibility(View.VISIBLE);
            if (balance >= 0) {
                tvHostBalance.setText("Dư quỹ buổi đánh: +" + formatMoney(balance) + " VNĐ");
                tvHostBalance.setTextColor(Color.parseColor("#2E7D32"));
            } else {
                tvHostBalance.setText("Bù lỗ buổi đánh (Host): " + formatMoney(balance) + " VNĐ");
                tvHostBalance.setTextColor(Color.parseColor("#D81B60"));
            }
        }
    }

    private void showRevenueReportDialog() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        Date now = new Date();

        String dateStr = dateFormat.format(now);
        String monthStr = monthFormat.format(now);
        String yearStr = yearFormat.format(now);

        DatabaseHelper.ReportSummary daily = dbHelper.getDailyReport(dateStr);
        DatabaseHelper.ReportSummary monthly = dbHelper.getMonthlyReport(monthStr);
        DatabaseHelper.ReportSummary yearly = dbHelper.getYearlyReport(yearStr);

        StringBuilder sb = new StringBuilder();
        sb.append("📅 HÔM NAY (").append(dateStr).append("):\n");
        sb.append(" • Số buổi: ").append(daily.sessionCount).append(" buổi\n");
        sb.append(" • Tổng thu: ").append(formatMoney(daily.totalCollected)).append(" VNĐ\n");
        sb.append(" • Tổng chi: ").append(formatMoney(daily.totalExpense)).append(" VNĐ\n");
        sb.append(" • ").append(daily.profitOrLoss >= 0 ? "Lợi nhuận: +" : "Lỗ: ").append(formatMoney(daily.profitOrLoss)).append(" VNĐ\n");

        sb.append("\n📆 THÁNG NÀY (Tháng ").append(monthStr).append("):\n");
        sb.append(" • Số buổi: ").append(monthly.sessionCount).append(" buổi\n");
        sb.append(" • Tổng thu: ").append(formatMoney(monthly.totalCollected)).append(" VNĐ\n");
        sb.append(" • Tổng chi: ").append(formatMoney(monthly.totalExpense)).append(" VNĐ\n");
        sb.append(" • ").append(monthly.profitOrLoss >= 0 ? "Lợi nhuận ròng: +" : "Lỗ ròng: ").append(formatMoney(monthly.profitOrLoss)).append(" VNĐ\n");

        sb.append("\n📊 NĂM NAY (Năm ").append(yearStr).append("):\n");
        sb.append(" • Số buổi: ").append(yearly.sessionCount).append(" buổi\n");
        sb.append(" • Tổng thu: ").append(formatMoney(yearly.totalCollected)).append(" VNĐ\n");
        sb.append(" • Tổng chi: ").append(formatMoney(yearly.totalExpense)).append(" VNĐ\n");
        sb.append(" • ").append(yearly.profitOrLoss >= 0 ? "Lợi nhuận ròng: +" : "Lỗ ròng: ").append(formatMoney(yearly.profitOrLoss)).append(" VNĐ");

        new AlertDialog.Builder(this)
                .setTitle("📈 BÁO CÁO DOANH THU & LỢI NHUẬN")
                .setMessage(sb.toString())
                .setPositiveButton("Đóng", null)
                .setNeutralButton("📋 Quản Lý Lịch Sử Buổi Đánh", (dialog, which) -> showManageSessionReportsDialog())
                .show();
    }

    // --- DIALOG SỬA / XÓA LỊCH SỬ DOANH THU ---
    private void showManageSessionReportsDialog() {
        List<DatabaseHelper.SessionReportItem> reportItems = dbHelper.getAllSessionReportsList();
        if (reportItems.isEmpty()) {
            Toast.makeText(this, "Chưa có buổi đánh nào được chốt lưu doanh thu!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> displayTitles = new ArrayList<>();
        for (DatabaseHelper.SessionReportItem item : reportItems) {
            String status = item.profitOrLoss >= 0 ? "Lời +" + formatMoney(item.profitOrLoss) : "Lỗ " + formatMoney(item.profitOrLoss);
            displayTitles.add("Buổi " + item.dateStr + " (" + item.playerCount + " người): Thu " + formatMoney(item.totalCollected) + "k - Chi " + formatMoney(item.totalExpense) + "k (" + status + ")");
        }

        new AlertDialog.Builder(this)
                .setTitle("📋 Chọn Buổi Đánh Để Sửa/Xóa")
                .setItems(displayTitles.toArray(new String[0]), (dialog, which) -> {
                    DatabaseHelper.SessionReportItem selectedItem = reportItems.get(which);
                    String[] options = {"Chỉnh Sửa Doanh Thu / Chi Phí", "Xóa Buổi Đánh Này"};

                    new AlertDialog.Builder(this)
                            .setTitle("Quản lý buổi: " + selectedItem.dateStr)
                            .setItems(options, (d, w) -> {
                                if (w == 0) {
                                    showEditSessionReportDialog(selectedItem);
                                } else {
                                    dbHelper.deleteSessionReport(selectedItem.id);
                                    Toast.makeText(this, "Đã xóa buổi đánh thành công!", Toast.LENGTH_SHORT).show();
                                    showRevenueReportDialog();
                                }
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showEditSessionReportDialog(DatabaseHelper.SessionReportItem item) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView tvInfo = new TextView(this);
        tvInfo.setText("Sửa doanh thu buổi ngày " + item.dateStr + ":");
        tvInfo.setTextColor(Color.BLACK);
        layout.addView(tvInfo);

        EditText etTotalExp = new EditText(this);
        etTotalExp.setHint("Tổng chi phí (VNĐ)");
        etTotalExp.setInputType(InputType.TYPE_CLASS_NUMBER);
        etTotalExp.setText(String.valueOf((long) item.totalExpense));
        etTotalExp.setTextColor(Color.BLACK);

        EditText etTotalColl = new EditText(this);
        etTotalColl.setHint("Tổng thu được (VNĐ)");
        etTotalColl.setInputType(InputType.TYPE_CLASS_NUMBER);
        etTotalColl.setText(String.valueOf((long) item.totalCollected));
        etTotalColl.setTextColor(Color.BLACK);

        layout.addView(etTotalExp);
        layout.addView(etTotalColl);

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh Sửa Doanh Thu")
                .setView(layout)
                .setPositiveButton("Lưu Thay Đổi", (dialog, which) -> {
                    double exp = parseDoubleSafe(etTotalExp.getText().toString());
                    double coll = parseDoubleSafe(etTotalColl.getText().toString());
                    dbHelper.updateSessionReport(item.id, exp, coll);
                    Toast.makeText(this, "Đã cập nhật doanh thu thành công!", Toast.LENGTH_SHORT).show();
                    showRevenueReportDialog();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void calculateAndDisplayStats() {
        if (tvStatsOverview == null || tvTopPlayers == null) return;
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

        List<HistoryManager.SetRecord> historyRecords = historyManager.getAllSetRecords();
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
        double totalExp = currentExpense != null ? currentExpense.getTotalExpense() : 0;
        int playerCount = playerList.size();

        int maleCount = 0;
        int femaleCount = 0;
        for (Player p : playerList) {
            if (p.getGender() == Gender.MALE) maleCount++;
            else femaleCount++;
        }

        boolean isFixed = cbFixedFeeMode != null && cbFixedFeeMode.isChecked();
        boolean isSepGender = cbSeparateGenderFee != null && cbSeparateGenderFee.isChecked();
        boolean isThuRieng = isFixed || isSepGender;

        StringBuilder sb = new StringBuilder();
        sb.append("BÁO CÁO THU TIỀN SÂN CẦU LÔNG\n");
        sb.append("------------------------------------\n");
        if (!isThuRieng) {
            if (currentExpense != null && currentExpense.getCourtFee() > 0) {
                sb.append("• Tiền sân: ").append(formatMoney(currentExpense.getCourtFee())).append(" VNĐ\n");
            }
            if (currentExpense != null && currentExpense.getShuttleFee() > 0) {
                sb.append("• Tiền cầu: ").append(formatMoney(currentExpense.getShuttleFee())).append(" VNĐ\n");
            }
            if (currentExpense != null && currentExpense.getOtherFee() > 0) {
                sb.append("• Chi phí khác: ").append(formatMoney(currentExpense.getOtherFee())).append(" VNĐ\n");
            }
            sb.append("------------------------------------\n");
            sb.append("TỔNG CHI PHÍ BUỔI ĐÁNH: ").append(formatMoney(totalExp)).append(" VNĐ\n");
        }
        sb.append("Tổng số người tham gia: ").append(playerCount).append(" người (Nam: ").append(maleCount).append(", Nữ: ").append(femaleCount).append(")\n");

        if (isSepGender) {
            double maleFee = etMaleFee != null ? parseDoubleSafe(etMaleFee.getText().toString()) : 0;
            double femaleFee = etFemaleFee != null ? parseDoubleSafe(etFemaleFee.getText().toString()) : 0;
            sb.append("MỨC THU CHUẨN: Nam ").append(formatMoney(maleFee)).append(" VNĐ | Nữ ").append(formatMoney(femaleFee)).append(" VNĐ\n");
        } else if (isFixed) {
            double fixedFee = etFixedFeePerPlayer != null ? parseDoubleSafe(etFixedFeePerPlayer.getText().toString()) : 0;
            sb.append("MỨC THU CHUẨN (CỐ ĐỊNH): ").append(formatMoney(fixedFee)).append(" VNĐ / người\n");
        } else {
            double perPlayer = playerCount > 0 ? (totalExp / playerCount) : 0;
            sb.append("MỨC THU CHUẨN (CHIA ĐỀU): ").append(formatMoney(perPlayer)).append(" VNĐ / người\n");
        }

        StringBuilder discountSb = new StringBuilder();
        for (Player p : playerList) {
            if (p.getDiscountAmount() > 0) {
                double baseFee = getBaseFeeForPlayer(p);
                double payable = Math.max(0, baseFee - p.getDiscountAmount());
                if (payable > 0) {
                    discountSb.append(" • ").append(p.getName()).append(": ").append(formatMoney(payable)).append(" VNĐ (Được giảm ").append(formatMoney(p.getDiscountAmount())).append(" VNĐ)\n");
                }
            }
        }

        if (discountSb.length() > 0) {
            sb.append("------------------------------------\n");
            sb.append("Danh sách giảm giá riêng:\n");
            sb.append(discountSb.toString());
        }

        if (currentExpense != null && !currentExpense.getNote().isEmpty()) {
            sb.append("------------------------------------\n");
            sb.append("Thông tin chuyển khoản / Ghi chú:\n");
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

    private Player getPlayerById(String id) {
        for (Player p : playerList) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    private void savePlayersToPrefs() {
        dbHelper.savePlayers(playerList);
        dbHelper.saveHistory(historyManager.getAllSetRecords());
    }

    private void loadPlayersFromPrefs() {
        playerList.clear();
        playerList.addAll(dbHelper.loadPlayers());
        List<HistoryManager.SetRecord> loadedHistory = dbHelper.loadHistory(playerList);
        historyManager.setRecords(loadedHistory);
    }

    private void showScoreInputDialog() {
        List<HistoryManager.SetRecord> allRecords = historyManager.getAllSetRecords();
        if (allRecords.isEmpty()) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);

        List<EditText> inputs1 = new ArrayList<>();
        List<EditText> inputs2 = new ArrayList<>();

        for (int i = 0; i < allRecords.size(); i++) {
            HistoryManager.SetRecord rec = allRecords.get(i);

            TextView tvTitle = new TextView(this);
            tvTitle.setText("Trận " + (i + 1) + " (Sân " + rec.getCourtNumber() + " - Lượt " + rec.getRoundNumber() + "):");
            tvTitle.setPadding(0, 20, 0, 10);
            tvTitle.setTextSize(14);
            tvTitle.setTextColor(Color.BLACK);
            layout.addView(tvTitle);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            EditText et1 = new EditText(this);
            et1.setHint("Điểm Đội 1");
            et1.setInputType(InputType.TYPE_CLASS_NUMBER);
            et1.setTextColor(Color.BLACK);
            if (rec.getScore1() != null) et1.setText(String.valueOf(rec.getScore1()));
            LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            et1.setLayoutParams(p1);
            row.addView(et1);
            inputs1.add(et1);

            TextView tvVs = new TextView(this);
            tvVs.setText(" - ");
            tvVs.setTextSize(16);
            tvVs.setTextColor(Color.BLACK);
            row.addView(tvVs);

            EditText et2 = new EditText(this);
            et2.setHint("Điểm Đội 2");
            et2.setInputType(InputType.TYPE_CLASS_NUMBER);
            et2.setTextColor(Color.BLACK);
            if (rec.getScore2() != null) et2.setText(String.valueOf(rec.getScore2()));
            LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            et2.setLayoutParams(p2);
            row.addView(et2);
            inputs2.add(et2);

            layout.addView(row);
        }

        new AlertDialog.Builder(this)
                .setTitle("Nhập Tỉ Số Các Set")
                .setView(scrollView)
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

        String avoidOptionText = (player.getAvoidPlayerId() != null) ?
                "Hủy yêu cầu né người chơi" : "🚫 Yêu cầu KHÔNG xếp cùng...";

        String styleOptionText = "⚡ Chế độ chơi (" + player.getPlayStyle().getDisplayName() + ")...";

        String discountOptionText = player.getDiscountAmount() > 0 ?
                "🏷️ Giảm giá riêng (Đang giảm " + formatMoney(player.getDiscountAmount()) + " VNĐ)" :
                "🏷️ Giảm giá / Mức đóng riêng...";

        String[] options = {
                "Sửa thông tin",
                reqOptionText,
                avoidOptionText,
                styleOptionText,
                discountOptionText,
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
                            updatePlayerListUI();
                            Toast.makeText(this, "Đã hủy yêu cầu ghép cặp.", Toast.LENGTH_SHORT).show();
                        } else {
                            showSelectPartnerDialog(player);
                        }
                    } else if (which == 2) {
                        if (player.getAvoidPlayerId() != null) {
                            player.setAvoidPlayerId(null);
                            savePlayersToPrefs();
                            updatePlayerListUI();
                            Toast.makeText(this, "Đã hủy yêu cầu né người chơi.", Toast.LENGTH_SHORT).show();
                        } else {
                            showAvoidPlayerDialog(player);
                        }
                    } else if (which == 3) {
                        showPlayStyleDialog(player);
                    } else if (which == 4) {
                        showDiscountDialog(player);
                    } else if (which == 5) {
                        showPlayerSetHistoryDialog(player);
                    } else if (which == 6) {
                        player.setResting(!player.isResting());
                        savePlayersToPrefs();
                        updatePlayerListUI();
                        Toast.makeText(this, player.getName() + (player.isResting() ? " đã tạm nghỉ." : " đã tiếp tục chơi."), Toast.LENGTH_SHORT).show();
                    } else if (which == 7) {
                        new AlertDialog.Builder(this)
                                .setTitle("Xác nhận xóa")
                                .setMessage("Bạn có chắc chắn muốn xóa " + player.getName() + " khỏi danh sách?")
                                .setPositiveButton("Xóa", (d, w) -> {
                                    playerList.remove(position);
                                    savePlayersToPrefs();
                                    updatePlayerListUI();
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
                    updatePlayerListUI();
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
        tvNameLabel.setTextColor(Color.BLACK);
        layout.addView(tvNameLabel);

        EditText etName = new EditText(this);
        etName.setText(player.getName());
        etName.setTextColor(Color.BLACK);
        layout.addView(etName);

        TextView tvGenderLabel = new TextView(this);
        tvGenderLabel.setText("Giới tính:");
        tvGenderLabel.setTextColor(Color.BLACK);
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
        tvSkillLabel.setTextColor(Color.BLACK);
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
        tvMatchesLabel.setTextColor(Color.BLACK);
        tvMatchesLabel.setPadding(0, 20, 0, 0);
        layout.addView(tvMatchesLabel);

        EditText etMatches = new EditText(this);
        etMatches.setInputType(InputType.TYPE_CLASS_NUMBER);
        etMatches.setText(String.valueOf(player.getMatchesPlayed()));
        etMatches.setTextColor(Color.BLACK);
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
                    updatePlayerListUI();
                    Toast.makeText(this, "Đã cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showManualMatchDialog() {
        if (playerList.size() < 4) {
            Toast.makeText(this, "Cần có ít nhất 4 người chơi trong danh sách để xếp thủ công!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xếp Trận Thủ Công");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = 32;
        layout.setPadding(padding, padding, padding, padding);

        ArrayAdapter<Player> pAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, playerList);
        pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        TextView tv1 = new TextView(this);
        tv1.setText("Đội 1 - Lông thủ 1:");
        tv1.setTextColor(Color.BLACK);
        Spinner sp1 = new Spinner(this);
        sp1.setAdapter(pAdapter);

        TextView tv2 = new TextView(this);
        tv2.setText("Đội 1 - Lông thủ 2:");
        tv2.setTextColor(Color.BLACK);
        Spinner sp2 = new Spinner(this);
        sp2.setAdapter(pAdapter);
        if (playerList.size() > 1) sp2.setSelection(1);

        TextView tv3 = new TextView(this);
        tv3.setText("Đội 2 - Lông thủ 1:");
        tv3.setTextColor(Color.BLACK);
        Spinner sp3 = new Spinner(this);
        sp3.setAdapter(pAdapter);
        if (playerList.size() > 2) sp3.setSelection(2);

        TextView tv4 = new TextView(this);
        tv4.setText("Đội 2 - Lông thủ 2:");
        tv4.setTextColor(Color.BLACK);
        Spinner sp4 = new Spinner(this);
        sp4.setAdapter(pAdapter);
        if (playerList.size() > 3) sp4.setSelection(3);

        layout.addView(tv1);
        layout.addView(sp1);
        layout.addView(tv2);
        layout.addView(sp2);
        layout.addView(tv3);
        layout.addView(sp3);
        layout.addView(tv4);
        layout.addView(sp4);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);

        builder.setView(scrollView);

        builder.setPositiveButton("Xếp Trận", (dialog, which) -> {
            Player p1 = (Player) sp1.getSelectedItem();
            Player p2 = (Player) sp2.getSelectedItem();
            Player p3 = (Player) sp3.getSelectedItem();
            Player p4 = (Player) sp4.getSelectedItem();

            if (p1.getId().equals(p2.getId()) || p1.getId().equals(p3.getId()) || p1.getId().equals(p4.getId()) ||
                    p2.getId().equals(p3.getId()) || p2.getId().equals(p4.getId()) || p3.getId().equals(p4.getId())) {
                Toast.makeText(this, "Lỗi: Không được chọn trùng một người chơi trong cùng một trận!", Toast.LENGTH_LONG).show();
                return;
            }

            MatchOption match = BadmintonMatchScheduler.evaluateMatch(p1, p2, p3, p4, historyManager);

            currentRoundRecords.clear();
            StringBuilder sb = new StringBuilder();
            sb.append("=== XẾP THỦ CÔNG (SÂN 1) ===\n")
                    .append("Đội 1: ").append(p1.getName()).append(" (").append(p1.getGender().getDisplayName()).append(" - ").append(p1.getSkill().getDisplayName()).append(") + ")
                    .append(p2.getName()).append(" (").append(p2.getGender().getDisplayName()).append(" - ").append(p2.getSkill().getDisplayName()).append(")\n")
                    .append("VS\n")
                    .append("Đội 2: ").append(p3.getName()).append(" (").append(p3.getGender().getDisplayName()).append(" - ").append(p3.getSkill().getDisplayName()).append(") + ")
                    .append(p4.getName()).append(" (").append(p4.getGender().getDisplayName()).append(" - ").append(p4.getSkill().getDisplayName()).append(")\n")
                    .append("(Lệch trình: ").append(String.format("%.2f", match.getSkillDiff()))
                    .append(" | Lịch sử phạt: ").append(String.format("%.1f", match.getHistoryPenalty())).append(")\n\n");

            p1.setMatchesPlayed(p1.getMatchesPlayed() + 1);
            p2.setMatchesPlayed(p2.getMatchesPlayed() + 1);
            p3.setMatchesPlayed(p3.getMatchesPlayed() + 1);
            p4.setMatchesPlayed(p4.getMatchesPlayed() + 1);

            HistoryManager.SetRecord rec = historyManager.recordMatch(1, match.getTeam1(), match.getTeam2(), match.getSkillDiff());
            currentRoundRecords.add(rec);
            historyManager.nextRound();

            tvMatchResult.setText(sb.toString().trim());
            savePlayersToPrefs();
            updatePlayerListUI();

            new AlertDialog.Builder(this)
                    .setTitle("Xếp Trận Thủ Công Thành Công!")
                    .setMessage("Bạn có muốn nhập tỉ số cho trận đấu này ngay bây giờ?")
                    .setPositiveButton("Nhập Tỉ Số", (d, w) -> showScoreInputDialog())
                    .setNegativeButton("Để Sau", null)
                    .show();
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showTopToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL, 0, 120);
        toast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_QR_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {}
                qrCodeUri = uri;
                if (ivQrCodePreview != null) {
                    ivQrCodePreview.setImageURI(qrCodeUri);
                    ivQrCodePreview.setVisibility(View.VISIBLE);
                }
                getSharedPreferences("HostBadmintonPrefs", MODE_PRIVATE)
                    .edit().putString("qr_code_uri", qrCodeUri.toString()).apply();
                Toast.makeText(this, "Đã chọn ảnh mã QR ngân hàng thành công!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
