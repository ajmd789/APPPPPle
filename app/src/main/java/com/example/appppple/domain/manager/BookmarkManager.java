package com.example.appppple.domain.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.example.appppple.domain.model.Bookmark;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 书签管理器
 */
public class BookmarkManager {
    private static final String TAG = "BookmarkManager";
    private static final String PREFS_NAME = "bookmarks";
    private static final String KEY_BOOKMARK_LIST = "bookmark_list";
    private static BookmarkManager instance;
    private final SharedPreferences preferences;
    private final Gson gson;

    private BookmarkManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriTypeAdapter())
                .create();
    }

    public static BookmarkManager getInstance(Context context) {
        if (instance == null) {
            instance = new BookmarkManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 添加书签
     */
    public void addBookmark(String bookName, Uri bookUri, int pageNumber, String note) {
        List<Bookmark> bookmarkList = getAllBookmarks();
        
        // 检查是否已存在相同页面的书签
        for (Bookmark bookmark : bookmarkList) {
            if (bookmark.getBookUri().equals(bookUri) && bookmark.getPageNumber() == pageNumber) {
                Log.d(TAG, "该页面已存在书签");
                return;
            }
        }

        // 创建新书签
        Bookmark newBookmark = new Bookmark(bookName, bookUri, pageNumber, note);
        bookmarkList.add(newBookmark);

        // 保存到 SharedPreferences
        String json = gson.toJson(bookmarkList);
        preferences.edit().putString(KEY_BOOKMARK_LIST, json).apply();
        
        Log.d(TAG, String.format("添加书签 - 书名: %s, 页码: %d", bookName, pageNumber));
    }

    /**
     * 获取所有书签
     */
    public List<Bookmark> getAllBookmarks() {
        String json = preferences.getString(KEY_BOOKMARK_LIST, "[]");
        Type type = new TypeToken<List<Bookmark>>(){}.getType();
        List<Bookmark> bookmarkList = gson.fromJson(json, type);
        return bookmarkList != null ? bookmarkList : new ArrayList<>();
    }

    /**
     * 获取指定书籍的书签列表
     */
    public List<Bookmark> getBookmarksByUri(Uri bookUri) {
        List<Bookmark> allBookmarks = getAllBookmarks();
        List<Bookmark> bookBookmarks = new ArrayList<>();
        
        for (Bookmark bookmark : allBookmarks) {
            if (bookmark.getBookUri().equals(bookUri)) {
                bookBookmarks.add(bookmark);
            }
        }
        
        // 按页码排序
        Collections.sort(bookBookmarks, Comparator.comparingInt(Bookmark::getPageNumber));
        return bookBookmarks;
    }

    /**
     * 删除书签
     */
    public void removeBookmark(Uri bookUri, int pageNumber) {
        List<Bookmark> bookmarkList = getAllBookmarks();
        bookmarkList.removeIf(bookmark -> 
            bookmark.getBookUri().equals(bookUri) && bookmark.getPageNumber() == pageNumber);
        
        String json = gson.toJson(bookmarkList);
        preferences.edit().putString(KEY_BOOKMARK_LIST, json).apply();
        
        Log.d(TAG, String.format("删除书签 - 页码: %d", pageNumber));
    }

    /**
     * 检查指定页面是否有书签
     */
    public boolean hasBookmark(Uri bookUri, int pageNumber) {
        List<Bookmark> bookmarkList = getAllBookmarks();
        for (Bookmark bookmark : bookmarkList) {
            if (bookmark.getBookUri().equals(bookUri) && bookmark.getPageNumber() == pageNumber) {
                return true;
            }
        }
        return false;
    }
} 