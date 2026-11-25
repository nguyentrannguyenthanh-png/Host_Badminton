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

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etPhone;
    private CheckBox cbRememberPass;
    private Button btnSubmit, btnSwitchMode, btnForgotPassword;
    private LinearLayout layoutRegisterFields;
    private TextView tvTitleMode;
    private boolean isLoginMode = true;

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
        etPhone = findViewById(R.id.etPhone);
        layoutRegisterFields = findViewById(R.id.layoutRegisterFields);
        btnSubmit = findViewById(R.id.btnLogin);
        btnSwitchMode = findViewById(R.id.btnSwitchMode);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        tvTitleMode = findViewById(R.id.tvTitleMode);

        loadSavedCredentials();

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
                String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
                if (phone.isEmpty()) {
                    showTopToast("Vui lòng nhập số điện thoại!");
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
                    if (etPhone != null) etPhone.setText("");
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
            tvTitleMode.setText("ĐĂNG NHẬP TÀI KHOẢN CHỦ SÂN");
            btnSubmit.setText("ĐĂNG NHẬP");
            btnSwitchMode.setText("Chưa có tài khoản? Đăng ký ngay");
            if (layoutRegisterFields != null) layoutRegisterFields.setVisibility(View.GONE);
            cbRememberPass.setVisibility(View.VISIBLE);
        } else {
            tvTitleMode.setText("ĐĂNG KÝ TÀI KHOẢN CHỦ SÂN MỚI");
            btnSubmit.setText("ĐĂNG KÝ");
            btnSwitchMode.setText("Đã có tài khoản? Đăng nhập ngay");
            if (layoutRegisterFields != null) layoutRegisterFields.setVisibility(View.VISIBLE);
            cbRememberPass.setVisibility(View.GONE);
        }
    }

    private void showForgotPasswordDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = 32;
        layout.setPadding(padding, padding, padding, padding);
        layout.setBackgroundColor(Color.parseColor("#1E2738"));

        EditText etUser = new EditText(this);
        etUser.setHint("Nhập tên tài khoản");
        etUser.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        etUser.setTextColor(Color.WHITE);
        etUser.setHintTextColor(Color.parseColor("#8A94A6"));
        etUser.setBackgroundColor(Color.parseColor("#1A2233"));
        etUser.setPadding(24, 24, 24, 24);

        EditText etNewPass = new EditText(this);
        etNewPass.setHint("Nhập mật khẩu mới");
        etNewPass.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD | android.text.InputType.TYPE_CLASS_TEXT);
        etNewPass.setTextColor(Color.WHITE);
        etNewPass.setHintTextColor(Color.parseColor("#8A94A6"));
        etNewPass.setBackgroundColor(Color.parseColor("#1A2233"));
        etNewPass.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 16, 0, 16);
        etUser.setLayoutParams(lp);
        etNewPass.setLayoutParams(lp);

        layout.addView(etUser);
        layout.addView(etNewPass);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Khôi Phục Mật Khẩu")
                .setView(scrollView)
                .setPositiveButton("Xác Nhận", (d, which) -> {
                    String user = etUser.getText().toString().trim();
                    String newPass = etNewPass.getText().toString().trim();

                    if (user.isEmpty() || newPass.isEmpty()) {
                        showTopToast("Vui lòng điền đầy đủ tất cả các trường!");
                        return;
                    }

                    if (dbHelper.isUserExists(user)) {
                        dbHelper.updatePassword(user, newPass);
                        showTopToast("Đổi mật khẩu thành công! Hãy đăng nhập lại.");
                    } else {
                        showTopToast("Tài khoản không tồn tại!");
                    }
                })
                .setNegativeButton("Hủy", null);

        AlertDialog dlg = builder.create();
        dlg.setOnShowListener(d -> {
            dlg.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.parseColor("#1E2738")));
            Button posBtn = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
            if (posBtn != null) {
                posBtn.setTextColor(Color.parseColor("#4DF197"));
                posBtn.setTypeface(null, Typeface.BOLD);
            }
            Button negBtn = dlg.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (negBtn != null) {
                negBtn.setTextColor(Color.parseColor("#8A94A6"));
            }
        });
        dlg.show();
    }

    private void showTopToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL, 0, 120);
        toast.show();
    }
}
