package cn.itcast.wordmaster;

import android.app.Application;
import cn.itcast.wordmaster.db.WordMasterDBHelper;

public class WordMasterApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化数据库连接
        WordMasterDBHelper.getInstance(this);
    }
}