package com.example.appppple.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.appppple.data.converter.UriConverter;

/**
 * 阅读进度实体类
 */
@Entity(tableName = "reading_progress")
public class ReadingProgressEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String bookName;
    
    @TypeConverters(UriConverter.class)
    private android.net.Uri bookUri;
    
    private int currentPage;
    private int totalPages;
    private long lastReadTime;

    public ReadingProgressEntity(String bookName, android.net.Uri bookUri, int currentPage, int totalPages) {
        this.bookName = bookName;
        this.bookUri = bookUri;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.lastReadTime = System.currentTimeMillis();
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

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getLastReadTime() {
        return lastReadTime;
    }

    public void setLastReadTime(long lastReadTime) {
        this.lastReadTime = lastReadTime;
    }
} 