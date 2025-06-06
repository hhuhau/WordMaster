package cn.itcast.wordmaster.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类
 * 负责数据库的创建和版本管理
 */
public class WordMasterDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "wordmaster.db";
    private static final int DATABASE_VERSION = 1;
    private static WordMasterDBHelper instance;
    private SQLiteDatabase db;

    private WordMasterDBHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized WordMasterDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new WordMasterDBHelper(context);
        }
        return instance;
    }

    public synchronized SQLiteDatabase getDatabase() {
        return getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 数据库表已经创建好了，不需要再创建表
        // 这里不执行任何操作
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果需要升级数据库结构，在这里添加相应的逻辑
    }


}