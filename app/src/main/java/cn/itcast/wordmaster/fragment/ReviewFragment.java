package cn.itcast.wordmaster.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import cn.itcast.wordmaster.R;
import cn.itcast.wordmaster.db.WordDao;
import cn.itcast.wordmaster.entity.AnswerType;
import cn.itcast.wordmaster.entity.Word;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class ReviewFragment extends Fragment {
    private static final int COLOR_CORRECT = 0xFFC8E6C9; // 更浅的绿色
    private static final int COLOR_WRONG = 0xFFFFCDD2; // 更浅的红色

    // 拼写阶段UI组件
    private TextView progressTextView;
    private EditText spellingEditText;
    private TextView meaningTextView;
    private TextView correctAnswerTextView;
    private ImageView wrongButton;
    private ImageView rightButton;
    private Button nextButton;
    private LinearLayout spellingInputLayout;

    // 完成页面UI组件
    private LinearLayout completionLayout;
    private TextView completionTextView;
    private Button nextBatchButton;
    private Button finishButton;

    private WordDao wordDao;
    private List<Word> reviewBatch;
    private int currentIndex = 0;
    private Word currentWord;
    private int batchSize;
    private SharedPreferences reviewCache; // 复习进度缓存

    private List<Word> failedWords = new ArrayList<>(); // 记录拼写错误的单词

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        // 初始化拼写阶段UI组件
        initSpellingUI(view);

        // 初始化完成页面UI组件
        initCompletionUI(view);

        // 初始化WordDao和缓存
        wordDao = new WordDao(requireContext());
        reviewCache = requireActivity().getSharedPreferences("review_cache", Context.MODE_PRIVATE);

        // 尝试恢复复习进度，如果没有缓存则加载新批次
        if (!restoreReviewProgress()) {
            loadReviewBatch();
        }

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 只有在未完成时才保存进度
        if (completionLayout.getVisibility() != View.VISIBLE) {
            saveReviewProgress();
        }
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        // 只有在未完成时才保存进度
        if (completionLayout.getVisibility() != View.VISIBLE) {
            saveReviewProgress();
        }
    }

    private void initSpellingUI(View view) {
        ImageView backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        progressTextView = view.findViewById(R.id.progressTextView);
        spellingEditText = view.findViewById(R.id.spellingEditText);
        meaningTextView = view.findViewById(R.id.meaningTextView);
        correctAnswerTextView = view.findViewById(R.id.correctAnswerTextView);
        wrongButton = view.findViewById(R.id.wrongButton);
        rightButton = view.findViewById(R.id.rightButton);
        nextButton = view.findViewById(R.id.nextButton);
        spellingInputLayout = view.findViewById(R.id.spellingInputLayout);

        // 设置按钮点击事件
        wrongButton.setOnClickListener(v -> onWrongButtonClick());
        rightButton.setOnClickListener(v -> onRightButtonClick());
        nextButton.setOnClickListener(v -> onNextButtonClick());

        // 监听输入框文本变化，输入时改变文本颜色为黑色
        spellingEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                spellingEditText.setTextColor(Color.BLACK);
                correctAnswerTextView.setVisibility(View.GONE);
            }
        });
    }



    private void initCompletionUI(View view) {
        completionLayout = view.findViewById(R.id.completionLayout);
        completionTextView = view.findViewById(R.id.completionTextView);
        nextBatchButton = view.findViewById(R.id.nextBatchButton);
        finishButton = view.findViewById(R.id.finishButton);

        // 注意：按钮点击事件现在在showCompletionPage方法中动态设置
    }

    private void loadReviewBatch() {
        // 从SharedPreferences获取复习单词量设置
        SharedPreferences prefs = requireActivity().getSharedPreferences("study_settings", android.content.Context.MODE_PRIVATE);
        batchSize = prefs.getInt("review_count", 10); // 默认为10个

        // 获取需要复习的单词
        reviewBatch = getReviewWords(batchSize);

        if (reviewBatch.isEmpty()) {
            Toast.makeText(requireContext(), "没有需要复习的单词", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        // 重置状态
        currentIndex = 0;
        failedWords.clear();
        
        // 显示第一个单词
        showCurrentWord();
    }

    private List<Word> getReviewWords(int count) {
        // 获取correctCount >= 3且到期的单词
        List<Word> words = new ArrayList<>();
        List<Word> allWords = wordDao.getAllWords();
        long currentTime = System.currentTimeMillis();

        for (Word word : allWords) {
            if (word.getCorrectCount() >= 3) {
                String lastReviewed = word.getLastReviewed();
                if (lastReviewed != null && !lastReviewed.isEmpty()) {
                    long lastReviewTime = Long.parseLong(lastReviewed);
                    int nextDueOffset = word.getNextDueOffset();
                    // 检查是否到期复习（单位：天）
                    long daysSinceLastReview = (currentTime - lastReviewTime) / (24 * 60 * 60 * 1000);
                    if (daysSinceLastReview >= nextDueOffset) {
                        words.add(word);
                        if (words.size() >= count) {
                            break;
                        }
                    }
                }
            }
        }

        // 如果到期的单词不足，添加correctCount = 3但未到期的单词
        if (words.size() < count) {
            for (Word word : allWords) {
                if (word.getCorrectCount() == 3 && !words.contains(word)) {
                    words.add(word);
                    if (words.size() >= count) {
                        break;
                    }
                }
            }
        }

        return words;
    }



    private void showCurrentWord() {
        if (currentIndex >= reviewBatch.size()) {
            showCompletionPage();
            return;
        }

        currentWord = reviewBatch.get(currentIndex);

        // 更新进度显示
        progressTextView.setText((currentIndex + 1) + "/" + reviewBatch.size());

        // 只显示拼写界面
        showSpellingUI();
    }

    private void showSpellingUI() {
        // 显示拼写界面
        try {
            spellingEditText.setText("");
            spellingEditText.setTextColor(Color.BLACK);
            meaningTextView.setText(currentWord.getMeaning());
            correctAnswerTextView.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);

            // 显示拼写相关控件
            spellingEditText.setVisibility(View.VISIBLE);
            meaningTextView.setVisibility(View.VISIBLE);
            wrongButton.setVisibility(View.VISIBLE);
            rightButton.setVisibility(View.VISIBLE);

            // 隐藏完成页面
            completionLayout.setVisibility(View.GONE);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "显示拼写界面时出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    private void onWrongButtonClick() {
        // 显示正确答案
        correctAnswerTextView.setText(currentWord.getSpelling());
        correctAnswerTextView.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);

        // 将单词添加到失败列表
        if (!failedWords.contains(currentWord)) {
            failedWords.add(currentWord);
        }
    }

    private void onRightButtonClick() {
        String userInput = spellingEditText.getText().toString().trim();
        
        if (userInput.equalsIgnoreCase(currentWord.getSpelling())) {
            // 拼写正确，correctCount加一
            updateWordStatus(currentWord, true);
            
            // 显示正确提示 - 字体变绿色
            spellingEditText.setTextColor(Color.GREEN);
        } else {
            // 拼写错误，字体变红色并显示正确答案
            spellingEditText.setTextColor(Color.RED);
            correctAnswerTextView.setText(currentWord.getSpelling());
            correctAnswerTextView.setVisibility(View.VISIBLE);
            
            // 将单词添加到失败列表
            if (!failedWords.contains(currentWord)) {
                failedWords.add(currentWord);
            }
        }
        
        // 延迟一下再显示下一个单词
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            spellingEditText.setTextColor(Color.BLACK);
            spellingEditText.setText("");
            correctAnswerTextView.setVisibility(View.GONE);
            currentIndex++;
            showCurrentWord();
        }, 1500);
        
        // 保存复习进度
        saveReviewProgress();
    }

    private void updateWordStatus(Word word, boolean isCorrect) {
        if (isCorrect) {
            // 正确时增加correctCount，最多到4
            if (word.getCorrectCount() < 4) {
                word.setCorrectCount(word.getCorrectCount() + 1);
            }
            
            // 更新最后复习时间
            word.setLastReviewed(String.valueOf(System.currentTimeMillis()));
            
            // 计算下次复习时间偏移
            word.setNextDueOffset(wordDao.calcNextDueOffset(word.getCorrectCount()));
            
            // 更新数据库
            wordDao.updateWordStatus(word.getWordId(), AnswerType.CORRECT);
        }
        // 错误时不做任何处理
    }

    private void onNextButtonClick() {
        // 进入下一个单词
        currentIndex++;
        
        // 重置UI状态
        spellingEditText.setText("");
        spellingEditText.setTextColor(Color.BLACK);
        correctAnswerTextView.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        
        // 显示下一个单词
        showCurrentWord();
        
        // 保存复习进度
        saveReviewProgress();
    }





    private void showCompletionPage() {
        // 清除复习进度缓存，防止错误状态被保存
        clearReviewProgress();
        
        // 隐藏所有其他UI
        spellingInputLayout.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        progressTextView.setVisibility(View.GONE);
        wrongButton.setVisibility(View.GONE);
        rightButton.setVisibility(View.GONE);
        
        // 动态构建 completionLayout 中的内容
        completionLayout.removeAllViews();
        completionLayout.setVisibility(View.VISIBLE);

        // 1. "恭喜！本次复习完成！"标题，居中
        TextView title = new TextView(requireContext());
        title.setText("恭喜！本次复习完成！");
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20f);
        title.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, 8);
        completionLayout.addView(title, titleParams);

        // 2. "快速回顾本组单词吧~"小标题，居中
        TextView subtitle = new TextView(requireContext());
        subtitle.setText("快速回顾本组单词吧~");
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextSize(16f);
        subtitle.setTextColor(Color.DKGRAY);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.setMargins(0, 0, 0, 16);
        completionLayout.addView(subtitle, subtitleParams);

        // 3. 创建滚动视图包装表格
        android.widget.ScrollView scrollView = new android.widget.ScrollView(requireContext());
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        scrollParams.setMargins(16, 0, 16, 16);
        scrollView.setLayoutParams(scrollParams);

        // 4. 创建表格容器
        LinearLayout tableContainer = new LinearLayout(requireContext());
        tableContainer.setOrientation(LinearLayout.VERTICAL);
        tableContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        
        // 5. 创建表格头部
        LinearLayout headerRow = new LinearLayout(requireContext());
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        headerRow.setBackgroundColor(0xFFE3F2FD); // 浅蓝色背景
        headerRow.setPadding(0, 12, 0, 12);
        
        TextView headerWord = new TextView(requireContext());
        headerWord.setText("单词");
        headerWord.setGravity(Gravity.CENTER);
        headerWord.setTextSize(16f);
        headerWord.setTextColor(0xFF1976D2);
        headerWord.setTypeface(headerWord.getTypeface(), android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        headerRow.addView(headerWord, headerParams);
        
        TextView headerMeaning = new TextView(requireContext());
        headerMeaning.setText("释义");
        headerMeaning.setGravity(Gravity.CENTER);
        headerMeaning.setTextSize(16f);
        headerMeaning.setTextColor(0xFF1976D2);
        headerMeaning.setTypeface(headerMeaning.getTypeface(), android.graphics.Typeface.BOLD);
        headerRow.addView(headerMeaning, headerParams);
        
        tableContainer.addView(headerRow);

        // 6. 填充每行"单词 + 中文释义"
        for (int i = 0; i < reviewBatch.size(); i++) {
            Word w = reviewBatch.get(i);
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            
            // 交替行背景色
            if (i % 2 == 0) {
                row.setBackgroundColor(0xFFF8F9FA); // 浅灰色
            } else {
                row.setBackgroundColor(Color.WHITE);
            }
            row.setPadding(0, 16, 0, 16);

            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            );

            // 左列：单词
            TextView tvWord = new TextView(requireContext());
            tvWord.setText(w.getSpelling());
            tvWord.setGravity(Gravity.CENTER);
            tvWord.setTextSize(16f);
            tvWord.setTextColor(0xFF212529);
            tvWord.setPadding(12, 8, 12, 8);
            row.addView(tvWord, cellParams);
            
            // 分隔线
            View divider = new View(requireContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));
            divider.setBackgroundColor(0xFFDEE2E6);
            row.addView(divider);

            // 右列：中文
            TextView tvMeaning = new TextView(requireContext());
            tvMeaning.setText(w.getMeaning());
            tvMeaning.setGravity(Gravity.CENTER);
            tvMeaning.setTextSize(16f);
            tvMeaning.setTextColor(0xFF212529);
            tvMeaning.setPadding(12, 8, 12, 8);
            row.addView(tvMeaning, cellParams);

            tableContainer.addView(row);
            
            // 添加底部分隔线（除了最后一行）
            if (i < reviewBatch.size() - 1) {
                View bottomDivider = new View(requireContext());
                bottomDivider.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1
                ));
                bottomDivider.setBackgroundColor(0xFFDEE2E6);
                tableContainer.addView(bottomDivider);
            }
        }
        
        // 添加表格边框
        android.graphics.drawable.GradientDrawable tableBorder = new android.graphics.drawable.GradientDrawable();
        tableBorder.setStroke(2, 0xFFDEE2E6);
        tableBorder.setCornerRadius(12f);
        tableContainer.setBackground(tableBorder);
        
        scrollView.addView(tableContainer);

        // 7. 把滚动视图加到 completionLayout
        completionLayout.addView(scrollView);

        // 8. 添加"再复习一组"和"完成"按钮的行
        LinearLayout buttonContainer = new LinearLayout(requireContext());
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(16, 0, 16, 0);

        // "再复习一组"按钮 - 圆角样式
        Button btnNextBatch = new Button(requireContext());
        btnNextBatch.setText("再复习一组");
        btnNextBatch.setTextColor(Color.WHITE);
        btnNextBatch.setTextSize(16f);
        btnNextBatch.setPadding(32, 16, 32, 16);
        
        // 创建圆角背景
        android.graphics.drawable.GradientDrawable nextBatchBg = new android.graphics.drawable.GradientDrawable();
        nextBatchBg.setColor(0xFF2196F3); // 蓝色背景
        nextBatchBg.setCornerRadius(24f); // 圆角
        btnNextBatch.setBackground(nextBatchBg);
        
        btnNextBatch.setOnClickListener(v -> {
            completionLayout.setVisibility(View.GONE);
            // 重新显示所有拼写界面组件
            spellingInputLayout.setVisibility(View.VISIBLE);
            wrongButton.setVisibility(View.VISIBLE);
            rightButton.setVisibility(View.VISIBLE);
            progressTextView.setVisibility(View.VISIBLE);
            loadReviewBatch();
        });
        buttonContainer.addView(btnNextBatch, btnParams);

        // "完成"按钮 - 圆角样式
        Button btnFinish = new Button(requireContext());
        btnFinish.setText("完成");
        btnFinish.setTextColor(Color.WHITE);
        btnFinish.setTextSize(16f);
        btnFinish.setPadding(32, 16, 32, 16);
        
        // 创建圆角背景
        android.graphics.drawable.GradientDrawable finishBg = new android.graphics.drawable.GradientDrawable();
        finishBg.setColor(0xFF4CAF50); // 绿色背景
        finishBg.setCornerRadius(24f); // 圆角
        btnFinish.setBackground(finishBg);
        
        btnFinish.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
        buttonContainer.addView(btnFinish, btnParams);

        // 9. 把按钮行加入到 completionLayout
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 16, 0, 0);
        completionLayout.addView(buttonContainer, containerParams);
    }
    
    /**
     * 保存当前复习进度到SharedPreferences
     */
    private void saveReviewProgress() {
        if (reviewBatch == null || reviewBatch.isEmpty()) {
            return; // 没有数据可保存
        }
        
        try {
            SharedPreferences.Editor editor = reviewCache.edit();
            
            // 保存单词列表
            Gson gson = new Gson();
            String batchJson = gson.toJson(reviewBatch);
            editor.putString("current_batch", batchJson);
            
            // 保存当前索引
            editor.putInt("current_index", currentIndex);
            
            // 保存失败单词列表
            String failedWordsJson = gson.toJson(failedWords);
            editor.putString("failed_words", failedWordsJson);
            
            // 保存当前批次大小设置
            SharedPreferences prefs = requireActivity().getSharedPreferences("study_settings", android.content.Context.MODE_PRIVATE);
            int currentBatchSize = prefs.getInt("review_count", 10);
            editor.putInt("batch_size", currentBatchSize);
            
            // 保存时间戳
            editor.putLong("save_timestamp", System.currentTimeMillis());
            
            editor.apply();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "保存复习进度失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 从SharedPreferences恢复复习进度
     * @return 是否成功恢复进度
     */
    private boolean restoreReviewProgress() {
        try {
            // 检查是否有缓存数据
            if (!reviewCache.contains("current_batch")) {
                return false;
            }
            
            // 检查缓存是否过期（24小时）
            long saveTime = reviewCache.getLong("save_timestamp", 0);
            long currentTime = System.currentTimeMillis();
            long twentyFourHours = 24 * 60 * 60 * 1000;
            
            if (currentTime - saveTime > twentyFourHours) {
                // 缓存已过期，清除并返回false
                clearReviewProgress();
                return false;
            }
            
            // 检查批次大小是否与当前设置一致
            SharedPreferences prefs = requireActivity().getSharedPreferences("study_settings", android.content.Context.MODE_PRIVATE);
            int currentBatchSize = prefs.getInt("review_count", 10);
            int cachedBatchSize = reviewCache.getInt("batch_size", 0);
            
            if (cachedBatchSize != currentBatchSize) {
                // 批次大小已改变，清除缓存并返回false
                clearReviewProgress();
                return false;
            }
            
            // 恢复单词列表
            String batchJson = reviewCache.getString("current_batch", "");
            if (batchJson.isEmpty()) {
                return false;
            }
            
            Gson gson = new Gson();
            Type wordListType = new TypeToken<ArrayList<Word>>(){}.getType();
            reviewBatch = gson.fromJson(batchJson, wordListType);
            
            if (reviewBatch == null || reviewBatch.isEmpty()) {
                return false;
            }
            
            // 恢复当前索引
            currentIndex = reviewCache.getInt("current_index", 0);
            
            // 恢复失败单词列表
            String failedWordsJson = reviewCache.getString("failed_words", "");
            if (!failedWordsJson.isEmpty()) {
                failedWords = gson.fromJson(failedWordsJson, wordListType);
            }
            
            // 确保当前单词是最新状态
            if (currentIndex < reviewBatch.size()) {
                currentWord = wordDao.getWordById(reviewBatch.get(currentIndex).getWordId());
                if (currentWord != null) {
                    reviewBatch.set(currentIndex, currentWord);
                } else {
                    currentWord = reviewBatch.get(currentIndex);
                }
            }
            
            // 显示当前单词
            showCurrentWord();
            return true;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "恢复复习进度失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            clearReviewProgress();
            return false;
        }
    }
    
    /**
     * 清除复习进度缓存
     */
    private void clearReviewProgress() {
        SharedPreferences.Editor editor = reviewCache.edit();
        editor.clear();
        editor.apply();
    }
}