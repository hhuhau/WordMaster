package cn.itcast.wordmaster.entity;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Word实体类单元测试
 * 测试Word类的基本功能
 */
public class WordTest {

    private Word word;

    @Before
    public void setUp() {
        word = new Word();
    }

    @Test
    public void testDefaultConstructor() {
        // 测试默认构造函数
        Word newWord = new Word();
        assertNotNull("Word实例不应为null", newWord);
        assertEquals("默认wordId应为0", 0, newWord.getWordId());
        assertNull("默认spelling应为null", newWord.getSpelling());
        assertNull("默认phonetic应为null", newWord.getPhonetic());
        assertNull("默认meaning应为null", newWord.getMeaning());
        assertNull("默认bookId应为null", newWord.getBookId());
        assertEquals("默认correctCount应为0", 0, newWord.getCorrectCount());
        assertNull("默认lastReviewed应为null", newWord.getLastReviewed());
        assertEquals("默认nextDueOffset应为0", 0, newWord.getNextDueOffset());
    }

    @Test
    public void testParameterizedConstructor() {
        // 测试带参数的构造函数
        int wordId = 1;
        String spelling = "apple";
        String phonetic = "/ˈæpl/";
        String meaning = "苹果";
        String bookId = "book1";
        int correctCount = 2;
        String lastReviewed = "1234567890";
        int nextDueOffset = 3;

        Word paramWord = new Word(wordId, spelling, phonetic, meaning, bookId, 
                                 correctCount, lastReviewed, nextDueOffset);

        assertEquals("wordId应正确设置", wordId, paramWord.getWordId());
        assertEquals("spelling应正确设置", spelling, paramWord.getSpelling());
        assertEquals("phonetic应正确设置", phonetic, paramWord.getPhonetic());
        assertEquals("meaning应正确设置", meaning, paramWord.getMeaning());
        assertEquals("bookId应正确设置", bookId, paramWord.getBookId());
        assertEquals("correctCount应正确设置", correctCount, paramWord.getCorrectCount());
        assertEquals("lastReviewed应正确设置", lastReviewed, paramWord.getLastReviewed());
        assertEquals("nextDueOffset应正确设置", nextDueOffset, paramWord.getNextDueOffset());
    }

    @Test
    public void testWordIdGetterSetter() {
        // 测试wordId的getter和setter
        int testId = 123;
        word.setWordId(testId);
        assertEquals("wordId应正确设置和获取", testId, word.getWordId());
    }

    @Test
    public void testSpellingGetterSetter() {
        // 测试spelling的getter和setter
        String testSpelling = "hello";
        word.setSpelling(testSpelling);
        assertEquals("spelling应正确设置和获取", testSpelling, word.getSpelling());
    }

    @Test
    public void testPhoneticGetterSetter() {
        // 测试phonetic的getter和setter
        String testPhonetic = "/həˈloʊ/";
        word.setPhonetic(testPhonetic);
        assertEquals("phonetic应正确设置和获取", testPhonetic, word.getPhonetic());
    }

    @Test
    public void testMeaningGetterSetter() {
        // 测试meaning的getter和setter
        String testMeaning = "你好";
        word.setMeaning(testMeaning);
        assertEquals("meaning应正确设置和获取", testMeaning, word.getMeaning());
    }

    @Test
    public void testBookIdGetterSetter() {
        // 测试bookId的getter和setter
        String testBookId = "book123";
        word.setBookId(testBookId);
        assertEquals("bookId应正确设置和获取", testBookId, word.getBookId());
    }

    @Test
    public void testCorrectCountGetterSetter() {
        // 测试correctCount的getter和setter
        int testCount = 5;
        word.setCorrectCount(testCount);
        assertEquals("correctCount应正确设置和获取", testCount, word.getCorrectCount());
    }

    @Test
    public void testLastReviewedGetterSetter() {
        // 测试lastReviewed的getter和setter
        String testTime = "1609459200000";
        word.setLastReviewed(testTime);
        assertEquals("lastReviewed应正确设置和获取", testTime, word.getLastReviewed());
    }

    @Test
    public void testNextDueOffsetGetterSetter() {
        // 测试nextDueOffset的getter和setter
        int testOffset = 7;
        word.setNextDueOffset(testOffset);
        assertEquals("nextDueOffset应正确设置和获取", testOffset, word.getNextDueOffset());
    }

    @Test
    public void testNullValues() {
        // 测试null值的处理
        word.setSpelling(null);
        word.setPhonetic(null);
        word.setMeaning(null);
        word.setBookId(null);
        word.setLastReviewed(null);

        assertNull("spelling可以设置为null", word.getSpelling());
        assertNull("phonetic可以设置为null", word.getPhonetic());
        assertNull("meaning可以设置为null", word.getMeaning());
        assertNull("bookId可以设置为null", word.getBookId());
        assertNull("lastReviewed可以设置为null", word.getLastReviewed());
    }

    @Test
    public void testEmptyStringValues() {
        // 测试空字符串的处理
        word.setSpelling("");
        word.setPhonetic("");
        word.setMeaning("");
        word.setBookId("");
        word.setLastReviewed("");

        assertEquals("spelling可以设置为空字符串", "", word.getSpelling());
        assertEquals("phonetic可以设置为空字符串", "", word.getPhonetic());
        assertEquals("meaning可以设置为空字符串", "", word.getMeaning());
        assertEquals("bookId可以设置为空字符串", "", word.getBookId());
        assertEquals("lastReviewed可以设置为空字符串", "", word.getLastReviewed());
    }

    @Test
    public void testNegativeValues() {
        // 测试负数值的处理
        word.setWordId(-1);
        word.setCorrectCount(-1);
        word.setNextDueOffset(-1);

        assertEquals("wordId可以设置为负数", -1, word.getWordId());
        assertEquals("correctCount可以设置为负数", -1, word.getCorrectCount());
        assertEquals("nextDueOffset可以设置为负数", -1, word.getNextDueOffset());
    }

    @Test
    public void testToString() {
        // 测试toString方法
        word.setWordId(1);
        word.setSpelling("test");
        word.setMeaning("测试");
        
        String result = word.toString();
        assertNotNull("toString不应返回null", result);
        assertTrue("toString应包含类名", result.contains("Word"));
    }
}