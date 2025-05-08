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
import java.util.Iterator;
import java.util.List;

/**
 * 书签管理器：负责书签的增删查与持久化
 */
public class BookmarkManager {
    private static final String TAG = "BookmarkManager";
    private static final String PREFS_NAME = "bookmark_prefs";
    private static final String KEY_BOOKMARKS = "bookmarks";
    private static BookmarkManager instance;
    private final SharedPreferences preferences;
    private final Gson gson;

    private BookmarkManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder().create();
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
    public List<Bookmark> getBookmarksForBook(Uri bookUri) {
        List<Bookmark> all = getAllBookmarks();
        List<Bookmark> result = new ArrayList<>();
        for (Bookmark b : all) {
            if (b.getBookUri() != null && b.getBookUri().equals(bookUri)) {
                result.add(b);
            }
        }
        return result;
    }

    /**
     * 查询当前页是否有书签
     */
    public boolean isBookmarked(Uri bookUri, int page) {
        List<Bookmark> all = getAllBookmarks();
        for (Bookmark b : all) {
            if (b.getBookUri() != null && b.getBookUri().equals(bookUri) && b.getPage() == page) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加或删除书签：若没有则添加，有则删除（切换效果）
     */
    public boolean toggleBookmark(String bookName, Uri bookUri, int page) {
        List<Bookmark> all = getAllBookmarks();
        Iterator<Bookmark> it = all.iterator();
        boolean removed = false;
        while (it.hasNext()) {
            Bookmark b = it.next();
            if (b.getBookUri() != null && b.getBookUri().equals(bookUri) && b.getPage() == page) {
                it.remove();
                removed = true;
            }
        }
        if (!removed) {
            all.add(new Bookmark(bookName, bookUri, page, System.currentTimeMillis()));
        }
        saveAllBookmarks(all);
        Log.d(TAG, removed ? "删除书签" : "添加书签" + "，页码: " + page);
        return !removed; // true=添加，false=删除
    }

    /**
     * 获取所有书签
     */
    public List<Bookmark> getAllBookmarks() {
        String json = preferences.getString(KEY_BOOKMARKS, "[]");
        Type type = new TypeToken<List<Bookmark>>(){}.getType();
        List<Bookmark> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    private void saveAllBookmarks(List<Bookmark> bookmarks) {
        String json = gson.toJson(bookmarks);
        preferences.edit().putString(KEY_BOOKMARKS, json).apply();
    }
} 