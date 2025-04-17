package cn.itcast.wordmaster;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.wordmaster.WordDao;

public class HomeActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private MaterialButton learnButton;
    private MaterialButton reviewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
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
        
        // 获取并显示随机单词
        TextView wordTextView = findViewById(R.id.tv_word);
        WordDao wordDao = new WordDao(this);
        String randomWord = wordDao.getRandomWordSpelling();
        if (randomWord != null) {
            // 将单词首字母转换为大写
            String capitalizedWord = randomWord.substring(0, 1).toUpperCase() + randomWord.substring(1);
            wordTextView.setText(capitalizedWord);
        }
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