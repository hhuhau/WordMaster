package cn.itcast.wordmaster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.CalendarView;
import android.graphics.Color;
import android.widget.Toast;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import cn.itcast.wordmaster.db.WordDao;

public class DataCalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private WordDao wordDao;
    private static final int TODAY_MARKER_COLOR = Color.parseColor("#E6F4FF"); // 浅蓝色背景色

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_data_calendar);

        // 设置返回按钮点击事件
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // 初始化WordDao
        wordDao = new WordDao(this);
        
        // 初始化日历视图
        calendarView = findViewById(R.id.calendarView);
        
        // 设置日历初始显示为当前日期
        calendarView.setDate(System.currentTimeMillis(), false, true);

        // 设置日历日期变化监听器
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // 月份从0开始，所以需要+1
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            long dateInMillis = selectedDate.getTimeInMillis();
            
            // 检查选中日期是否有学习记录
            boolean hasLearningRecord = checkDateLearningStatus(dateInMillis);
            
            // 只有当有学习记录时才跳转到详情页面
            if (hasLearningRecord) {
                navigateToDetailPage(dateInMillis);
            }
        });

        // 标记当天日期
        markTodayDate();
    }

    /**
     * 检查选中日期是否有学习记录
     * @return 是否有学习记录
     */
    private boolean checkDateLearningStatus(long dateInMillis) {
        // 计算当天开始和结束时间戳
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();
        
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endTime = calendar.getTimeInMillis();
        
        // 查询当天学习和复习记录
        int learningCount = wordDao.getCompletedWordCountForDate(startTime, endTime);
        int reviewCount = wordDao.getReviewedWordCountForDate(startTime, endTime);
        
        // 如果有学习或复习记录，显示提示
        if (learningCount > 0 || reviewCount > 0) {
            String message = "当日";
            if (learningCount > 0) {
                message += "学习了 " + learningCount + " 个单词";
            }
            if (reviewCount > 0) {
                if (learningCount > 0) {
                    message += "，";
                }
                message += "复习了 " + reviewCount + " 个单词";
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this, "当日无学习记录", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * 标记当天日期
     */
    private void markTodayDate() {
        try {
            // 获取当前日期
            Calendar today = Calendar.getInstance();
            
            // 使用反射获取CalendarView的内部实现
            if (calendarView.getChildCount() > 0) {
                View monthView = calendarView.getChildAt(0);
                if (monthView instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) monthView;
                    // 遍历所有子视图，查找日期单元格
                    for (int i = 0; i < viewGroup.getChildCount(); i++) {
                        View cell = viewGroup.getChildAt(i);
                        if (cell instanceof TextView) {
                            TextView textView = (TextView) cell;
                            String dayText = textView.getText().toString();
                            // 如果找到了对应的当天日期单元格
                            if (!dayText.isEmpty() && Integer.parseInt(dayText) == today.get(Calendar.DAY_OF_MONTH)) {
                                // 设置背景色为圆形
                                GradientDrawable shape = new GradientDrawable();
                                shape.setShape(GradientDrawable.OVAL);
                                shape.setColor(TODAY_MARKER_COLOR);
                                textView.setBackground(shape);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到详情页面
     */
    private void navigateToDetailPage(long dateInMillis) {
        // 创建Intent
        Intent intent = new Intent(this, DataOverviewActivity.class);
        
        // 传递选中的日期参数
        intent.putExtra("SELECTED_DATE", dateInMillis);
        
        // 启动详情页面
        startActivity(intent);
    }
}