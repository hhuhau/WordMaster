package cn.itcast.wordmaster;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import cn.itcast.wordmaster.fragment.HomeFragment;
import cn.itcast.wordmaster.fragment.ProfileFragment;

public class HomeActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置沉浸式状态栏
        setupImmersiveMode();
        
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_home);

        // 初始化Fragment
        initFragments();
        // 初始化底部导航栏
        initBottomNavigation();
        // 默认显示首页Fragment
        showFragment(homeFragment);
    }

    private void initFragments() {
        homeFragment = new HomeFragment();
        profileFragment = new ProfileFragment();
    }

    private void initBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // 默认选中首页
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                showFragment(homeFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                showFragment(profileFragment);
                return true;
            }
            return false;
        });
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        // 获取当前显示的Fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        
        // 如果当前是ProfileFragment，返回到HomeFragment
        if (currentFragment instanceof ProfileFragment) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            showFragment(homeFragment);
        } else {
            // 如果已经在HomeFragment，则退出应用
            super.onBackPressed();
        }
    }
    
    /**
     * 设置透明状态栏，显示系统状态栏内容
     */
    private void setupImmersiveMode() {
        Window window = getWindow();
        
        // 设置状态栏透明但显示内容
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        // 设置导航栏透明
        window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        
        // 允许内容延伸到系统栏区域，但保持状态栏可见
        WindowCompat.setDecorFitsSystemWindows(window, false);
        
        // 设置状态栏文字颜色为深色（适配浅色背景）
        View decorView = window.getDecorView();
        int flags = decorView.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // 设置浅色背景标志，让文字显示为深色
        decorView.setSystemUiVisibility(flags);
    }
}