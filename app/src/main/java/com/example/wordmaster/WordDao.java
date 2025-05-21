package com.example.wordmaster;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WordDao {
    private static final String TAG = "WordDao";
    private SQLiteDatabase db;

    public WordDao(Context context) {
        this.db = cn.itcast.wordmaster.db.WordMasterDBHelper.getInstance(context).getDatabase();
    }

    public String getRandomWordSpelling() {
        String spelling = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT spelling FROM word WHERE bookId = 1 ORDER BY RANDOM() LIMIT 1", null);
            if (cursor != null && cursor.moveToFirst()) {
                spelling = cursor.getString(cursor.getColumnIndexOrThrow("spelling"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting random word: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return spelling;
    }
}