package cn.itcast.wordmaster.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.itcast.wordmaster.entity.Word;
import cn.itcast.wordmaster.entity.AnswerType;

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
     * 获取一组学习单词（10个）
     */
    public List<Word> getLearningBatch() {
        List<Word> batch = new ArrayList<>();
        // 优先获取未学习的单词（correctCount = 0）
        String sql = "SELECT * FROM word WHERE correctCount = 0 ORDER BY RANDOM() LIMIT 10";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, null);
            while (cursor != null && cursor.moveToNext()) {
                Word word = cursorToWord(cursor);
                batch.add(word);
            }

            // 如果不足10个，补充需要复习的单词
            if (batch.size() < 10) {
                String supplementSql = "SELECT * FROM word WHERE correctCount > 0 AND correctCount < 3 AND (lastReviewed IS NULL OR nextDueOffset > 0) ORDER BY nextDueOffset ASC LIMIT ?";
                Cursor supplementCursor = db.rawQuery(supplementSql, new String[]{String.valueOf(10 - batch.size())});
                while (supplementCursor != null && supplementCursor.moveToNext()) {
                    Word word = cursorToWord(supplementCursor);
                    batch.add(word);
                }
                supplementCursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting learning batch: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return batch;
    }

    /**
     * 获取随机干扰选项
     */
    public List<String> getRandomMeanings(String correctMeaning, int count) {
        List<String> meanings = new ArrayList<>();
        String sql = "SELECT meaning FROM word WHERE meaning != ? ORDER BY RANDOM() LIMIT ?";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{correctMeaning, String.valueOf(count)});
            while (cursor != null && cursor.moveToNext()) {
                meanings.add(cursor.getString(cursor.getColumnIndexOrThrow("meaning")));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting random meanings: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return meanings;
    }

    /**
     * 更新单词学习状态
     */
    public void updateWordStatus(int wordId, AnswerType type) {
        Word word = getWordById(wordId);
        if (word != null) {
            try {
                switch (type) {
                    case CORRECT:
                    case RECOGNIZE:
                        word.setCorrectCount(word.getCorrectCount() + 1);
                        break;
                    case FUZZY:
                        // 模糊状态保持当前正确次数不变
                        break;
                    case WRONG:
                    case FORGET:
                        word.setCorrectCount(0);
                        break;
                }

                word.setLastReviewed(String.valueOf(System.currentTimeMillis()));
                word.setNextDueOffset(calcNextDueOffset(word.getCorrectCount()));

                String sql = "UPDATE word SET correctCount = ?, lastReviewed = ?, nextDueOffset = ? WHERE wordId = ?";
                db.execSQL(sql, new Object[]{
                    word.getCorrectCount(),
                    word.getLastReviewed(),
                    word.getNextDueOffset(),
                    word.getWordId()
                });
            } catch (Exception e) {
                Log.e(TAG, "Error updating word status: " + e.getMessage());
            }
        }
    }

    /**
     * 计算下次复习间隔
     */
    private int calcNextDueOffset(int correctCount) {
        if (correctCount <= 1) return 3;
        return 3 * (1 << (correctCount - 1));
    }

    /**
     * 将Cursor转换为Word对象
     */
    private Word cursorToWord(Cursor cursor) {
        Word word = new Word();
        word.setWordId(cursor.getInt(cursor.getColumnIndexOrThrow("wordId")));
        word.setSpelling(cursor.getString(cursor.getColumnIndexOrThrow("spelling")));
        word.setPhonetic(cursor.getString(cursor.getColumnIndexOrThrow("phonetic")));
        word.setMeaning(cursor.getString(cursor.getColumnIndexOrThrow("meaning")));
        word.setBookId(cursor.getString(cursor.getColumnIndexOrThrow("bookId")));
        word.setCorrectCount(cursor.getInt(cursor.getColumnIndexOrThrow("correctCount")));
        word.setLastReviewed(cursor.getString(cursor.getColumnIndexOrThrow("lastReviewed")));
        word.setNextDueOffset(cursor.getInt(cursor.getColumnIndexOrThrow("nextDueOffset")));
        return word;
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
    /**
     * 更新单词的nextDueOffset值
     */
    public void updateOffset(int wordId, int offset) {
        try {
            String sql = "UPDATE word SET nextDueOffset = ? WHERE wordId = ?";
            db.execSQL(sql, new Object[]{offset, wordId});
        } catch (Exception e) {
            Log.e(TAG, "Error updating word offset: " + e.getMessage());
        }
    }

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