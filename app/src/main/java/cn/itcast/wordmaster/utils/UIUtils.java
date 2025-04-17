package cn.itcast.wordmaster.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.textfield.TextInputLayout;

public class UIUtils {
    /**
     * 设置输入框的提示文字和焦点变化效果
     * @param inputLayout 输入框布局
     * @param hint 提示文字
     * @param context 上下文
     */
    public static void setupInputLayout(TextInputLayout inputLayout, String hint, Context context) {
        if (inputLayout != null && inputLayout.getEditText() != null) {
            inputLayout.setHintEnabled(false);
            inputLayout.getEditText().setHint(hint);

            inputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    inputLayout.getEditText().setHint(s == null || s.length() == 0 ? hint : "");
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            inputLayout.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    inputLayout.setHintTextColor(context.getResources().getColorStateList(cn.itcast.wordmaster.R.color.white));
                    inputLayout.setBoxStrokeColor(context.getResources().getColor(cn.itcast.wordmaster.R.color.light_blue_border));
                } else {
                    inputLayout.setHintTextColor(context.getResources().getColorStateList(android.R.color.darker_gray));
                    inputLayout.setBoxStrokeColor(context.getResources().getColor(android.R.color.darker_gray));
                }
            });
        }
    }
}