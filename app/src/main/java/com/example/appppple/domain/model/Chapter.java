package com.example.appppple.domain.model;

/**
 * 章节模型类
 */
public class Chapter {
    private String mTitle;
    private String mContent;

    public Chapter(String title, String content) {
        this.mTitle = title;
        this.mContent = content;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    /**
     * 追加内容
     * @param content 要追加的内容
     */
    public void appendContent(String content) {
        if (mContent == null) {
            mContent = content;
        } else {
            mContent += content;
        }
    }
} 