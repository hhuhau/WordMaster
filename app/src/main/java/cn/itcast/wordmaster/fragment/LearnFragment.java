package cn.itcast.wordmaster.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import cn.itcast.wordmaster.R;
import cn.itcast.wordmaster.db.WordDao;
import cn.itcast.wordmaster.entity.Word;
import cn.itcast.wordmaster.entity.AnswerType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import androidx.core.content.ContextCompat;
import android.content.SharedPreferences;
import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;


public class LearnFragment extends Fragment {
    private static final int COLOR_CORRECT = 0xFFC8E6C9; // 更浅的绿色
    private static final int COLOR_WRONG   = 0xFFFFCDD2; // 更浅的红色

    private TextView wordTextView;
    private TextView phoneticTextView;
    private TextView correctCountTextView;
    private TextView instructionTextView;
    private TextView meaningTextView;
    private TextView progressTextView;

    private CardView[] optionCards;
    private TextView[] optionTextViews;
    private Button continueButton;
    private Button nextWordButton;

    private LinearLayout completionLayout;
    private TextView completionTextView;
    private Button nextBatchButton;
    private Button finishButton;

    private WordDao wordDao;
    private List<Word> batch;
    private int currentIndex = 0;
    private Word currentWord;
    private boolean hasAnsweredWrong = false; // 标记本轮是否答错过
    private String[] optionTexts;             // 保存当前选项文本
    private SharedPreferences learningCache; // 学习进度缓存
    private Gson gson;                        // JSON序列化工具

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
        View view = inflater.inflate(R.layout.fragment_learn, container, false);

        ImageView backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        wordTextView         = view.findViewById(R.id.wordTextView);
        phoneticTextView     = view.findViewById(R.id.phoneticTextView);
        correctCountTextView = view.findViewById(R.id.correctCountTextView);
        instructionTextView  = view.findViewById(R.id.instructionTextView);
        meaningTextView      = view.findViewById(R.id.meaningTextView);
        progressTextView     = view.findViewById(R.id.progressTextView);
        continueButton       = view.findViewById(R.id.continueButton);
        nextWordButton       = view.findViewById(R.id.nextWordButton);

        completionLayout     = view.findViewById(R.id.completionLayout);
        completionTextView   = view.findViewById(R.id.completionTextView);
        nextBatchButton      = view.findViewById(R.id.nextBatchButton);
        finishButton         = view.findViewById(R.id.finishButton);

        // “继续”按钮：前两次答题后才会调用 showMeaningPage()
        continueButton.setOnClickListener(v -> showMeaningPage());

        // 第三次复习的“下一题”按钮是在 onOptionClick 内直接显示
        nextWordButton.setOnClickListener(v -> {
            meaningTextView.setVisibility(View.GONE);
            nextWordButton.setVisibility(View.GONE);
            nextWord();
        });

        nextBatchButton.setOnClickListener(v -> {
            completionLayout.setVisibility(View.GONE);
            loadBatchAndShowFirst();
        });
        finishButton.setOnClickListener(v -> requireActivity().onBackPressed());

        optionCards = new CardView[4];
        optionTextViews = new TextView[4];
        optionCards[0] = view.findViewById(R.id.optionCard1);
        optionCards[1] = view.findViewById(R.id.optionCard2);
        optionCards[2] = view.findViewById(R.id.optionCard3);
        optionCards[3] = view.findViewById(R.id.optionCard4);

        optionTextViews[0] = view.findViewById(R.id.optionText1);
        optionTextViews[1] = view.findViewById(R.id.optionText2);
        optionTextViews[2] = view.findViewById(R.id.optionText3);
        optionTextViews[3] = view.findViewById(R.id.optionText4);

        for (int i = 0; i < optionCards.length; i++) {
            final int index = i;
            optionCards[i].setOnClickListener(v -> onOptionClick(index));
        }

