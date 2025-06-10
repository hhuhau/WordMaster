package cn.itcast.wordmaster.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import cn.itcast.wordmaster.db.WordDao;
import cn.itcast.wordmaster.R;
import cn.itcast.wordmaster.entity.Word;
import cn.itcast.wordmaster.entity.Wordbook;
import cn.itcast.wordmaster.db.WordbookDao;

public class HomeFragment extends Fragment {
    private MaterialButton learnButton;
    private MaterialButton reviewButton;
    private TextView wordTextView;
    private TextView chineseTextView;
    // 移除TextView引用，数字直接显示在按钮内
    private WordDao wordDao;
    private WordbookDao wordbookDao;
    private String currentWordSpelling;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // 初始化视图
        learnButton = view.findViewById(R.id.btn_learn);
        reviewButton = view.findViewById(R.id.btn_review);
        wordTextView = view.findViewById(R.id.tv_word);
        chineseTextView = view.findViewById(R.id.tv_chinese);
        // TextView已移除，数字直接显示在按钮内
        
        // 初始化数据库
        wordDao = new WordDao(getContext());
        wordbookDao = new WordbookDao(getContext());
        
        // 设置按钮点击事件
        learnButton.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new LearnFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
        
        reviewButton.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ReviewFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
        
        // 加载随机单词
        loadRandomWord();
        
        // 更新计数
        updateCounts();
        
        return view;
    }
    
    private void loadRandomWord() {
        // 获取并显示随机单词
        String randomWord = wordDao.getRandomWordSpelling();
        if (randomWord != null) {
            currentWordSpelling = randomWord;
            // 将单词首字母转换为大写
            String capitalizedWord = randomWord.substring(0, 1).toUpperCase() + randomWord.substring(1);
            wordTextView.setText(capitalizedWord);
            
            // 初始隐藏中文释义
            chineseTextView.setVisibility(View.GONE);
            
            // 设置点击事件切换显示/隐藏中文释义
            wordTextView.setOnClickListener(v -> {
                if (chineseTextView.getVisibility() == View.VISIBLE) {
                    // 如果当前显示中文释义，则隐藏
                    chineseTextView.setVisibility(View.GONE);
                } else {
                    // 如果当前隐藏中文释义，则显示
                    Word word = wordDao.getWordBySpelling(currentWordSpelling);
                    if (word != null && word.getMeaning() != null) {
                        chineseTextView.setText(word.getMeaning());
                        chineseTextView.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }
    
    private void updateCounts() {
        String currentWordbookId = wordbookDao.getCurrentWordbookId(getContext());
        Wordbook currentWordbook = wordbookDao.getWordbookById(currentWordbookId);
        if (currentWordbook != null) {
            // 计算学习数量：总单词数 - 已学会的单词数（correctCount >= 3）
            int learnedCount = getLearnedWordsCount(currentWordbook.getBookId());
            int learnCount = currentWordbook.getWordCount() - learnedCount;
            
            // 计算复习数量：correctCount >= 3 且根据nextDueOffset到期的单词
            int reviewCount = getReviewCount(currentWordbook.getBookId());
            
            // 更新按钮文本，数字显示在文字下方，使用更小的灰色字体，并添加适当间距
              String learnText = "LEARN<br/><small><small><font color='#001E78' style='margin-top:2px; display:inline-block;'>" + learnCount + "</font></small></small>";
              String reviewText = "REVIEW<br/><small><small><font color='#001E78' style='margin-top:2px; display:inline-block;'>" + reviewCount + "</font></small></small>";
             
             learnButton.setText(android.text.Html.fromHtml(learnText, android.text.Html.FROM_HTML_MODE_LEGACY));
             reviewButton.setText(android.text.Html.fromHtml(reviewText, android.text.Html.FROM_HTML_MODE_LEGACY));
        }
    }
    
    /**
     * 获取已学单词数量（correctCount达到3的单词）
     */
    private int getLearnedWordsCount(String wordbookId) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM word WHERE correctCount >= 3 AND bookId = ?";
        
        android.database.Cursor cursor = null;
        try {
            cursor = wordDao.getDatabase().rawQuery(sql, new String[]{wordbookId});
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return count;
    }
    
    /**
     * 获取需要复习的单词数量（有lastReviewed记录且根据nextDueOffset到期的单词）
     */
    private int getReviewCount(String wordbookId) {
        int count = 0;
        long currentTime = System.currentTimeMillis();
        
        // 查询所有有复习记录的单词
        String sql = "SELECT * FROM word WHERE lastReviewed IS NOT NULL AND lastReviewed != '' AND bookId = ?";
        
        android.database.Cursor cursor = null;
        try {
            cursor = wordDao.getDatabase().rawQuery(sql, new String[]{wordbookId});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String lastReviewed = cursor.getString(cursor.getColumnIndexOrThrow("lastReviewed"));
                    int nextDueOffset = cursor.getInt(cursor.getColumnIndexOrThrow("nextDueOffset"));
                    
                    try {
                        long lastReviewTime = Long.parseLong(lastReviewed);
                        // 计算距离上次复习的天数
                        long daysSinceLastReview = (currentTime - lastReviewTime) / (24 * 60 * 60 * 1000);
                        
                        // 如果距离上次复习的天数 >= nextDueOffset，则需要复习
                        if (daysSinceLastReview >= nextDueOffset) {
                            count++;
                        }
                    } catch (NumberFormatException e) {
                        // 如果lastReviewed格式错误，跳过这个单词
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return count;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次返回主页时更新计数
        updateCounts();
    }
    
    /**
     * 公开方法，供其他Fragment调用以更新计数
     */
    public void refreshCounts() {
        updateCounts();
    }
}