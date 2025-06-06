package cn.itcast.wordmaster.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.button.MaterialButton;
import cn.itcast.wordmaster.R;
import cn.itcast.wordmaster.db.WordDao;

public class HomeFragment extends Fragment {
    private MaterialButton learnButton;
    private MaterialButton reviewButton;
    private TextView wordTextView;
    private WordDao wordDao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        learnButton = view.findViewById(R.id.btn_learn);
        reviewButton = view.findViewById(R.id.btn_review);
        wordTextView = view.findViewById(R.id.tv_word);
        
        wordDao = new WordDao(getContext());
        
        // 跳转到学习页面
        learnButton.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new LearnFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
        
        reviewButton.setOnClickListener(v -> {
            // 跳转到复习页面
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ReviewFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
        
        // 获取并显示随机单词
        String randomWord = wordDao.getRandomWordSpelling();
        if (randomWord != null) {
            // 将单词首字母转换为大写
            String capitalizedWord = randomWord.substring(0, 1).toUpperCase() + randomWord.substring(1);
            wordTextView.setText(capitalizedWord);
        }
    }
}