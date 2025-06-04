package cn.itcast.wordmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MyDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.fragment_my_data);

        // 设置返回按钮点击事件
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // 设置概览选项点击事件
        LinearLayout overviewOption = findViewById(R.id.overview_option);
        overviewOption.setOnClickListener(v -> {
            Intent intent = new Intent(this, DataOverviewActivity.class);
            startActivity(intent);
        });

        // 设置日历选项点击事件
        LinearLayout calendarOption = findViewById(R.id.calendar_option);
        calendarOption.setOnClickListener(v -> {
            Intent intent = new Intent(this, DataCalendarActivity.class);
            startActivity(intent);
        });
    }
}