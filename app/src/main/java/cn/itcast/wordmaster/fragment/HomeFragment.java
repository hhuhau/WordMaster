package cn.itcast.wordmaster.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.example.wordmaster.WordDao;
import cn.itcast.wordmaster.R;

public class HomeFragment extends Fragment {
    private MaterialButton learnButton;
    private MaterialButton reviewButton;
    private TextView wordTextView;

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
        
        // 设置Learn按钮点击事件
        learnButton.setOnClickListener(v -> {
            LearnFragment learnFragment = new LearnFragment();
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, learnFragment)
                .addToBackStack(null)
                .commit();
        });
        
        // 获取并显示随机单词
        WordDao wordDao = new WordDao(requireContext());
        String randomWord = wordDao.getRandomWordSpelling();
        if (randomWord != null) {
            // 将单词首字母转换为大写
            String capitalizedWord = randomWord.substring(0, 1).toUpperCase() + randomWord.substring(1);
            wordTextView.setText(capitalizedWord);
        }
    }
}