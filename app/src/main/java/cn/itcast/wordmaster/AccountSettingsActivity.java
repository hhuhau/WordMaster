package cn.itcast.wordmaster;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;
import cn.itcast.wordmaster.db.UserDao;

public class AccountSettingsActivity extends AppCompatActivity {
    private TextInputLayout usernameInputLayout;
    private TextInputLayout phoneInputLayout;
    private TextInputLayout passwordInputLayout;
    private MaterialButton saveButton;
    private UserDao userDao;
    private String currentPhoneNumber; // 保存当前登录的手机号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_account_settings);

        // 初始化视图
        usernameInputLayout = findViewById(R.id.usernameInputLayout);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        saveButton = findViewById(R.id.saveButton);
        ImageView backButton = findViewById(R.id.backButton);

        // 设置输入框样式
        setupInputLayout(usernameInputLayout, "用户名");
        setupInputLayout(phoneInputLayout, "手机号");
        setupInputLayout(passwordInputLayout, "密码");

        userDao = new UserDao(this);

        // 获取当前登录的手机号
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        currentPhoneNumber = prefs.getString("phone_number", "");

        // 加载用户信息
        loadUserInfo();

        // 设置返回按钮点击事件
        backButton.setOnClickListener(v -> finish());

        // 设置保存按钮点击事件
        saveButton.setOnClickListener(v -> saveUserInfo());
    }

    private void loadUserInfo() {
        if (currentPhoneNumber.isEmpty()) {
            Toast.makeText(this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String[] userInfo = userDao.getUserInfo(currentPhoneNumber);
        if (userInfo != null) {
            usernameInputLayout.getEditText().setText(userInfo[0]);
            phoneInputLayout.getEditText().setText(userInfo[1]);
            passwordInputLayout.getEditText().setText(userInfo[2]);
        } else {
            Toast.makeText(this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveUserInfo() {
        String newUsername = usernameInputLayout.getEditText().getText().toString().trim();
        String newPhoneNumber = phoneInputLayout.getEditText().getText().toString().trim();
        String newPassword = passwordInputLayout.getEditText().getText().toString().trim();

        // 验证输入不为空
        if (newUsername.isEmpty() || newPhoneNumber.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证手机号格式
        if (!newPhoneNumber.matches("^\\d{11}$")) {
            Toast.makeText(this, "请输入正确的手机号格式", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新用户信息
        boolean success = userDao.updateUserInfo(currentPhoneNumber, newUsername, newPhoneNumber, newPassword);
        if (success) {
            // 如果手机号变更了，更新SharedPreferences中保存的手机号
            if (!currentPhoneNumber.equals(newPhoneNumber)) {
                SharedPreferences.Editor editor = getSharedPreferences("user_info", MODE_PRIVATE).edit();
                editor.putString("phone_number", newPhoneNumber);
                editor.apply();
            }
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "保存失败，手机号可能已被使用", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupInputLayout(TextInputLayout inputLayout, String hint) {
        if (inputLayout != null && inputLayout.getEditText() != null) {
            // 启用浮动标签，使标签始终可见
            inputLayout.setHintEnabled(true);
            inputLayout.setHint(hint);
            
            // 设置浮动标签的样式和颜色
            inputLayout.setHintTextAppearance(R.style.TextInputLayoutHint);
            
            // 设置输入框的内边距，增加标签与输入框的距离
            inputLayout.getEditText().setPadding(
                inputLayout.getEditText().getPaddingLeft(),
                inputLayout.getEditText().getPaddingTop() + 8,
                inputLayout.getEditText().getPaddingRight(),
                inputLayout.getEditText().getPaddingBottom()
            );
            
            // 设置输入框获取焦点时的颜色变化
            inputLayout.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    inputLayout.setBoxStrokeColor(getResources().getColor(R.color.light_blue_border));
                } else {
                    inputLayout.setBoxStrokeColor(getResources().getColor(android.R.color.darker_gray));
                }
            });
        }
    }
}