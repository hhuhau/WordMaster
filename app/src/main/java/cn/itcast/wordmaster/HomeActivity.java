package cn.itcast.wordmaster;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private MaterialButton learnButton;
    private MaterialButton reviewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 初始化视图
        initViews();
        // 设置底部导航栏
        setupBottomNavigation();
    }

    private void initViews() {
        learnButton = findViewById(R.id.btn_learn);
        reviewButton = findViewById(R.id.btn_review);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // 默认选中首页
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                // TODO: 切换到个人中心页面
                return true;
            }
            return false;
        });
    }
}