package com.example.appppple.domain.manager;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.appppple.data.repository.BookmarkRepository;
import com.example.appppple.domain.model.Bookmark;

import java.util.List;

/**
 * 书签管理器：负责书签的增删查与持久化
 */
public class BookmarkManager {
    private static final String TAG = "BookmarkManager";
    private static BookmarkManager instance;
    private final BookmarkRepository repository;

    private BookmarkManager(Context context) {
        repository = BookmarkRepository.getInstance(context);
    }

    public static BookmarkManager getInstance(Context context) {
        if (instance == null) {
            instance = new BookmarkManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 获取当前书籍的所有书签
     */
    public LiveData<List<Bookmark>> getBookmarksForBook(Uri bookUri) {
        return repository.getBookmarksForBook(bookUri);
    }

    /**
     * 查询当前页是否有书签
     */
    public LiveData<Boolean> isBookmarked(Uri bookUri, int page) {
        return repository.isBookmarked(bookUri, page);
    }

    /**
     * 添加或删除书签：若没有则添加，有则删除（切换效果）
     */
    public LiveData<Boolean> toggleBookmark(String bookName, Uri bookUri, int page) {
        LiveData<Boolean> result = repository.toggleBookmark(bookName, bookUri, page);
        result.observeForever(isAdded -> {
            Log.d(TAG, isAdded ? "添加书签" : "删除书签" + "，页码: " + page);
        });
        return result;
    }

    /**
     * 获取所有书签
     */
    public LiveData<List<Bookmark>> getAllBookmarks() {
        return repository.getAllBookmarks();
    }

} 