        wordDao = new WordDao(requireContext());
        learningCache = requireActivity().getSharedPreferences("learning_cache", Context.MODE_PRIVATE);
        
        // 临时清除缓存以确保使用新的学习逻辑（避免第一个单词闪退问题）
        // 这个逻辑可以在确认问题解决后移除
        SharedPreferences tempCheck = requireActivity().getSharedPreferences("temp_cache_cleared", Context.MODE_PRIVATE);
        if (!tempCheck.getBoolean("cache_cleared_v2", false)) {
            clearLearningProgress();
            tempCheck.edit().putBoolean("cache_cleared_v2", true).apply();
        }
        gson = new Gson();
        loadBatchAndShowFirst();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
        // 只有在学习未完成时才保存进度
        if (completionLayout.getVisibility() != View.VISIBLE) {
            saveLearningProgress();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // 只有在学习未完成时才保存进度
        if (completionLayout.getVisibility() != View.VISIBLE) {
            saveLearningProgress();
        }
    }

    private void loadBatchAndShowFirst() {
        // 尝试恢复之前的学习进度
        if (restoreLearningProgress()) {
            // 成功恢复进度，继续之前的学习
            if (batch != null && !batch.isEmpty() && 
                currentIndex >= 0 && currentIndex < batch.size() && 
                batch.get(currentIndex) != null) {
                try {
                    showWord(batch.get(currentIndex));
                    return;
                } catch (Exception e) {
                    // 显示单词失败，清除缓存并加载新批次
                    clearLearningProgress();
                }
            }
        }
        
        // 没有缓存或缓存无效，加载新的学习批次
        loadNewBatch();
    }
    
    private void loadNewBatch() {
        // 从SharedPreferences获取学习单词量设置
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("study_settings", android.content.Context.MODE_PRIVATE);
        int batchSize = prefs.getInt("learn_count", 10); // 默认为10个
        
        batch = wordDao.getLearningBatch(batchSize);
        if (!batch.isEmpty()) {
            currentIndex = 0;
            hasAnsweredWrong = false;
            showWord(batch.get(0));
            // 保存新的学习进度
            saveLearningProgress();
        }
    }

    private void showWord(Word w) {
        // 安全检查，确保单词不为空
        if (w == null) {
            // 如果单词为空，清除缓存并加载新批次
            clearLearningProgress();
            loadNewBatch();
            return;
        }
        
        currentWord = w;
        hasAnsweredWrong = false;

        // —— 更新进度显示 ——
        if (batch != null && !batch.isEmpty()) {
            int completedCount = 0;
            for (Word word : batch) {
                if (word.getCorrectCount() >= 3) {
                    completedCount++;
                }
            }
            progressTextView.setText(completedCount + "/" + batch.size());
        }

        // —— 隐藏／重置所有状态 ——
        instructionTextView.setVisibility(View.GONE);
        meaningTextView.setVisibility(View.GONE);
        nextWordButton.setVisibility(View.GONE);

        wordTextView.setVisibility(View.VISIBLE);
        phoneticTextView.setVisibility(View.VISIBLE);
        correctCountTextView.setVisibility(View.VISIBLE);
        continueButton.setVisibility(View.INVISIBLE);

        completionLayout.setVisibility(View.GONE);

        for (CardView card : optionCards) {
            card.setVisibility(View.VISIBLE);
            card.setEnabled(true);
            card.setCardBackgroundColor(Color.WHITE);
        }

        if (w.getCorrectCount() < 2) {
            // —— 普通四选一环节 ——
            List<String> distractors = wordDao.getRandomMeanings(w.getMeaning(), 3);
            List<String> options = new ArrayList<>(distractors);
            options.add(w.getMeaning());
            Collections.shuffle(options);
            updateWord(w.getSpelling(), w.getPhonetic(), options.toArray(new String[0]));
        } else {
            // —— 第三次复习环节 ——
            wordTextView.setText(w.getSpelling());
            phoneticTextView.setText(w.getPhonetic());

            instructionTextView.setVisibility(View.VISIBLE);

            optionCards[0].setVisibility(View.VISIBLE);
            optionTextViews[0].setText("认识");
            optionCards[1].setVisibility(View.VISIBLE);
            optionTextViews[1].setText("不认识");
            optionCards[2].setVisibility(View.GONE);
            optionCards[3].setVisibility(View.GONE);

            continueButton.setVisibility(View.GONE);
        }
    }

