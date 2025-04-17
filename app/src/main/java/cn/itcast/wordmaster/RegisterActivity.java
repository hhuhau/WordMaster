package cn.itcast.wordmaster;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;
import android.text.TextWatcher;
import android.text.Editable;
import cn.itcast.wordmaster.db.UserDao;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化所有输入框
        TextInputLayout usernameInputLayout = findViewById(R.id.usernameInputLayout);
        TextInputLayout phoneInputLayout = findViewById(R.id.phoneInputLayout);
        TextInputLayout passwordInputLayout = findViewById(R.id.passwordInputLayout);
        TextInputLayout confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);

        // 设置输入框的提示文字和焦点变化效果
        setupInputLayout(usernameInputLayout, "请输入用户名");
        setupInputLayout(phoneInputLayout, "请输入手机号");
        setupInputLayout(passwordInputLayout, "请输入密码");
        setupInputLayout(confirmPasswordInputLayout, "请再次输入密码");

        // 获取注册按钮并设置点击事件
        MaterialButton registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> {
            // 获取用户输入
            String username = usernameInputLayout.getEditText().getText().toString().trim();
            String phoneNumber = phoneInputLayout.getEditText().getText().toString().trim();
            String password = passwordInputLayout.getEditText().getText().toString().trim();
            String confirmPassword = confirmPasswordInputLayout.getEditText().getText().toString().trim();

            // 验证输入不为空
            if (username.isEmpty() || phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 验证两次密码输入是否一致
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            // 验证手机号格式（简单验证11位数字）
            if (!phoneNumber.matches("^\\d{11}$")) {
                Toast.makeText(this, "请输入正确的手机号格式", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建UserDao实例并尝试注册
            UserDao userDao = new UserDao(this);
            boolean success = userDao.register(phoneNumber, username, password);

            if (success) {
                Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
                finish(); // 关闭注册页面，返回登录页面
            } else {
                Toast.makeText(this, "注册失败，该手机号已被注册", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 设置输入框的提示文字和焦点变化效果
     * @param inputLayout 输入框布局
     * @param hint 提示文字
     */
    private void setupInputLayout(TextInputLayout inputLayout, String hint) {
        if (inputLayout != null && inputLayout.getEditText() != null) {
            inputLayout.setHintEnabled(false);
            inputLayout.getEditText().setHint(hint);

            inputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    inputLayout.getEditText().setHint(s == null || s.length() == 0 ? hint : "");
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            inputLayout.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    inputLayout.setHintTextColor(getResources().getColorStateList(R.color.white));
                    inputLayout.setBoxStrokeColor(getResources().getColor(R.color.light_blue_border));
                } else {
                    inputLayout.setHintTextColor(getResources().getColorStateList(android.R.color.darker_gray));
                    inputLayout.setBoxStrokeColor(getResources().getColor(android.R.color.darker_gray));
                }
            });
        }
    }
}