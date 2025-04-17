package cn.itcast.wordmaster;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;
import android.text.TextWatcher;
import android.text.Editable;
import cn.itcast.wordmaster.db.UserDao;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextInputLayout phoneInputLayout = findViewById(R.id.phoneInputLayout);
        TextInputLayout passwordInputLayout = findViewById(R.id.passwordInputLayout);
        TextView registerLink = findViewById(R.id.registerLink);

        setupInputLayout(phoneInputLayout, "请输入手机号");
        setupInputLayout(passwordInputLayout, "请输入密码");
        // 我要测试一下
        // 设置快速注册链接的点击事件
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 获取登录按钮并设置点击事件
        MaterialButton loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            // 获取用户输入
            String phoneNumber = phoneInputLayout.getEditText().getText().toString().trim();
            String password = passwordInputLayout.getEditText().getText().toString().trim();

            // 验证输入不为空
            if (phoneNumber.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 验证手机号格式（简单验证11位数字）
            if (!phoneNumber.matches("^\\d{11}$")) {
                Toast.makeText(this, "请输入正确的手机号格式", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建UserDao实例并验证登录
            UserDao userDao = new UserDao(this);
            boolean success = userDao.login(phoneNumber, password);

            if (success) {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                // 跳转到单词列表页面，并传递词典ID
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish(); // 结束登录页面
            } else {
                Toast.makeText(this, "手机号或密码错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

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