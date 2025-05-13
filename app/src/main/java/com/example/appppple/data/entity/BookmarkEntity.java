package com.example.appppple.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.appppple.data.converter.UriConverter;

/**
 * 书签实体类
 */
@Entity(tableName = "bookmarks")
public class BookmarkEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String bookName;
    
    @TypeConverters(UriConverter.class)
    private android.net.Uri bookUri;
    
    private int page;
    private long timestamp;

    public BookmarkEntity(String bookName, android.net.Uri bookUri, int page, long timestamp) {
        this.bookName = bookName;
        this.bookUri = bookUri;
        this.page = page;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public android.net.Uri getBookUri() {
        return bookUri;
    }

    public void setBookUri(android.net.Uri bookUri) {
        this.bookUri = bookUri;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 