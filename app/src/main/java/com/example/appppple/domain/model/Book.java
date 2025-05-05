package com.example.appppple.domain.model;

import java.util.List;

/**
 * 书籍模型类
 */
public class Book {
    private String mTitle;
    private List<Chapter> mChapters;
    private String mAuthor;
    private String mCoverPath;
    private long mLastReadTime;
    private int mLastReadPosition;

    public Book() {
        mLastReadTime = System.currentTimeMillis();
        mLastReadPosition = 0;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public List<Chapter> getChapters() {
        return mChapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.mChapters = chapters;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        this.mAuthor = author;
    }

    public String getCoverPath() {
        return mCoverPath;
    }

    public void setCoverPath(String coverPath) {
        this.mCoverPath = coverPath;
    }

    public long getLastReadTime() {
        return mLastReadTime;
    }

    public void setLastReadTime(long lastReadTime) {
        this.mLastReadTime = lastReadTime;
    }

    public int getLastReadPosition() {
        return mLastReadPosition;
    }

    public void setLastReadPosition(int lastReadPosition) {
        this.mLastReadPosition = lastReadPosition;
    }

    /**
     * 获取书籍内容
     * @return 所有章节内容的组合
     */
    public String getContent() {
        if (mChapters == null || mChapters.isEmpty()) {
            return "";
        }

        StringBuilder content = new StringBuilder();
        for (Chapter chapter : mChapters) {
            content.append(chapter.getTitle()).append("\n\n");
            content.append(chapter.getContent()).append("\n\n");
        }
        return content.toString();
    }
} 