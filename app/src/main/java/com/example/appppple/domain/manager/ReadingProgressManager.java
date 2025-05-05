package com.example.appppple.domain.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

/**
 * 阅读进度管理器
 */
public class ReadingProgressManager {
    private static final String TAG = "ReadingProgressManager";
    private static final String PREF_NAME = "reading_progress";
    private static final String KEY_BOOK_URI = "last_book_uri";
    private static final String KEY_BOOK_NAME = "last_book_name";
    private static final String KEY_CURRENT_PAGE = "current_page";
    private static final String KEY_TOTAL_PAGES = "total_pages";

    private final SharedPreferences preferences;
    private static ReadingProgressManager instance;

    private ReadingProgressManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ReadingProgressManager getInstance(Context context) {
        if (instance == null) {
            instance = new ReadingProgressManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 保存阅读进度
     */
    public void saveProgress(String bookName, Uri bookUri, int currentPage, int totalPages) {
        if (bookUri == null || bookName == null) {
            Log.w(TAG, "保存进度失败：书籍信息为空");
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_BOOK_URI, bookUri.toString());
        editor.putString(KEY_BOOK_NAME, bookName);
        editor.putInt(KEY_CURRENT_PAGE, currentPage);
        editor.putInt(KEY_TOTAL_PAGES, totalPages);
        editor.apply();

        Log.d(TAG, String.format("保存阅读进度 - 书名: %s, URI: %s, 当前页: %d/%d",
                bookName, bookUri, currentPage, totalPages));
    }

    /**
     * 获取上次阅读的书籍信息
     */
    public ReadingProgress getLastReadingProgress() {
        String bookUriStr = preferences.getString(KEY_BOOK_URI, null);
        String bookName = preferences.getString(KEY_BOOK_NAME, null);
        int currentPage = preferences.getInt(KEY_CURRENT_PAGE, 0);
        int totalPages = preferences.getInt(KEY_TOTAL_PAGES, 0);

        if (bookUriStr == null || bookName == null) {
            return null;
        }

        return new ReadingProgress(bookName, Uri.parse(bookUriStr), currentPage, totalPages);
    }

    /**
     * 清除阅读进度
     */
    public void clearProgress() {
        preferences.edit().clear().apply();
        Log.d(TAG, "清除阅读进度");
    }

    /**
     * 阅读进度数据类
     */
    public static class ReadingProgress {
        private final String bookName;
        private final Uri bookUri;
        private final int currentPage;
        private final int totalPages;

        public ReadingProgress(String bookName, Uri bookUri, int currentPage, int totalPages) {
            this.bookName = bookName;
            this.bookUri = bookUri;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
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
    }
} 