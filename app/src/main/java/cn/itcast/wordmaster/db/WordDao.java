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
     * 获取一组学习单词
     * @param batchSize 每批次单词数量，默认为10
     */
    public List<Word> getLearningBatch(int batchSize) {
        List<Word> batch = new ArrayList<>();
        
        // 直接随机抽取指定数量的correctCount < 3的单词
        String sql = "SELECT * FROM word WHERE correctCount < 3 ORDER BY RANDOM() LIMIT " + batchSize;
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, null);
            while (cursor != null && cursor.moveToNext()) {
                Word word = cursorToWord(cursor);
                batch.add(word);
            }

            // 如果没有正在学习的单词（可能所有单词都已经学完了）
            if (batch.isEmpty()) {
                // 获取新的未学习的单词
                String newSql = "SELECT * FROM word WHERE correctCount = 0 ORDER BY RANDOM() LIMIT " + batchSize;
                Cursor newCursor = db.rawQuery(newSql, null);
                while (newCursor != null && newCursor.moveToNext()) {
                    Word word = cursorToWord(newCursor);
                    batch.add(word);
                }
                newCursor.close();
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
     * 获取一组学习单词（默认10个）
     * 为了保持向后兼容
     */
    public List<Word> getLearningBatch() {
        return getLearningBatch(10);
    }

    /**
     * 检查当前批次是否学习时间过长（超过7天）
     */
    private boolean isLearningTooLong(List<Word> batch) {
        if (batch.isEmpty()) return false;
        
        long currentTime = System.currentTimeMillis();
        long sevenDaysAgo = currentTime - (7 * 24 * 60 * 60 * 1000); // 7天前的时间戳
        
        // 检查第一个单词的最后复习时间
        Word firstWord = batch.get(0);
        String lastReviewed = firstWord.getLastReviewed();
        if (lastReviewed == null) return false;
        
        long lastReviewTime = Long.parseLong(lastReviewed);
        return lastReviewTime < sevenDaysAgo;
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
    public int calcNextDueOffset(int correctCount) {
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
        String[] columns = {
            "wordId", "spelling", "phonetic", "meaning", "bookId",
            "correctCount", "lastReviewed", "nextDueOffset" // 添加这些字段以确保能够正确更新
        };
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
                word.setCorrectCount(cursor.getInt(cursor.getColumnIndexOrThrow("correctCount")));
                word.setLastReviewed(cursor.getString(cursor.getColumnIndexOrThrow("lastReviewed")));
                word.setNextDueOffset(cursor.getInt(cursor.getColumnIndexOrThrow("nextDueOffset")));
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

    /**
     * 获取指定时间范围内学习完成的单词数量
     * 学习完成的定义是：correctCount = 3 且 lastReviewed 在指定时间范围内
     * 注意：只统计首次学会的单词，不包括复习
     * 
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 学习完成的单词数量
     */
    public int getCompletedWordCountForDate(long startTime, long endTime) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM word WHERE correctCount = 3 AND lastReviewed >= ? AND lastReviewed < ?";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(startTime), String.valueOf(endTime)});
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting completed word count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return count;
    }
    
    /**
     * 获取指定时间范围内复习的单词数量
     * 复习的定义是：correctCount > 3 的单词在指定时间范围内进行了复习
     * 注意：correctCount = 3 表示刚学会，不算复习；只有 correctCount > 3 才算复习
     * 
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 复习的单词数量
     */
    public int getReviewedWordCountForDate(long startTime, long endTime) {
        int count = 0;
        
        // 只查询correctCount > 3的单词（已经学会且进行过复习的单词）
        String sql = "SELECT lastReviewed FROM word WHERE correctCount > 3 AND lastReviewed >= ? AND lastReviewed < ?";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(startTime), String.valueOf(endTime)});
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting reviewed word count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return count;
    }
    
    /**
     * 获取指定日期范围内每天学习完成的单词数量
     * 
     * @param startDate 开始日期的时间戳
     * @param endDate 结束日期的时间戳
     * @return 每天学习完成的单词数量列表
     */
    public List<Integer> getCompletedWordCountsByDateRange(long startDate, long endDate) {
        List<Integer> counts = new ArrayList<>();
        
        // 计算天数
        int days = (int) ((endDate - startDate) / (24 * 60 * 60 * 1000));
        
        // 获取每天的学习完成单词数量
        for (int i = 0; i < days; i++) {
            long dayStart = startDate + i * (24 * 60 * 60 * 1000);
            long dayEnd = dayStart + (24 * 60 * 60 * 1000);
            
            int count = getCompletedWordCountForDate(dayStart, dayEnd);
            counts.add(count);
        }
        
        return counts;
    }
    
    /**
     * 获取指定日期范围内每天复习的单词数量
     * 
     * @param startDate 开始日期的时间戳
     * @param endDate 结束日期的时间戳
     * @return 每天复习的单词数量列表
     */
    public List<Integer> getReviewedWordCountsByDateRange(long startDate, long endDate) {
        List<Integer> counts = new ArrayList<>();
        
        // 计算天数
        int days = (int) ((endDate - startDate) / (24 * 60 * 60 * 1000));
        
        // 获取每天的复习单词数量
        for (int i = 0; i < days; i++) {
            long dayStart = startDate + i * (24 * 60 * 60 * 1000);
            long dayEnd = dayStart + (24 * 60 * 60 * 1000);
            
            int count = getReviewedWordCountForDate(dayStart, dayEnd);
            counts.add(count);
        }
        
        return counts;
    }
    
    /**
     * 获取所有单词
     * @return 所有单词列表
     */
    public List<Word> getAllWords() {
        List<Word> wordList = new ArrayList<>();
        String[] columns = {
            "wordId", "spelling", "phonetic", "meaning", "bookId",
            "correctCount", "lastReviewed", "nextDueOffset"
        };
        
        Cursor cursor = null;
        try {
            cursor = db.query("word", columns, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Word word = cursorToWord(cursor);
                    wordList.add(word);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all words: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return wordList;
    }
    
    /**
     * 获取随机单词拼写
     * @return 随机单词的拼写，如果没有单词则返回null
     */
    public String getRandomWordSpelling() {
        String spelling = null;
        String sql = "SELECT spelling FROM word ORDER BY RANDOM() LIMIT 1";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                spelling = cursor.getString(cursor.getColumnIndexOrThrow("spelling"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting random word spelling: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return spelling;
    }
}