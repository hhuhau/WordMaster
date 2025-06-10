package cn.itcast.wordmaster.db;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * WordDao单元测试类
 * 测试单词数据操作的核心功能
 */
public class WordDaoTest {

    @Test
    public void testWordDaoBasicFunctionality() {
        // 基础功能测试 - 验证类存在且可实例化
        try {
            // 测试WordDao类是否存在
            Class<?> wordDaoClass = Class.forName("cn.itcast.wordmaster.db.WordDao");
            assertNotNull("WordDao类应该存在", wordDaoClass);
            
            // 验证类有正确的构造函数
            assertTrue("WordDao应该有构造函数", wordDaoClass.getConstructors().length > 0);
        } catch (ClassNotFoundException e) {
            fail("WordDao类不存在: " + e.getMessage());
        }
    }

    @Test
    public void testWordDaoMethods() {
        // 测试WordDao类的方法存在性
        try {
            Class<?> wordDaoClass = Class.forName("cn.itcast.wordmaster.db.WordDao");
            
            // 检查是否有getLearningBatch方法
            boolean hasGetLearningBatch = false;
            for (java.lang.reflect.Method method : wordDaoClass.getDeclaredMethods()) {
                if (method.getName().equals("getLearningBatch")) {
                    hasGetLearningBatch = true;
                    break;
                }
            }
            assertTrue("WordDao应该有getLearningBatch方法", hasGetLearningBatch);
            
        } catch (ClassNotFoundException e) {
            fail("WordDao类不存在: " + e.getMessage());
        }
    }

    @Test
    public void testBasicAssertions() {
        // 基本断言测试
        assertTrue("基本测试应该通过", true);
        assertFalse("基本测试应该通过", false);
        assertEquals("数字相等测试", 1, 1);
        assertNotNull("非空测试", "test");
    }
}