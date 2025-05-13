package com.example.appppple.data.repository;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appppple.data.AppDatabase;
import com.example.appppple.data.dao.BookmarkDao;
import com.example.appppple.data.entity.BookmarkEntity;
import com.example.appppple.domain.model.Bookmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 书签仓库
 */
public class BookmarkRepository {
    private static BookmarkRepository instance;
    private final BookmarkDao bookmarkDao;
    private final ExecutorService executorService;

    private BookmarkRepository(Context context) {
        bookmarkDao = AppDatabase.getInstance(context).bookmarkDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized BookmarkRepository getInstance(Context context) {
        if (instance == null) {
            instance = new BookmarkRepository(context.getApplicationContext());
        }
        return instance;
    }

    public LiveData<List<Bookmark>> getBookmarksForBook(Uri bookUri) {
        MutableLiveData<List<Bookmark>> result = new MutableLiveData<>();
        executorService.execute(() -> {
            List<BookmarkEntity> entities = bookmarkDao.getBookmarksForBook(bookUri.toString());
            List<Bookmark> bookmarks = new ArrayList<>();
            for (BookmarkEntity entity : entities) {
                bookmarks.add(new Bookmark(
                    entity.getBookName(),
                    entity.getBookUri(),
                    entity.getPage(),
                    entity.getTimestamp()
                ));
            }
            result.postValue(bookmarks);
        });
        return result;
    }

    public LiveData<Boolean> isBookmarked(Uri bookUri, int page) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        executorService.execute(() -> {
            BookmarkEntity entity = bookmarkDao.getBookmark(bookUri.toString(), page);
            result.postValue(entity != null);
        });
        return result;
    }

    public LiveData<Boolean> toggleBookmark(String bookName, Uri bookUri, int page) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        executorService.execute(() -> {
            BookmarkEntity entity = bookmarkDao.getBookmark(bookUri.toString(), page);
            if (entity != null) {
                bookmarkDao.delete(entity);
                result.postValue(false);
            } else {
                entity = new BookmarkEntity(bookName, bookUri, page, System.currentTimeMillis());
                bookmarkDao.insert(entity);
                result.postValue(true);
            }
        });
        return result;
    }

    public LiveData<List<Bookmark>> getAllBookmarks() {
        MutableLiveData<List<Bookmark>> result = new MutableLiveData<>();
        executorService.execute(() -> {
            List<BookmarkEntity> entities = bookmarkDao.getAllBookmarks();
            List<Bookmark> bookmarks = new ArrayList<>();
            for (BookmarkEntity entity : entities) {
                bookmarks.add(new Bookmark(
                    entity.getBookName(),
                    entity.getBookUri(),
                    entity.getPage(),
                    entity.getTimestamp()
                ));
            }
            result.postValue(bookmarks);
        });
        return result;
    }
} 