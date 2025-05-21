package cn.itcast.wordmaster.entity;

/**
 * 单词实体类
 * 对应数据库中的word表
 */
public class Word {
    private int wordId;
    private String spelling;
    private String phonetic;
    private String meaning;
    private String bookId;
    private int correctCount;
    private String lastReviewed;
    private int nextDueOffset;

    public Word() {
    }

    public Word(int wordId, String spelling, String phonetic, String meaning, String bookId,
                int correctCount, String lastReviewed, int nextDueOffset) {
        this.wordId = wordId;
        this.spelling = spelling;
        this.phonetic = phonetic;
        this.meaning = meaning;
        this.bookId = bookId;
        this.correctCount = correctCount;
        this.lastReviewed = lastReviewed;
        this.nextDueOffset = nextDueOffset;
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public String getSpelling() {
        return spelling;
    }

    public void setSpelling(String spelling) {
        this.spelling = spelling;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
        this.correctCount = correctCount;
        this.lastReviewed = lastReviewed;
        this.nextDueOffset = nextDueOffset;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public String getLastReviewed() {
        return lastReviewed;
    }

    public void setLastReviewed(String lastReviewed) {
        this.lastReviewed = lastReviewed;
    }

    public int getNextDueOffset() {
        return nextDueOffset;
    }

    public void setNextDueOffset(int nextDueOffset) {
        this.nextDueOffset = nextDueOffset;
    }

    @Override
    public String toString() {
        return "Word{" +
                "wordId=" + wordId +
                ", spelling='" + spelling + '\'' +
                ", phonetic='" + phonetic + '\'' +
                ", meaning='" + meaning + '\'' +
                ", bookId='" + bookId + '\'' +
                '}';
    }
}