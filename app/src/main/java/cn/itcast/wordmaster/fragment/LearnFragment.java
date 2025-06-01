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


public class LearnFragment extends Fragment {
    private static final int COLOR_CORRECT = 0xFFC8E6C9; // 更浅的绿色
    private static final int COLOR_WRONG   = 0xFFFFCDD2; // 更浅的红色

    private TextView wordTextView;
    private TextView phoneticTextView;
    private TextView correctCountTextView;
    private TextView instructionTextView;
    private TextView meaningTextView;

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
        loadBatchAndShowFirst();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
    }

    private void loadBatchAndShowFirst() {
        batch = wordDao.getLearningBatch();
        if (!batch.isEmpty()) {
            currentIndex = 0;
            hasAnsweredWrong = false;
            showWord(batch.get(0));
        }
    }

    private void showWord(Word w) {
        currentWord = w;
        hasAnsweredWrong = false;

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

        // 5. 准备单元格边框
        GradientDrawable cellBorder = new GradientDrawable();
        cellBorder.setStroke(1, Color.GRAY);

        // 6. 创建 TableLayout
        TableLayout table = new TableLayout(requireContext());
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        table.setLayoutParams(tableParams);
        table.setStretchAllColumns(true);
        table.setShrinkAllColumns(true);
        table.setPadding(0, 0, 0, 16);

        // 7. 填充每行“单词 + 中文释义”
        for (Word w : batch) {
            TableRow row = new TableRow(requireContext());
            TableRow.LayoutParams cellParams = new TableRow.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            );

            // 左列：单词
            TextView tvWord = new TextView(requireContext());
            tvWord.setText(w.getSpelling());
            tvWord.setGravity(Gravity.CENTER);
            tvWord.setTextSize(18f);
            tvWord.setTextColor(Color.BLACK);
            tvWord.setBackground(cellBorder);
            tvWord.setPadding(8, 8, 8, 8);
            row.addView(tvWord, cellParams);

            // 右列：中文
            TextView tvMeaning = new TextView(requireContext());
            tvMeaning.setText(w.getMeaning());
            tvMeaning.setGravity(Gravity.CENTER);
            tvMeaning.setTextSize(18f);
            tvMeaning.setTextColor(Color.BLACK);
            tvMeaning.setBackground(cellBorder);
            tvMeaning.setPadding(8, 8, 8, 8);
            row.addView(tvMeaning, cellParams);

            table.addView(row);
        }

        // 8. 把表格加到 completionLayout
        completionLayout.addView(table);

        // 9. 添加“再学一组”和“完成”按钮的行
        LinearLayout buttonContainer = new LinearLayout(requireContext());
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(16, 0, 16, 0);

        // “再学一组”按钮
        Button btnNextBatch = new Button(requireContext());
        btnNextBatch.setText("再学一组");
        btnNextBatch.setBackgroundTintList(
                ContextCompat.getColorStateList(requireContext(), R.color.light_blue_border)
        );
        btnNextBatch.setOnClickListener(v -> {
            completionLayout.setVisibility(View.GONE);
            // 恢复中间区域，以便下次再次学习时可见
            if (learningContainer != null) {
                learningContainer.setVisibility(View.VISIBLE);
            }
            loadBatchAndShowFirst();
        });
        buttonContainer.addView(btnNextBatch, btnParams);

        // “完成”按钮
        Button btnFinish = new Button(requireContext());
        btnFinish.setText("完成");
        btnFinish.setBackgroundTintList(
                ContextCompat.getColorStateList(requireContext(), R.color.light_blue_border)
        );
        btnFinish.setOnClickListener(v -> requireActivity().onBackPressed());
        buttonContainer.addView(btnFinish, btnParams);

        // 10. 把按钮行加入到 completionLayout
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
}