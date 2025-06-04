package cn.itcast.wordmaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StudySettingsActivity extends AppCompatActivity {

    private TextView tvLearnCount;
    private TextView tvReviewCount;
    private TextView tvChangeWordbook;
    
    private LinearLayout layoutLearnCount;
    private LinearLayout layoutReviewCount;
    
    private SharedPreferences sharedPreferences;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_study_settings);

        // 初始化视图
        initViews();
        
        // 设置返回按钮点击事件
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvLearnCount = findViewById(R.id.tv_learn_count);
        tvReviewCount = findViewById(R.id.tv_review_count);
        tvChangeWordbook = findViewById(R.id.tv_change_wordbook);
        layoutLearnCount = findViewById(R.id.layout_learn_count);
        layoutReviewCount = findViewById(R.id.layout_review_count);
        
        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences("study_settings", MODE_PRIVATE);
        
        // 从SharedPreferences加载设置
        loadSettings();
        
        // 设置点击事件
        layoutLearnCount.setOnClickListener(v -> showCountSelector("learn_count", "每组学习单词量"));
        layoutReviewCount.setOnClickListener(v -> showCountSelector("review_count", "每组复习单词量"));
        
        // 设置更换词书点击事件
        tvChangeWordbook.setOnClickListener(v -> {
            Intent intent = new Intent(StudySettingsActivity.this, WordbookListActivity.class);
            startActivity(intent);
        });
    }

    private void loadSettings() {
        // 从SharedPreferences加载用户设置
        int learnCount = sharedPreferences.getInt("learn_count", 10);
        int reviewCount = sharedPreferences.getInt("review_count", 10);
        
        tvLearnCount.setText(learnCount + "个");
        tvReviewCount.setText(reviewCount + "个");
    }
    
    /**
     * 显示数量选择器
     * @param settingKey 设置项的键
     * @param title 选择器标题
     */
    private void showCountSelector(String settingKey, String title) {
        // 关闭已存在的弹窗
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        
        // 创建弹窗视图
        View popupView = LayoutInflater.from(this).inflate(R.layout.layout_count_selector, null);
        
        // 设置标题
        TextView tvTitle = popupView.findViewById(R.id.tv_selector_title);
        tvTitle.setText(title);
        
        // 创建弹窗
        popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );
        
        // 设置背景以便弹窗外点击可关闭
        popupWindow.setBackgroundDrawable(getDrawable(android.R.drawable.dialog_holo_light_frame));
        popupWindow.setOutsideTouchable(true);
        
        // 设置选项点击事件
        setupCountOptionClickListener(popupView, R.id.tv_count_5, settingKey, 5);
        setupCountOptionClickListener(popupView, R.id.tv_count_10, settingKey, 10);
        setupCountOptionClickListener(popupView, R.id.tv_count_15, settingKey, 15);
        setupCountOptionClickListener(popupView, R.id.tv_count_20, settingKey, 20);
        
        // 显示弹窗在底部
        View rootView = findViewById(android.R.id.content);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }
    
    /**
     * 设置数量选项的点击事件
     */
    private void setupCountOptionClickListener(View popupView, int viewId, String settingKey, int count) {
        TextView tvOption = popupView.findViewById(viewId);
        tvOption.setOnClickListener(v -> {
            // 保存设置
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(settingKey, count);
            editor.apply();
            
            // 更新UI
            if ("learn_count".equals(settingKey)) {
                tvLearnCount.setText(count + "个");
            } else if ("review_count".equals(settingKey)) {
                tvReviewCount.setText(count + "个");
            }
            
            // 关闭弹窗
            popupWindow.dismiss();
        });
    }
}