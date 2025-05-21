package cn.itcast.wordmaster.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.widget.LinearLayout;
import cn.itcast.wordmaster.R;
import cn.itcast.wordmaster.db.WordDao;
import cn.itcast.wordmaster.entity.Word;
import cn.itcast.wordmaster.entity.AnswerType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LearnFragment extends Fragment {
    private TextView wordTextView;
    private TextView phoneticTextView;
    private CardView[] optionCards;
    private TextView[] optionTextViews;
    private WordDao wordDao;
    private List<Word> batch;
    private int currentIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 隐藏底部导航栏
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
        View view = inflater.inflate(R.layout.fragment_learn, container, false);
        
        // 初始化视图组件
        wordTextView = view.findViewById(R.id.wordTextView);
        phoneticTextView = view.findViewById(R.id.phoneticTextView);
        
        // 初始化选项卡片和文本视图数组
        optionCards = new CardView[4];
        optionTextViews = new TextView[4];
        
        // 获取选项卡片和文本视图引用
        optionCards[0] = view.findViewById(R.id.optionCard1);
        optionCards[1] = view.findViewById(R.id.optionCard2);
        optionCards[2] = view.findViewById(R.id.optionCard3);
        optionCards[3] = view.findViewById(R.id.optionCard4);
        
        optionTextViews[0] = view.findViewById(R.id.optionText1);
        optionTextViews[1] = view.findViewById(R.id.optionText2);
        optionTextViews[2] = view.findViewById(R.id.optionText3);
        optionTextViews[3] = view.findViewById(R.id.optionText4);
        
        // 设置选项点击事件
        for (int i = 0; i < optionCards.length; i++) {
            final int optionIndex = i;
            optionCards[i].setOnClickListener(v -> onOptionClick(optionIndex));
        }
        
        wordDao = new WordDao(requireContext());
        loadBatchAndShowFirst();
        
        return view;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 显示底部导航栏
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
    }

    private void loadBatchAndShowFirst() {
        batch = wordDao.getLearningBatch();
        if (!batch.isEmpty()) {
            showWord(batch.get(0));
        } else {
            // TODO: 提示"没有可学习的单词"
        }
    }

    private void showWord(Word w) {
        // 在"出题"前，先 --nextDueOffset 并写回
        int offset = Math.max(0, w.getNextDueOffset() - 1);
        w.setNextDueOffset(offset);
        wordDao.updateOffset(w.getWordId(), offset);
        
        // 根据 correctCount 切换模式
        if (w.getCorrectCount() < 2) {
            // 普通模式：四选一
            List<String> distractors = wordDao.getRandomMeanings(w.getMeaning(), 3);
            List<String> options = new ArrayList<>(distractors);
            options.add(w.getMeaning());
            Collections.shuffle(options);
            
            updateWord(w.getSpelling(), w.getPhonetic(), options.toArray(new String[0]));
        } else {
            // 第三次复习模式：回想英译
            updateWord(w.getSpelling(), w.getPhonetic(),
                       new String[]{"认识", "模糊", "不记得", ""});
        }
    }

    private void onOptionClick(int idx) {
        Word w = batch.get(currentIndex);
        String selected = optionTextViews[idx].getText().toString();
        
        // 判定结果类型
        boolean isCorrect = selected.equals(w.getMeaning());
        AnswerType type;
        if (w.getCorrectCount() < 2) {
            type = isCorrect ? AnswerType.CORRECT : AnswerType.WRONG;
        } else {
            // 第三次复习
            if (selected.equals("认识"))   type = AnswerType.RECOGNIZE;
            else if (selected.equals("模糊")) type = AnswerType.FUZZY;
            else                            type = AnswerType.FORGET;
        }
        
        // 更新状态
        wordDao.updateWordStatus(w.getWordId(), type);
        
        // 切换到下一个
        currentIndex++;
        if (currentIndex < batch.size()) {
            showWord(batch.get(currentIndex));
        } else {
            // 本组结束，重新加载新的批次
            loadBatchAndShowFirst();
        }
    }
    
    // 更新界面显示的单词和选项
    public void updateWord(String word, String phonetic, String[] options) {
        wordTextView.setText(word);
        phoneticTextView.setText(phonetic);
        
        for (int i = 0; i < options.length && i < optionTextViews.length; i++) {
            optionTextViews[i].setText(options[i]);
        }
    }
}