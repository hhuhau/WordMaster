package cn.itcast.wordmaster.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cn.itcast.wordmaster.AccountSettingsActivity;
import cn.itcast.wordmaster.LoginActivity;
import cn.itcast.wordmaster.MyDataActivity;
import cn.itcast.wordmaster.R;
import cn.itcast.wordmaster.StudySettingsActivity;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 学习设置
        TextView studySettingsText = view.findViewById(R.id.pref_study);
        studySettingsText.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), StudySettingsActivity.class);
            startActivity(intent);
        });

        // 账号设置
        TextView accountSettingsText = view.findViewById(R.id.pref_account);
        accountSettingsText.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
            startActivity(intent);
        });

        // 我的数据
        TextView myDataText = view.findViewById(R.id.pref_data);
        myDataText.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyDataActivity.class);
            startActivity(intent);
        });

        // 退出登录
        TextView logoutText = view.findViewById(R.id.pref_logout);
        logoutText.setOnClickListener(v -> logout());

        return view;
    }

    private void logout() {
        // 清除用户信息
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences("user_info", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // 跳转到登录页面
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}