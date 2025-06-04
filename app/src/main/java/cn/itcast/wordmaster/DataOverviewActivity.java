package cn.itcast.wordmaster;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

// MPAndroidChart 导入
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

// 数据库相关导入
import cn.itcast.wordmaster.db.WordDao;
import cn.itcast.wordmaster.entity.Word;

public class DataOverviewActivity extends AppCompatActivity {

    private TextView weekTabText;
    private TextView monthTabText;
    private View weekIndicator;
    private View monthIndicator;
    private LinearLayout chartContainer;
    private TextView todayLearningCount;
    private TextView todayReviewCount;
    private boolean isWeekMode = true;
    private BarChart barChart;
    private WordDao wordDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_data_overview);

        // 初始化WordDao
        wordDao = new WordDao(this);
        
        // 初始化视图
        initViews();
        
        // 获取传入的日期参数
        long selectedDate = getIntent().getLongExtra("SELECTED_DATE", -1);
        
        // 如果有传入日期参数，则加载该日期的数据
        if (selectedDate != -1) {
            loadDataForSelectedDate(selectedDate);
        } else {
            // 否则加载默认的用户学习数据统计
            loadUserLearningStats();
        }
    }

    private void initViews() {
        // 设置返回按钮点击事件
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        
        // 设置刷新按钮点击事件
        ImageView refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(v -> refreshData());
        
        // 初始化Tab相关视图
        weekTabText = findViewById(R.id.weekTabText);
        monthTabText = findViewById(R.id.monthTabText);
        weekIndicator = findViewById(R.id.weekIndicator);
        monthIndicator = findViewById(R.id.monthIndicator);
        chartContainer = findViewById(R.id.chartContainer);
        
        // 设置Tab点击事件
        findViewById(R.id.weekTab).setOnClickListener(v -> switchToWeekMode());
        findViewById(R.id.monthTab).setOnClickListener(v -> switchToMonthMode());
        
        // 初始化底部统计数据视图
        todayLearningCount = findViewById(R.id.todayLearningCount);
        todayReviewCount = findViewById(R.id.todayReviewCount);
        
        // 初始化图表
        setupBarChart();
    }
    
    private void setupBarChart() {
        // 移除chartContainer中的所有视图
        chartContainer.removeAllViews();
        
        // 创建BarChart
        barChart = new BarChart(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        barChart.setLayoutParams(layoutParams);
        
        // 添加到容器
        chartContainer.addView(barChart);
        
        // 配置图表
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.getLegend().setEnabled(false); // 禁用图例，因为我们有自定义图例
        
        // 配置X轴
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);
        
        // 配置Y轴
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);
    }
    
    private void switchToWeekMode() {
        if (!isWeekMode) {
            isWeekMode = true;
            weekTabText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            monthTabText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            weekIndicator.setVisibility(View.VISIBLE);
            monthIndicator.setVisibility(View.INVISIBLE);
            loadChartData();
        }
    }
    
    private void switchToMonthMode() {
        if (isWeekMode) {
            isWeekMode = false;
            weekTabText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            monthTabText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            weekIndicator.setVisibility(View.INVISIBLE);
            monthIndicator.setVisibility(View.VISIBLE);
            loadChartData();
        }
    }
    
    private void refreshData() {
        // 刷新数据
        loadUserLearningStats();
    }

    private void loadUserLearningStats() {
        // 从数据库加载用户学习数据并显示
        // 这里可以加载已掌握单词数、待复习单词数、学习天数等数据
        
        // 加载图表数据
        loadChartData();
        
        // 加载今日学习和复习数据
        loadTodayStats();
    }
    
    private void loadChartData() {
        // 根据当前模式加载图表数据
        if (isWeekMode) {
            loadWeekChartData();
        } else {
            loadMonthChartData();
        }
    }
    
    private void loadWeekChartData() {
        // 获取最近一周的数据
        List<Integer> learningCounts = new ArrayList<>();
        List<String> dateLabels = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.getDefault());
        
        // 计算一周前的日期
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -6);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long weekStart = calendar.getTimeInMillis();
        
        // 计算明天的日期（作为结束日期）
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long weekEnd = calendar.getTimeInMillis();
        
        // 获取一周内每天的学习完成单词数量
        List<Integer> counts = wordDao.getCompletedWordCountsByDateRange(weekStart, weekEnd);
        
        // 生成日期标签
        calendar.setTimeInMillis(weekStart);
        for (int i = 0; i < 7; i++) {
            dateLabels.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // 更新图表
        updateChart(dateLabels, counts);
    }
    
    private void loadMonthChartData() {
        // 获取最近四个月的数据（本月及往前三个月）
        List<String> monthLabels = new ArrayList<>();
        List<Integer> monthlyCounts = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        
        // 当前月份
        String currentMonth = monthFormat.format(calendar.getTime());
        
        // 获取最近四个月的数据
        for (int i = 0; i < 4; i++) {
            // 设置为当月第一天
            if (i > 0) {
                calendar.add(Calendar.MONTH, -1);
            }
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long monthStart = calendar.getTimeInMillis();
            
            // 设置为下月第一天
            Calendar endCalendar = (Calendar) calendar.clone();
            endCalendar.add(Calendar.MONTH, 1);
            long monthEnd = endCalendar.getTimeInMillis();
            
            // 获取当月学习完成的单词总数
            int monthCount = wordDao.getCompletedWordCountForDate(monthStart, monthEnd);
            monthlyCounts.add(0, monthCount); // 添加到列表前端，保持时间顺序
            
            // 添加月份标签
            String monthLabel = new SimpleDateFormat("MM月", Locale.getDefault()).format(calendar.getTime());
            monthLabels.add(0, monthLabel); // 添加到列表前端，保持时间顺序
        }
        
        // 更新图表
        updateChart(monthLabels, monthlyCounts);
    }
    
    private void loadTodayStats() {
        // 获取今日学习统计（correctCount达到3且时间戳是今天的单词数量）
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long todayStart = calendar.getTimeInMillis();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long tomorrowStart = calendar.getTimeInMillis();
        
        // 查询数据库获取今日学习完成的单词数量
        int learningCount = wordDao.getCompletedWordCountForDate(todayStart, tomorrowStart);
        todayLearningCount.setText(String.valueOf(learningCount));
        
        // 当日复习数量设置为0，因为复习功能尚未实现
        todayReviewCount.setText("0");
    }

    private void updateChart(List<String> dateLabels, List<Integer> learningCounts) {
        // 使用MPAndroidChart库更新图表
        List<BarEntry> learningEntries = new ArrayList<>();
        
        // 创建学习数据条目
        for (int i = 0; i < learningCounts.size(); i++) {
            learningEntries.add(new BarEntry(i, learningCounts.get(i)));
        }
        
        // 创建数据集
        BarDataSet learningDataSet = new BarDataSet(learningEntries, "学习");
        learningDataSet.setColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
        learningDataSet.setValueTextSize(10f);
        
        // 创建BarData
        BarData barData = new BarData(learningDataSet);
        barData.setBarWidth(0.6f);
        
        // 设置X轴标签
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        
        // 设置数据并刷新图表
        barChart.setData(barData);
        barChart.invalidate();
    }
    
    /**
     * 加载选中日期的数据
     */
    private void loadDataForSelectedDate(long selectedDate) {
        // 设置标题显示选中的日期
        // 找到顶部标题栏中的TextView（第二个TextView）
        LinearLayout titleBar = (LinearLayout) findViewById(R.id.backButton).getParent();
        TextView titleTextView = (TextView) ((LinearLayout) titleBar).getChildAt(1);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        titleTextView.setText(sdf.format(new Date(selectedDate)) + " 学习数据");
        
        // 计算选中日期的开始和结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long dayStart = calendar.getTimeInMillis();
        
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long dayEnd = calendar.getTimeInMillis();
        
        // 查询选中日期的学习数据
        int learningCount = wordDao.getCompletedWordCountForDate(dayStart, dayEnd);
        
        // 更新今日学习和复习数据显示
        todayLearningCount.setText(String.valueOf(learningCount));
        todayReviewCount.setText("0"); // 复习功能尚未实现
        
        // 加载图表数据（仍然显示周/月视图）
        loadChartData();
        
        // 高亮显示选中日期的数据条
        highlightSelectedDateInChart(selectedDate);
    }
    
    /**
     * 在图表中高亮显示选中日期的数据条
     */
    private void highlightSelectedDateInChart(long selectedDate) {
        if (barChart == null || barChart.getData() == null) {
            return;
        }
        
        // 获取选中日期的日期格式
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDate);
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.getDefault());
        String selectedDateStr = sdf.format(calendar.getTime());
        
        // 获取X轴标签
        XAxis xAxis = barChart.getXAxis();
        if (xAxis.getValueFormatter() instanceof IndexAxisValueFormatter) {
            IndexAxisValueFormatter formatter = (IndexAxisValueFormatter) xAxis.getValueFormatter();
            String[] labels = formatter.getValues();
            
            // 查找选中日期在标签中的索引
            for (int i = 0; i < labels.length; i++) {
                if (labels[i].equals(selectedDateStr)) {
                    // 高亮显示该索引对应的数据条
                    barChart.highlightValue(i, 0);
                    return;
                }
            }
        }
        
        // 如果没有找到匹配的日期，则不高亮显示
        barChart.highlightValues(null);
    }
}