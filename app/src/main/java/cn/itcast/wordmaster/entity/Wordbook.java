package cn.itcast.wordmaster.entity;

/**
 * 词典实体类
 * 对应数据库中的wordbook表
 */
public class Wordbook {
    private String bookId;
    private String bookName;
    private String description;
    private int wordCount;
    private String difficulty;
    private String coverImageUrl;

    public Wordbook() {
    }

    public Wordbook(String bookId, String bookName, String description, int wordCount, String difficulty, String coverImageUrl) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.description = description;
        this.wordCount = wordCount;
        this.difficulty = difficulty;
        this.coverImageUrl = coverImageUrl;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    @Override
    public String toString() {
        return "Wordbook{" +
                "bookId='" + bookId + '\'' +
                ", bookName='" + bookName + '\'' +
                ", description='" + description + '\'' +
                ", wordCount=" + wordCount +
                ", difficulty='" + difficulty + '\'' +
                ", coverImageUrl='" + coverImageUrl + '\'' +
                '}';
    }
}