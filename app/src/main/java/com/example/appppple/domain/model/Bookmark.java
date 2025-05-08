package com.example.appppple.domain.model;

import android.net.Uri;

/**
 * 书签数据模型
 */
public class Bookmark {
    private String bookName;
    private Uri bookUri;
    private int page;
    private long timestamp;

    public Bookmark(String bookName, Uri bookUri, int page, long timestamp) {
        this.bookName = bookName;
        this.bookUri = bookUri;
        this.page = page;
        this.timestamp = timestamp;
    }

    public String getBookName() {
        return bookName;
    }

    public Uri getBookUri() {
        return bookUri;
    }

    public int getPage() {
        return page;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public void setBookUri(Uri bookUri) {
        this.bookUri = bookUri;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Bookmark)) return false;
        Bookmark other = (Bookmark) obj;
        // 以书籍和页码为唯一性
        return bookUri != null && bookUri.equals(other.bookUri) && page == other.page;
    }

    @Override
    public int hashCode() {
        return (bookUri != null ? bookUri.hashCode() : 0) * 31 + page;
    }
} 