    private void onOptionClick(int optionIndex) {
        if (currentWord == null) return;
        String selectedOption = optionTexts[optionIndex];

        if (currentWord.getCorrectCount() < 2) {
            // —— 前两次：四选一逻辑 ——
            boolean isCorrect = selectedOption.equals(currentWord.getMeaning());
            if (isCorrect) {
                currentWord.setCorrectCount(currentWord.getCorrectCount() + 1);
                wordDao.updateWordStatus(currentWord.getWordId(), AnswerType.CORRECT);
                optionCards[optionIndex].setCardBackgroundColor(COLOR_CORRECT);
            } else {
                if (!hasAnsweredWrong) {
                    currentWord.setCorrectCount(Math.max(0, currentWord.getCorrectCount() - 1));
                    wordDao.updateWordStatus(currentWord.getWordId(), AnswerType.WRONG);
                    hasAnsweredWrong = true;
                }
                optionCards[optionIndex].setCardBackgroundColor(COLOR_WRONG);
                for (int i = 0; i < optionTexts.length; i++) {
                    if (optionTexts[i].equals(currentWord.getMeaning())) {
                        optionCards[i].setCardBackgroundColor(COLOR_CORRECT);
                        break;
                    }
                }
            }
            for (CardView card : optionCards) {
                card.setEnabled(false);
            }
            continueButton.setVisibility(View.VISIBLE);

        } else {
            // —— 第三次：只剩“认识/不认识”两项 ——
            if (optionIndex == 0) {
                currentWord.setCorrectCount(currentWord.getCorrectCount() + 1);
                wordDao.updateWordStatus(currentWord.getWordId(), AnswerType.RECOGNIZE);
                optionCards[optionIndex].setCardBackgroundColor(COLOR_CORRECT);
            } else if (optionIndex == 1) {
                currentWord.setCorrectCount(Math.max(0, currentWord.getCorrectCount() - 1));
                wordDao.updateWordStatus(currentWord.getWordId(), AnswerType.FORGET);
                optionCards[optionIndex].setCardBackgroundColor(COLOR_WRONG);
            } else {
                return;
            }
            for (CardView card : optionCards) {
                card.setEnabled(false);
            }

            // —— 第三次：只显示“中文释义”，并出现“下一题”按钮 ——
            meaningTextView.setText(currentWord.getMeaning());
            meaningTextView.setVisibility(View.VISIBLE);
            nextWordButton.setVisibility(View.VISIBLE);
        }

        correctCountTextView.setText(String.valueOf(currentWord.getCorrectCount()));
    }

    private void updateWord(String word, String phonetic, String[] options) {
        wordTextView.setText(word);
        phoneticTextView.setText(phonetic);
        correctCountTextView.setText(String.valueOf(currentWord != null ? currentWord.getCorrectCount() : 0));

        for (int i = 0; i < optionCards.length; i++) {
            optionCards[i].setEnabled(true);
            optionCards[i].setCardBackgroundColor(Color.WHITE);
            if (i < options.length && !options[i].isEmpty()) {
                optionTextViews[i].setText(options[i]);
                optionCards[i].setVisibility(View.VISIBLE);
            } else {
                optionCards[i].setVisibility(View.GONE);
            }
        }
        optionTexts = options;
        continueButton.setVisibility(View.INVISIBLE);
    }

