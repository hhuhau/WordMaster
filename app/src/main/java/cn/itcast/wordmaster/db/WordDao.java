package cn.itcast.wordmaster.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.itcast.wordmaster.entity.Word;

/**
 * 单词数据操作类
 * 负责word表的增删改查操作
 */
public class WordDao {
    private static final String TAG = "WordDao";
    private SQLiteDatabase db;

    public WordDao(Context context) {
        this.db = WordMasterDBHelper.getInstance(context).getDatabase();
    }

    /**
     * 根据词典ID获取单词列表
     * @param bookId 词典ID
     * @return 单词列表
     */
    public List<Word> getWordsByBookId(String bookId) {
        List<Word> wordList = new ArrayList<>();
        String[] columns = {"wordId", "spelling", "phonetic", "meaning", "bookId"};
        String selection = "bookId = ?";
        String[] selectionArgs = {bookId};
        String orderBy = "wordId ASC";

        Cursor cursor = null;
        try {
            cursor = db.query("word", columns, selection, selectionArgs, null, null, orderBy);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Word word = new Word();
                    word.setWordId(cursor.getInt(cursor.getColumnIndexOrThrow("wordId")));
                    word.setSpelling(cursor.getString(cursor.getColumnIndexOrThrow("spelling")));
                    word.setPhonetic(cursor.getString(cursor.getColumnIndexOrThrow("phonetic")));
                    word.setMeaning(cursor.getString(cursor.getColumnIndexOrThrow("meaning")));
                    word.setBookId(cursor.getString(cursor.getColumnIndexOrThrow("bookId")));
                    wordList.add(word);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting words by bookId: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return wordList;
    }

    /**
     * 根据单词ID获取单词
     * @param wordId 单词ID
     * @return 单词对象
     */
    public Word getWordById(int wordId) {
        String[] columns = {"wordId", "spelling", "phonetic", "meaning", "bookId"};
        String selection = "wordId = ?";
        String[] selectionArgs = {String.valueOf(wordId)};

        Cursor cursor = null;
        try {
            cursor = db.query("word", columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                Word word = new Word();
                word.setWordId(cursor.getInt(cursor.getColumnIndexOrThrow("wordId")));
                word.setSpelling(cursor.getString(cursor.getColumnIndexOrThrow("spelling")));
                word.setPhonetic(cursor.getString(cursor.getColumnIndexOrThrow("phonetic")));
                word.setMeaning(cursor.getString(cursor.getColumnIndexOrThrow("meaning")));
                word.setBookId(cursor.getString(cursor.getColumnIndexOrThrow("bookId")));
                return word;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting word by id: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    /**
     * 搜索单词
     * @param keyword 关键词
     * @return 单词列表
     */
    public List<Word> searchWords(String keyword) {
        List<Word> wordList = new ArrayList<>();
        String[] columns = {"wordId", "spelling", "phonetic", "meaning", "bookId"};
        String selection = "spelling LIKE ? OR meaning LIKE ?";
        String[] selectionArgs = {"%" + keyword + "%", "%" + keyword + "%"};

        Cursor cursor = null;
        try {
            cursor = db.query("word", columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Word word = new Word();
                    word.setWordId(cursor.getInt(cursor.getColumnIndexOrThrow("wordId")));
                    word.setSpelling(cursor.getString(cursor.getColumnIndexOrThrow("spelling")));
                    word.setPhonetic(cursor.getString(cursor.getColumnIndexOrThrow("phonetic")));
                    word.setMeaning(cursor.getString(cursor.getColumnIndexOrThrow("meaning")));
                    word.setBookId(cursor.getString(cursor.getColumnIndexOrThrow("bookId")));
                    wordList.add(word);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching words: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return wordList;
    }
}