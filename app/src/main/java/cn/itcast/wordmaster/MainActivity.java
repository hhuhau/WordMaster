package cn.itcast.wordmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 跳转到登录页面
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish(); // 结束MainActivity，防止用户返回
    }
}