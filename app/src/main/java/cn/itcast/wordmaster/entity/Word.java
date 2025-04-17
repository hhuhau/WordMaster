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

    public Word() {
    }

    public Word(int wordId, String spelling, String phonetic, String meaning, String bookId) {
        this.wordId = wordId;
        this.spelling = spelling;
        this.phonetic = phonetic;
        this.meaning = meaning;
        this.bookId = bookId;
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