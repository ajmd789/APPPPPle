package com.example.appppple.domain.model;

import android.net.Uri;

/**
 * 书签模型类
 */
public class Bookmark {
    private final String bookName;
    private final Uri bookUri;
    private final int pageNumber;
    private final String note;
    private final long createTime;

    public Bookmark(String bookName, Uri bookUri, int pageNumber, String note) {
        this.bookName = bookName;
        this.bookUri = bookUri;
        this.pageNumber = pageNumber;
        this.note = note;
        this.createTime = System.currentTimeMillis();
    }

    public String getBookName() {
        return bookName;
    }

    public Uri getBookUri() {
        return bookUri;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public String getNote() {
        return note;
    }

    public long getCreateTime() {
        return createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bookmark bookmark = (Bookmark) o;
        return pageNumber == bookmark.pageNumber &&
                bookUri.equals(bookmark.bookUri);
    }

    @Override
    public int hashCode() {
        return 31 * bookUri.hashCode() + pageNumber;
    }
} 