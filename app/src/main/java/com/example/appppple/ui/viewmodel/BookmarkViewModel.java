package com.example.appppple.ui.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appppple.domain.manager.BookmarkManager;
import com.example.appppple.domain.model.Bookmark;

import java.util.List;

/**
 * 书签 ViewModel
 */
public class BookmarkViewModel extends AndroidViewModel {
    private final BookmarkManager bookmarkManager;
    private final MutableLiveData<List<Bookmark>> currentBookmarks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> currentBookmarkState = new MutableLiveData<>();
    private final MediatorLiveData<List<Bookmark>> allBookmarks = new MediatorLiveData<>();

    public BookmarkViewModel(@NonNull Application application) {
        super(application);
        bookmarkManager = BookmarkManager.getInstance(application);
        allBookmarks.addSource(bookmarkManager.getAllBookmarks(), allBookmarks::setValue);
    }

    /**
     * 获取当前书籍的所有书签
     */
    public void loadBookmarksForBook(Uri bookUri) {
        bookmarkManager.getBookmarksForBook(bookUri).observeForever(currentBookmarks::setValue);
    }

    /**
     * 查询当前页是否有书签
     */
    public void checkBookmarkState(Uri bookUri, int page) {
        bookmarkManager.isBookmarked(bookUri, page).observeForever(currentBookmarkState::setValue);
    }

    /**
     * 添加或删除书签
     */
    public void toggleBookmark(String bookName, Uri bookUri, int page) {
        bookmarkManager.toggleBookmark(bookName, bookUri, page).observeForever(currentBookmarkState::setValue);
    }

    /**
     * 获取当前书籍的书签列表
     */
    public LiveData<List<Bookmark>> getCurrentBookmarks() {
        return currentBookmarks;
    }

    /**
     * 获取当前书签状态
     */
    public LiveData<Boolean> getCurrentBookmarkState() {
        return currentBookmarkState;
    }

    /**
     * 获取所有书签
     */
    public LiveData<List<Bookmark>> getAllBookmarks() {
        return allBookmarks;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理观察者
        currentBookmarks.setValue(null);
        currentBookmarkState.setValue(null);
        allBookmarks.setValue(null);
    }
} 