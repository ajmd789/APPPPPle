package com.example.appppple.domain.manager;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.appppple.data.repository.ReadingProgressRepository;

import java.util.List;

/**
 * 阅读进度管理器
 */
public class ReadingProgressManager {
    private static final String TAG = "ReadingProgressManager";
    private static ReadingProgressManager instance;
    private final ReadingProgressRepository repository;

    private ReadingProgressManager(Context context) {
        repository = ReadingProgressRepository.getInstance(context);
    }

    public LiveData<ReadingProgress> getProgress(Uri bookUri) {
        return repository.getProgress(bookUri);
    }

    public static ReadingProgressManager getInstance(Context context) {
        if (instance == null) {
            instance = new ReadingProgressManager(context.getApplicationContext());
        }
        return instance;
    }

    public static class ReadingProgress {
        private final String bookName;
        private final Uri bookUri;
        private final int currentPage;
        private final int totalPages;
        private final long lastReadTime;

        public ReadingProgress(String bookName, Uri bookUri, int currentPage, int totalPages) {
            this.bookName = bookName;
            this.bookUri = bookUri;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.lastReadTime = System.currentTimeMillis();
        }

        public String getBookName() {
            return bookName;
        }

        public Uri getBookUri() {
            return bookUri;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public long getLastReadTime() {
            return lastReadTime;
        }
    }

    /**
     * 保存阅读进度
     */
    public LiveData<Boolean> saveProgress(String bookName, Uri bookUri, int currentPage, int totalPages) {
        LiveData<Boolean> result = repository.saveProgress(bookName, bookUri, currentPage, totalPages);
        result.observeForever(success -> {
            if (success) {
                Log.d(TAG, String.format("保存阅读进度 - 书名: %s, 当前页: %d/%d", 
                    bookName, currentPage, totalPages));
            }
        });
        return result;
    }

    /**
     * 获取所有阅读进度
     */
    public LiveData<List<ReadingProgress>> getAllReadingProgress() {
        return repository.getAllProgress();
    }

    /**
     * 获取上次阅读的书籍信息
     */
    public LiveData<ReadingProgress> getLastReadingProgress() {
        return repository.getLastProgress();
    }

    /**
     * 清除阅读进度
     */
    public LiveData<Boolean> clearProgress(Uri bookUri) {
        LiveData<Boolean> result = repository.clearProgress(bookUri);
        result.observeForever(success -> {
            if (success) {
                Log.d(TAG, "清除阅读进度: " + bookUri);
            }
        });
        return result;
    }
} 