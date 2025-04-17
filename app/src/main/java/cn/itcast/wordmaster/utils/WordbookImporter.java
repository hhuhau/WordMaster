package cn.itcast.wordmaster.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cn.itcast.wordmaster.db.WordMasterDBHelper;

public class WordbookImporter {
    private static final String TAG = "WordbookImporter";
    private Context context;
    private WordMasterDBHelper dbHelper;

    public WordbookImporter(Context context) {
        this.context = context;
        this.dbHelper = WordMasterDBHelper.getInstance(context);
    }

    public void importWordbook(String wordbookJsonFileName, String wordsJsonFileName) {
        try {
            // 导入词典信息
            String wordbookJson = readJsonFromAssets(wordbookJsonFileName);
            JSONObject wordbook = new JSONObject(wordbookJson);
            importWordbookData(wordbook);

            // 导入单词信息
            String wordsJson = readJsonFromAssets(wordsJsonFileName);
            JSONArray words = new JSONArray(wordsJson);
            importWordsData(words);
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Error importing wordbook: " + e.getMessage());
        }
    }

    private void importWordbookData(JSONObject wordbook) throws JSONException {
        SQLiteDatabase db = dbHelper.getDatabase();
        ContentValues values = new ContentValues();

        values.put("bookId", wordbook.getString("bookId"));
        values.put("bookName", wordbook.getString("bookName"));
        values.put("description", wordbook.getString("description"));
        values.put("wordCount", wordbook.getInt("wordCount"));
        values.put("difficulty", wordbook.getString("difficulty"));
        values.put("coverImageUrl", wordbook.getString("coverImageUrl"));

        db.insert("wordbook", null, values);
    }

    private void importWordsData(JSONArray words) throws JSONException {
        SQLiteDatabase db = dbHelper.getDatabase();
        
        for (int i = 0; i < words.length(); i++) {
            JSONObject word = words.getJSONObject(i);
            ContentValues values = new ContentValues();

            values.put("wordId", word.getInt("wordId"));
            values.put("spelling", word.getString("spelling"));
            values.put("phonetic", word.getString("phonetic"));
            
            // 将meanings数组转换为字符串
            JSONArray meanings = word.getJSONArray("meanings");
            StringBuilder meaningsStr = new StringBuilder();
            for (int j = 0; j < meanings.length(); j++) {
                if (j > 0) meaningsStr.append("\n");
                meaningsStr.append(meanings.getString(j));
            }
            values.put("meaning", meaningsStr.toString());
            values.put("bookId", word.getString("bookId"));

            db.insert("word", null, values);
        }
    }

    private String readJsonFromAssets(String fileName) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = context.getAssets().open(fileName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }

        bufferedReader.close();
        return stringBuilder.toString();
    }
}