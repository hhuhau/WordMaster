package cn.itcast.wordmaster.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.itcast.wordmaster.entity.Wordbook;

/**
 * 词书数据操作类
 * 负责wordbook表的增删改查操作
 */
public class WordbookDao {
    private static final String TAG = "WordbookDao";
    private SQLiteDatabase db;

    public WordbookDao(Context context) {
        this.db = WordMasterDBHelper.getInstance(context).getDatabase();
    }

    /**
     * 获取所有词书
     * @return 词书列表
     */
    public List<Wordbook> getAllWordbooks() {
        List<Wordbook> wordbookList = new ArrayList<>();
        String[] columns = {"bookId", "bookName", "description", "wordCount", "difficulty", "coverImageUrl"};
        String orderBy = "bookId ASC";

        Cursor cursor = null;
        try {
            cursor = db.query("wordbook", columns, null, null, null, null, orderBy);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Wordbook wordbook = cursorToWordbook(cursor);
                    wordbookList.add(wordbook);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all wordbooks: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return wordbookList;
    }

    /**
     * 根据ID获取词书
     * @param bookId 词书ID
     * @return 词书对象
     */
    public Wordbook getWordbookById(String bookId) {
        String[] columns = {"bookId", "bookName", "description", "wordCount", "difficulty", "coverImageUrl"};
        String selection = "bookId = ?";
        String[] selectionArgs = {bookId};

        Cursor cursor = null;
        try {
            cursor = db.query("wordbook", columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToWordbook(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting wordbook by id: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 获取当前正在学习的词书ID
     * 默认返回第一本词书
     * @return 词书ID
     */
    public String getCurrentWordbookId(Context context) {
        // 从SharedPreferences获取当前词书ID
        android.content.SharedPreferences sharedPreferences = 
            context.getSharedPreferences("study_settings", Context.MODE_PRIVATE);
        return sharedPreferences.getString("current_wordbook_id", "1"); // 默认返回ID为1的词书
    }

    /**
     * 设置当前正在学习的词书ID
     * @param context 上下文
     * @param bookId 词书ID
     */
    public void setCurrentWordbookId(Context context, String bookId) {
        android.content.SharedPreferences sharedPreferences = 
            context.getSharedPreferences("study_settings", Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("current_wordbook_id", bookId);
        editor.apply();
    }

    /**
     * 将Cursor转换为Wordbook对象
     */
    private Wordbook cursorToWordbook(Cursor cursor) {
        Wordbook wordbook = new Wordbook();
        wordbook.setBookId(cursor.getString(cursor.getColumnIndexOrThrow("bookId")));
        wordbook.setBookName(cursor.getString(cursor.getColumnIndexOrThrow("bookName")));
        wordbook.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        wordbook.setWordCount(cursor.getInt(cursor.getColumnIndexOrThrow("wordCount")));
        wordbook.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow("difficulty")));
        wordbook.setCoverImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("coverImageUrl")));
        return wordbook;
    }
}