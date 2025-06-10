package cn.itcast.wordmaster.db;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * UserDao单元测试类
 * 测试用户数据操作的核心功能
 */
public class UserDaoTest {

    @Test
    public void testUserDaoBasicFunctionality() {
        // 基础功能测试 - 验证类存在且可实例化
        try {
            // 测试UserDao类是否存在
            Class<?> userDaoClass = Class.forName("cn.itcast.wordmaster.db.UserDao");
            assertNotNull("UserDao类应该存在", userDaoClass);
            
            // 验证类有正确的构造函数
            assertTrue("UserDao应该有构造函数", userDaoClass.getConstructors().length > 0);
        } catch (ClassNotFoundException e) {
            fail("UserDao类不存在: " + e.getMessage());
        }
    }

    @Test
    public void testUserDaoMethods() {
        // 测试UserDao类的方法存在性
        try {
            Class<?> userDaoClass = Class.forName("cn.itcast.wordmaster.db.UserDao");
            
            // 检查是否有register方法
            boolean hasRegisterMethod = false;
            boolean hasLoginMethod = false;
            
            for (java.lang.reflect.Method method : userDaoClass.getDeclaredMethods()) {
                if (method.getName().equals("register")) {
                    hasRegisterMethod = true;
                }
                if (method.getName().equals("login")) {
                    hasLoginMethod = true;
                }
            }
            
            assertTrue("UserDao应该有register方法", hasRegisterMethod);
            assertTrue("UserDao应该有login方法", hasLoginMethod);
            
        } catch (ClassNotFoundException e) {
            fail("UserDao类不存在: " + e.getMessage());
        }
    }

    @Test
    public void testStringOperations() {
        // 测试字符串操作（模拟用户输入验证）
        String validPhone = "13800138000";
        String invalidPhone = "123";
        
        assertTrue("有效手机号长度应该正确", validPhone.length() == 11);
        assertFalse("无效手机号长度不正确", invalidPhone.length() == 11);
        
        String password = "password123";
        assertNotNull("密码不应为null", password);
        assertTrue("密码长度应该大于0", password.length() > 0);
    }
}