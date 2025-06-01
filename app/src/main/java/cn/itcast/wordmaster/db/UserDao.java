package cn.itcast.wordmaster.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 用户数据操作类
 * 负责用户表的增删改查操作
 */
public class UserDao {
    private static final String TAG = "UserDao";
    private SQLiteDatabase db;

    public UserDao(Context context) {
        this.db = WordMasterDBHelper.getInstance(context).getDatabase();
    }

    /**
     * 注册新用户
     * @param phoneNumber 手机号
     * @param username 用户名
     * @param password 密码
     * @return 注册是否成功
     */

    public boolean register(String phoneNumber, String username, String password) {
        // 检查手机号是否已存在
        if (isPhoneNumberExists(phoneNumber)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("phoneNumber", phoneNumber);
        values.put("username", username);
        values.put("password", password);
        values.put("registerTime", System.currentTimeMillis());

        long result = db.insert("user", null, values);
        return result != -1;
    }

    /**
     * 用户登录验证
     * @param phoneNumber 手机号
     * @param password 密码
     * @return 登录是否成功
     */
    public boolean login(String phoneNumber, String password) {
        String[] columns = {"password"};
        String selection = "phoneNumber = ?";
        String[] selectionArgs = {phoneNumber};

        Cursor cursor = null;
        try {
            cursor = db.query("user", columns, selection, selectionArgs, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                int passwordIndex = cursor.getColumnIndex("password");
                if (passwordIndex >= 0) {
                    String storedPassword = cursor.getString(passwordIndex);
                    return password.equals(storedPassword);
                }
                return false;
            }
            return false;
        } catch (android.database.sqlite.SQLiteException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 检查手机号是否已存在
     * @param phoneNumber 手机号
     * @return 是否存在
     */
    private boolean isPhoneNumberExists(String phoneNumber) {
        String[] columns = {"phoneNumber"};
        String selection = "phoneNumber = ?";
        String[] selectionArgs = {phoneNumber};

        Cursor cursor = null;
        try {
            cursor = db.query("user", columns, selection, selectionArgs, null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } catch (android.database.sqlite.SQLiteException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    /**
     * 重置用户密码
     * @param phoneNumber 手机号
     * @param username 用户名
     * @param newPassword 新密码
     * @return 重置是否成功
     */
    public boolean resetPassword(String phoneNumber, String username, String newPassword) {
        // 验证手机号和用户名是否匹配
        String[] columns = {"username"};
        String selection = "phoneNumber = ?";
        String[] selectionArgs = {phoneNumber};
        
        Cursor cursor = null;
        try {
            cursor = db.query("user", columns, selection, selectionArgs, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                int usernameIndex = cursor.getColumnIndex("username");
                if (usernameIndex >= 0) {
                    String storedUsername = cursor.getString(usernameIndex);
                    if (!username.equals(storedUsername)) {
                        // 用户名不匹配
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                // 手机号不存在
                return false;
            }
        } catch (android.database.sqlite.SQLiteException e) {
            Log.e(TAG, "Error verifying user: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        // 更新密码
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        
        String updateSelection = "phoneNumber = ?";
        String[] updateSelectionArgs = {phoneNumber};
        
        int rowsAffected = db.update("user", values, updateSelection, updateSelectionArgs);
        return rowsAffected > 0;
    }
}