package cn.itcast.wordmaster.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.LineBackgroundSpan;
import android.util.AttributeSet;
import android.widget.CalendarView;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 自定义日历装饰器，用于标记有学习记录的日期
 * 注意：由于Android原生CalendarView的限制，此类仅提供接口，
 * 实际标记功能需要在实现中通过其他方式处理
 */
public class CalendarDecorator {
    
    private Set<Long> markedDates = new HashSet<>();
    private int markedColor = Color.parseColor("#E6F4FF"); // 浅蓝色背景色
    
    /**
     * 添加需要标记的日期
     * @param dateInMillis 日期的毫秒时间戳
     */
    public void addMarkedDate(long dateInMillis) {
        // 标准化时间戳到当天的0点
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        markedDates.add(calendar.getTimeInMillis());
    }
    
    /**
     * 检查日期是否被标记
     * @param dateInMillis 日期的毫秒时间戳
     * @return 是否被标记
     */
    public boolean isDateMarked(long dateInMillis) {
        // 标准化时间戳到当天的0点
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        return markedDates.contains(calendar.getTimeInMillis());
    }
    
    /**
     * 清除所有标记
     */
    public void clearMarkedDates() {
        markedDates.clear();
    }
    
    /**
     * 设置标记颜色
     * @param color 颜色值
     */
    public void setMarkedColor(int color) {
        this.markedColor = color;
    }
    
    /**
     * 获取标记颜色
     * @return 颜色值
     */
    public int getMarkedColor() {
        return markedColor;
    }
}