    private void nextWord() {
        boolean allCompleted = true;
        for (Word word : batch) {
            if (word.getCorrectCount() < 3) {
                allCompleted = false;
                break;
            }
        }
        if (allCompleted) {
            showCompletionPage();
            return;
        }
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < batch.size(); i++) {
            if (batch.get(i).getCorrectCount() < 3) {
                availableIndices.add(i);
            }
        }
        if (!availableIndices.isEmpty()) {
            int randomIndex = new Random().nextInt(availableIndices.size());
            currentIndex = availableIndices.get(randomIndex);
            showWord(batch.get(currentIndex));
        } else {
            showCompletionPage();
        }
    }

    private void showCompletionPage() {
        // 清除学习进度缓存，防止错误状态被保存
        clearLearningProgress();
        
        // —— 第一步：隐藏练习界面中不需要的所有视图 ——
        wordTextView.setVisibility(View.GONE);
        phoneticTextView.setVisibility(View.GONE);
        correctCountTextView.setVisibility(View.GONE);
        instructionTextView.setVisibility(View.GONE);
        continueButton.setVisibility(View.GONE);
        meaningTextView.setVisibility(View.GONE);
        nextWordButton.setVisibility(View.GONE);
        for (CardView card : optionCards) {
            card.setVisibility(View.GONE);
        }

        // **把“中间可伸缩空间”隐藏，这样 completionLayout 会往上顶**
        View learningContainer = getView().findViewById(R.id.learningContainer);
        if (learningContainer != null) {
            learningContainer.setVisibility(View.GONE);
        }

        // —— 第二步：动态构建 completionLayout 中的内容 ——
        completionLayout.removeAllViews();
        completionLayout.setVisibility(View.VISIBLE);

        // 3. “恭喜！本次学习完成！”标题，居中
        TextView title = new TextView(requireContext());
        title.setText("恭喜！本次学习完成！");
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20f);
        title.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, 8);
        completionLayout.addView(title, titleParams);

        // 4. “快速回顾本组单词吧~”小标题，居中
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

        // 5. 创建滚动视图包装表格
        android.widget.ScrollView scrollView = new android.widget.ScrollView(requireContext());
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        scrollParams.setMargins(16, 0, 16, 16);
        scrollView.setLayoutParams(scrollParams);

        // 6. 创建表格容器
        LinearLayout tableContainer = new LinearLayout(requireContext());
        tableContainer.setOrientation(LinearLayout.VERTICAL);
        tableContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        
        // 7. 创建表格头部
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

        // 8. 填充每行"单词 + 中文释义"
        for (int i = 0; i < batch.size(); i++) {
            Word w = batch.get(i);
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
            if (i < batch.size() - 1) {
                View bottomDivider = new View(requireContext());
                bottomDivider.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1
                ));
                bottomDivider.setBackgroundColor(0xFFDEE2E6);
                tableContainer.addView(bottomDivider);
            }
        }
        
        // 添加表格边框
        GradientDrawable tableBorder = new GradientDrawable();
        tableBorder.setStroke(2, 0xFFDEE2E6);
        tableBorder.setCornerRadius(12f);
        tableContainer.setBackground(tableBorder);
        
        scrollView.addView(tableContainer);

        // 9. 把滚动视图加到 completionLayout
        completionLayout.addView(scrollView);

        // 10. 添加"再学一组"和"完成"按钮的行
        LinearLayout buttonContainer = new LinearLayout(requireContext());
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(16, 0, 16, 0);

        // "再学一组"按钮 - 圆角样式
        Button btnNextBatch = new Button(requireContext());
        btnNextBatch.setText("再学一组");
        btnNextBatch.setTextColor(Color.WHITE);
        btnNextBatch.setTextSize(16f);
        btnNextBatch.setPadding(32, 16, 32, 16);
        
        // 创建圆角背景
        GradientDrawable nextBatchBg = new GradientDrawable();
        nextBatchBg.setColor(0xFF2196F3); // 蓝色背景
        nextBatchBg.setCornerRadius(24f); // 圆角
        btnNextBatch.setBackground(nextBatchBg);
        
        btnNextBatch.setOnClickListener(v -> {
            completionLayout.setVisibility(View.GONE);
            // 恢复中间区域，以便下次再次学习时可见
            if (learningContainer != null) {
                learningContainer.setVisibility(View.VISIBLE);
            }
            // 加载新的批次
            loadNewBatch();
        });
        buttonContainer.addView(btnNextBatch, btnParams);

        // "完成"按钮 - 圆角样式
        Button btnFinish = new Button(requireContext());
        btnFinish.setText("完成");
        btnFinish.setTextColor(Color.WHITE);
        btnFinish.setTextSize(16f);
        btnFinish.setPadding(32, 16, 32, 16);
        
        // 创建圆角背景
        GradientDrawable finishBg = new GradientDrawable();
        finishBg.setColor(0xFF4CAF50); // 绿色背景
        finishBg.setCornerRadius(24f); // 圆角
        btnFinish.setBackground(finishBg);
        
        btnFinish.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
        buttonContainer.addView(btnFinish, btnParams);

        // 11. 把按钮行加入到 completionLayout
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 16, 0, 0);
        completionLayout.addView(buttonContainer, containerParams);
    }


    private void showMeaningPage() {
        // —— 前两次（四选一）答题结束后调用 ——
        // 这里要显示“英文 + 音标 + 中文释义 + 下一题按钮”
        wordTextView.setVisibility(View.VISIBLE);
        phoneticTextView.setVisibility(View.VISIBLE);
        correctCountTextView.setVisibility(View.VISIBLE);

        for (CardView card : optionCards) {
            card.setVisibility(View.GONE);
        }
        continueButton.setVisibility(View.GONE);

        // “中文释义”就在原来的英文+音标下方显示：
        meaningTextView.setText(currentWord.getMeaning());
        meaningTextView.setVisibility(View.VISIBLE);

        nextWordButton.setVisibility(View.VISIBLE);
    }
    
    /**
     * 保存学习进度到SharedPreferences
     */
    private void saveLearningProgress() {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        
        SharedPreferences.Editor editor = learningCache.edit();
        
        // 保存单词列表
        String batchJson = gson.toJson(batch);
        editor.putString("current_batch", batchJson);
        
        // 保存当前索引
        editor.putInt("current_index", currentIndex);
        
        // 保存是否答错标记
        editor.putBoolean("has_answered_wrong", hasAnsweredWrong);
        
        // 保存当前批次大小设置
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("study_settings", android.content.Context.MODE_PRIVATE);
        int currentBatchSize = prefs.getInt("learn_count", 10);
        editor.putInt("batch_size", currentBatchSize);
        
        // 保存时间戳，用于判断缓存是否过期（24小时）
        editor.putLong("save_timestamp", System.currentTimeMillis());
        
        editor.apply();
    }
    
    /**
     * 从SharedPreferences恢复学习进度
     * @return 是否成功恢复
     */
    private boolean restoreLearningProgress() {
        try {
            // 检查缓存是否存在
            if (!learningCache.contains("current_batch")) {
                return false;
            }
            
            // 检查缓存是否过期（24小时）
            long saveTime = learningCache.getLong("save_timestamp", 0);
            long currentTime = System.currentTimeMillis();
            long twentyFourHours = 24 * 60 * 60 * 1000;
            
            if (currentTime - saveTime > twentyFourHours) {
                // 缓存过期，清除并返回false
                clearLearningProgress();
                return false;
            }
            
            // 检查批次大小是否与当前设置一致
            android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("study_settings", android.content.Context.MODE_PRIVATE);
            int currentBatchSize = prefs.getInt("learn_count", 10);
            int cachedBatchSize = learningCache.getInt("batch_size", 0);
            
            if (cachedBatchSize != currentBatchSize) {
                // 批次大小已改变，清除缓存并返回false
                clearLearningProgress();
                return false;
            }
            
            // 恢复单词列表
            String batchJson = learningCache.getString("current_batch", "");
            if (batchJson.isEmpty()) {
                return false;
            }
            
            Type listType = new TypeToken<List<Word>>(){}.getType();
            batch = gson.fromJson(batchJson, listType);
            
            if (batch == null || batch.isEmpty()) {
                return false;
            }
            
            // 恢复当前索引
            currentIndex = learningCache.getInt("current_index", 0);
            
            // 恢复答错标记
            hasAnsweredWrong = learningCache.getBoolean("has_answered_wrong", false);
            
            // 检查是否所有单词都已完成学习
            boolean allCompleted = true;
            List<Word> validWords = new ArrayList<>();
            
            for (Word word : batch) {
                if (word == null || word.getWordId() <= 0) {
                    continue; // 跳过无效单词
                }
                
                try {
                    // 重新从数据库获取最新的correctCount
                    Word updatedWord = wordDao.getWordById(word.getWordId());
                    if (updatedWord != null) {
                        word.setCorrectCount(updatedWord.getCorrectCount());
                        word.setLastReviewed(updatedWord.getLastReviewed());
                        word.setNextDueOffset(updatedWord.getNextDueOffset());
                        validWords.add(word);
                        
                        if (word.getCorrectCount() < 3) {
                            allCompleted = false;
                        }
                    }
                } catch (Exception e) {
                    // 数据库操作失败，跳过这个单词
                    continue;
                }
            }
            
            // 更新batch为有效单词列表
            batch = validWords;
            
            // 如果没有有效单词，清除缓存
            if (batch.isEmpty()) {
                clearLearningProgress();
                return false;
            }
            
            if (allCompleted) {
                // 所有单词都已完成，清除缓存
                clearLearningProgress();
                return false;
            }
            
            // 确保currentIndex指向一个未完成的单词，并且优先选择correctCount < 2的单词（四选一模式）
            if (currentIndex < 0 || currentIndex >= batch.size() || 
                batch.get(currentIndex) == null || batch.get(currentIndex).getCorrectCount() >= 3) {
                // 首先寻找correctCount < 2的单词（四选一模式）
                List<Integer> fourChoiceIndices = new ArrayList<>();
                List<Integer> allAvailableIndices = new ArrayList<>();
                
                for (int i = 0; i < batch.size(); i++) {
                    Word word = batch.get(i);
                    if (word != null && word.getCorrectCount() < 3) {
                        allAvailableIndices.add(i);
                        if (word.getCorrectCount() < 2) {
                            fourChoiceIndices.add(i);
                        }
                    }
                }
                
                if (allAvailableIndices.isEmpty()) {
                    clearLearningProgress();
                    return false;
                }
                
                // 优先选择四选一模式的单词，如果没有则选择其他未完成的单词
                if (!fourChoiceIndices.isEmpty()) {
                    int randomIndex = new Random().nextInt(fourChoiceIndices.size());
                    currentIndex = fourChoiceIndices.get(randomIndex);
                } else {
                    int randomIndex = new Random().nextInt(allAvailableIndices.size());
                    currentIndex = allAvailableIndices.get(randomIndex);
                }
            }
            
            // 如果当前单词不是四选一模式，尝试重新加载新的学习批次
            if (batch.get(currentIndex).getCorrectCount() >= 2) {
                // 清除缓存，强制重新加载新的学习批次
                clearLearningProgress();
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            // 恢复失败，清除缓存
            clearLearningProgress();
            return false;
        }
    }
    
    /**
     * 清除学习进度缓存
     */
    private void clearLearningProgress() {
        SharedPreferences.Editor editor = learningCache.edit();
        editor.clear();
        editor.apply();
    }
}