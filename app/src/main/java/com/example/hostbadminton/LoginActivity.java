package com.example.hostbadminton;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etPhone, etOtp;
    private CheckBox cbRememberPass;
    private Button btnSubmit, btnSwitchMode, btnForgotPassword, btnSendOtp;
    private LinearLayout layoutOtp;
    private TextView tvTitleMode;
    private boolean isLoginMode = true;
    private String sentOtp = null;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_USERNAME = "saved_username";
    private static final String KEY_PASSWORD = "saved_password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        cbRememberPass = findViewById(R.id.cbRemember);
        etPhone = findViewById(R.id.etUsername);
        etOtp = findViewById(R.id.etPassword);
        layoutOtp = new android.widget.LinearLayout(this);
        btnSubmit = findViewById(R.id.btnLogin);
        btnSwitchMode = findViewById(R.id.btnLogin);
        btnForgotPassword = findViewById(R.id.btnLogin);
        btnSendOtp = findViewById(R.id.btnLogin);
        tvTitleMode = findViewById(R.id.tvTitleMode);

        // Tự động điền thông tin đăng nhập đã lưu
        loadSavedCredentials();

        btnSendOtp.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (phone.isEmpty() || phone.length() < 9) {
                showTopToast("Vui lòng nhập số điện thoại hợp lệ để nhận mã OTP!");
                return;
            }
            sentOtp = String.valueOf(100000 + new Random().nextInt(900000));
            showTopToast("Mã OTP xác minh của bạn là: " + sentOtp);
        });

        btnSubmit.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                showTopToast("Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
                return;
            }

            if (isLoginMode) {
                boolean isValid = dbHelper.checkUser(user, pass);
                if (isValid) {
                    // Lưu thông tin ghi nhớ đăng nhập
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (cbRememberPass.isChecked()) {
                        editor.putBoolean(KEY_REMEMBER, true);
                        editor.putString(KEY_USERNAME, user);
                        editor.putString(KEY_PASSWORD, pass);
                    } else {
                        editor.clear();
                    }
                    editor.apply();

                    showTopToast("Đăng nhập thành công!");
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    showTopToast("Tài khoản không tồn tại hoặc mật khẩu không đúng!");
                }
            } else {
                String phone = etPhone.getText().toString().trim();
                String otpInput = etOtp.getText().toString().trim();

                if (phone.isEmpty()) {
                    showTopToast("Vui lòng nhập số điện thoại!");
                    return;
                }
                if (sentOtp == null || !sentOtp.equals(otpInput)) {
                    showTopToast("Mã OTP không chính xác hoặc chưa được gửi/xác minh!");
                    return;
                }
                if (dbHelper.isUserExists(user)) {
                    showTopToast("Tài khoản này đã tồn tại! Vui lòng chọn tên khác.");
                    return;
                }

                boolean success = dbHelper.registerUser(user, pass, phone);
                if (success) {
                    showTopToast("Đăng ký thành công! Hãy đăng nhập.");
                    isLoginMode = true;
                    updateModeUI();
                    etPassword.setText("");
                    etPhone.setText("");
                    etOtp.setText("");
                    sentOtp = null;
                } else {
                    showTopToast("Đăng ký thất bại, vui lòng thử lại!");
                }
            }
        });

        btnSwitchMode.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateModeUI();
        });

        btnForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loadSavedCredentials() {
        boolean isRemembered = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        if (isRemembered) {
            String savedUser = sharedPreferences.getString(KEY_USERNAME, "");
            String savedPass = sharedPreferences.getString(KEY_PASSWORD, "");
            etUsername.setText(savedUser);
            etPassword.setText(savedPass);
            cbRememberPass.setChecked(true);
        }
    }

    private void updateModeUI() {
        if (isLoginMode) {
            tvTitleMode.setText("Đăng Nhập Tài Khoản Chủ Sân");
            btnSubmit.setText("Đăng Nhập");
            btnSwitchMode.setText("Chưa có tài khoản? Đăng ký ngay");
            etPhone.setVisibility(View.GONE);
            layoutOtp.setVisibility(View.GONE);
            cbRememberPass.setVisibility(View.VISIBLE);
        } else {
            tvTitleMode.setText("Đăng Ký Tài Khoản Chủ Sân Mới");
            btnSubmit.setText("Đăng Ký");
            btnSwitchMode.setText("Đã có tài khoản? Đăng nhập ngay");
            etPhone.setVisibility(View.VISIBLE);
            layoutOtp.setVisibility(View.VISIBLE);
            cbRememberPass.setVisibility(View.GONE);
        }
    }

    private String forgotSentOtp = null;

    private void showForgotPasswordDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = 32;
        layout.setPadding(padding, padding, padding, padding);

        EditText etUser = new EditText(this);
        etUser.setHint("Nhập tên tài khoản");
        etUser.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        etUser.setTextColor(Color.BLACK);
        etUser.setHintTextColor(Color.parseColor("#757575"));

        EditText etPhone = new EditText(this);
        etPhone.setHint("Nhập số điện thoại đã đăng ký");
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        etPhone.setTextColor(Color.BLACK);
        etPhone.setHintTextColor(Color.parseColor("#757575"));

        LinearLayout layoutOtpBox = new LinearLayout(this);
        layoutOtpBox.setOrientation(LinearLayout.HORIZONTAL);

        EditText etOtp = new EditText(this);
        etOtp.setHint("Mã OTP (6 số)");
        etOtp.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etOtp.setTextColor(Color.BLACK);
        etOtp.setHintTextColor(Color.parseColor("#757575"));
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        etOtp.setLayoutParams(p1);

        Button btnSendOtp = new Button(this);
        btnSendOtp.setText("Gửi Mã");
        btnSendOtp.setBackgroundColor(Color.parseColor("#FF7096"));
        btnSendOtp.setTextColor(Color.WHITE);
        btnSendOtp.setTypeface(null, Typeface.BOLD);

        btnSendOtp.setOnClickListener(v -> {
            String user = etUser.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            if (user.isEmpty() || phone.isEmpty()) {
                showTopToast("Vui lòng nhập tài khoản và số điện thoại trước!");
                return;
            }
            if (dbHelper.checkUserPhone(user, phone)) {
                forgotSentOtp = String.valueOf(100000 + new Random().nextInt(900000));
                showTopToast("Mã OTP khôi phục của bạn là: " + forgotSentOtp);
            } else {
                showTopToast("Tài khoản hoặc số điện thoại không khớp trong hệ thống!");
            }
        });

        layoutOtpBox.addView(etOtp);
        layoutOtpBox.addView(btnSendOtp);

        EditText etNewPass = new EditText(this);
        etNewPass.setHint("Nhập mật khẩu mới");
        etNewPass.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD | android.text.InputType.TYPE_CLASS_TEXT);
        etNewPass.setTextColor(Color.BLACK);
        etNewPass.setHintTextColor(Color.parseColor("#757575"));

        layout.addView(etUser);
        layout.addView(etPhone);
        layout.addView(layoutOtpBox);
        layout.addView(etNewPass);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Khôi Phục Mật Khẩu qua OTP")
                .setView(scrollView)
                .setPositiveButton("Xác Nhận Đổi Mật Khẩu", (d, which) -> {
                    String user = etUser.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    String otpInput = etOtp.getText().toString().trim();
                    String newPass = etNewPass.getText().toString().trim();

                    if (user.isEmpty() || phone.isEmpty() || otpInput.isEmpty() || newPass.isEmpty()) {
                        showTopToast("Vui lòng điền đầy đủ tất cả các trường!");
                        return;
                    }

                    if (forgotSentOtp == null || !forgotSentOtp.equals(otpInput)) {
                        showTopToast("Mã OTP không chính xác hoặc chưa được gửi/xác minh!");
                        return;
                    }

                    if (dbHelper.checkUserPhone(user, phone)) {
                        dbHelper.updatePassword(user, newPass);
                        showTopToast("Đổi mật khẩu thành công! Hãy đăng nhập lại.");
                        forgotSentOtp = null;
                    } else {
                        showTopToast("Xác thực tài khoản thất bại!");
                    }
                })
                .setNegativeButton("Hủy", null)
                .create();

        dialog.show();

        // Đồng bộ chữ đen cho nút bấm AlertDialog
        Button posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (posBtn != null) {
            posBtn.setTextColor(Color.BLACK);
            posBtn.setTypeface(null, Typeface.BOLD);
        }
        Button negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (negBtn != null) {
            negBtn.setTextColor(Color.BLACK);
        }
    }

    private void showTopToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL, 0, 120);
        toast.show();
    }
}
