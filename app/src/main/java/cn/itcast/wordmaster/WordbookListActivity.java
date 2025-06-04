package cn.itcast.wordmaster;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.itcast.wordmaster.db.WordbookDao;
import cn.itcast.wordmaster.entity.Wordbook;

public class WordbookListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WordbookAdapter adapter;
    private WordbookDao wordbookDao;
    private String currentWordbookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_wordbook_list);

        // 初始化DAO
        wordbookDao = new WordbookDao(this);
        
        // 获取当前正在学习的词书ID
        currentWordbookId = wordbookDao.getCurrentWordbookId(this);

        // 初始化视图
        initViews();
        
        // 加载词书列表
        loadWordbooks();
    }

    private void initViews() {
        // 设置返回按钮
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // 设置RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadWordbooks() {
        // 获取所有词书
        List<Wordbook> wordbooks = wordbookDao.getAllWordbooks();
        
        // 设置适配器
        adapter = new WordbookAdapter(this, wordbooks, currentWordbookId);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 词书列表适配器
     */
    private class WordbookAdapter extends RecyclerView.Adapter<WordbookAdapter.ViewHolder> {

        private Context context;
        private List<Wordbook> wordbooks;
        private String currentWordbookId;

        public WordbookAdapter(Context context, List<Wordbook> wordbooks, String currentWordbookId) {
            this.context = context;
            this.wordbooks = wordbooks;
            this.currentWordbookId = currentWordbookId;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_wordbook, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Wordbook wordbook = wordbooks.get(position);
            
            // 设置词书名称
            holder.tvBookName.setText(wordbook.getBookName());
            
            // 设置词书描述
            holder.tvBookDescription.setText(wordbook.getDescription());
            
            // 设置词书单词数量
            holder.tvBookWordCount.setText(wordbook.getWordCount() + "词");
            
            // 设置封面图片
            String coverImageName = wordbook.getCoverImageUrl();
            int resourceId = context.getResources().getIdentifier(
                    coverImageName, "drawable", context.getPackageName());
            if (resourceId != 0) {
                holder.ivCover.setImageResource(resourceId);
            }
            
            // 如果是当前正在学习的词书，显示标记
            if (wordbook.getBookId().equals(currentWordbookId)) {
                holder.tvLearning.setVisibility(View.VISIBLE);
            } else {
                holder.tvLearning.setVisibility(View.GONE);
            }
            
            // 设置点击事件
            holder.itemView.setOnClickListener(v -> {
                // 设置为当前学习的词书
                wordbookDao.setCurrentWordbookId(context, wordbook.getBookId());
                
                // 更新UI
                String oldCurrentId = currentWordbookId;
                currentWordbookId = wordbook.getBookId();
                
                // 刷新列表
                notifyItemChanged(position);
                
                // 找到之前的当前词书位置并刷新
                for (int i = 0; i < wordbooks.size(); i++) {
                    if (wordbooks.get(i).getBookId().equals(oldCurrentId)) {
                        notifyItemChanged(i);
                        break;
                    }
                }
                
                // 提示用户
                Toast.makeText(context, "已选择《" + wordbook.getBookName() + "》", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return wordbooks.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivCover;
            TextView tvBookName;
            TextView tvBookDescription;
            TextView tvBookWordCount;
            TextView tvLearning;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivCover = itemView.findViewById(R.id.iv_cover);
                tvBookName = itemView.findViewById(R.id.tv_book_name);
                tvBookDescription = itemView.findViewById(R.id.tv_book_description);
                tvBookWordCount = itemView.findViewById(R.id.tv_book_word_count);
                tvLearning = itemView.findViewById(R.id.tv_learning);
            }
        }
    }
}