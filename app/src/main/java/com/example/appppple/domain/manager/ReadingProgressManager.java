package com.example.appppple.domain.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 阅读进度管理器
 */
public class ReadingProgressManager {
    private static final String TAG = "ReadingProgressManager";
    private static final String PREFS_NAME = "reading_progress";
    private static final String KEY_PROGRESS_LIST = "progress_list";
    private static ReadingProgressManager instance;
    private final SharedPreferences preferences;
    private final Gson gson;

    private static class UriTypeAdapter implements JsonSerializer<Uri>, JsonDeserializer<Uri> {
        @Override
        public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null || json.isJsonNull()) {
                return null;
            }
            
            if (json.isJsonPrimitive()) {
                String uriString = json.getAsString();
                return uriString.isEmpty() ? null : Uri.parse(uriString);
            }
            
            if (json.isJsonObject()) {
                JsonObject jsonObject = json.getAsJsonObject();
                if (jsonObject.has("uri")) {
                    String uriString = jsonObject.get("uri").getAsString();
                    return uriString.isEmpty() ? null : Uri.parse(uriString);
                }
            }
            
            Log.w(TAG, "无法解析 Uri JSON: " + json);
            return null;
        }
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

    private ReadingProgressManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriTypeAdapter())
                .create();
    }

    public static ReadingProgressManager getInstance(Context context) {
        if (instance == null) {
            instance = new ReadingProgressManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 保存阅读进度
     */
    public void saveProgress(String bookName, Uri bookUri, int currentPage, int totalPages) {
        List<ReadingProgress> progressList = getAllReadingProgress();
        
        // 查找是否已存在该书的进度
        ReadingProgress existingProgress = null;
        for (ReadingProgress progress : progressList) {
            if (progress.getBookUri().equals(bookUri)) {
                existingProgress = progress;
                break;
            }
        }

        // 创建新的进度记录
        ReadingProgress newProgress = new ReadingProgress(bookName, bookUri, currentPage, totalPages);

        // 如果已存在，则更新；否则添加新的
        if (existingProgress != null) {
            progressList.remove(existingProgress);
        }
        progressList.add(newProgress);

        // 按最后阅读时间排序
        Collections.sort(progressList, (p1, p2) -> 
            Long.compare(p2.getLastReadTime(), p1.getLastReadTime()));

        // 保存到 SharedPreferences
        String json = gson.toJson(progressList);
        preferences.edit().putString(KEY_PROGRESS_LIST, json).apply();
        
        Log.d(TAG, String.format("保存阅读进度 - 书名: %s, 当前页: %d/%d", 
            bookName, currentPage, totalPages));
    }

    public List<ReadingProgress> getAllReadingProgress() {
        String json = preferences.getString(KEY_PROGRESS_LIST, "[]");
        Type type = new TypeToken<List<ReadingProgress>>(){}.getType();
        List<ReadingProgress> progressList = gson.fromJson(json, type);
        
        // 过滤掉无效的进度记录
        if (progressList != null) {
            progressList.removeIf(progress -> 
                progress == null || 
                progress.getBookUri() == null || 
                progress.getBookName() == null || 
                progress.getBookName().isEmpty()
            );
        }
        
        return progressList != null ? progressList : new ArrayList<>();
    }

    /**
     * 获取上次阅读的书籍信息
     */
    public ReadingProgress getLastReadingProgress() {
        List<ReadingProgress> progressList = getAllReadingProgress();
        return !progressList.isEmpty() ? progressList.get(0) : null;
    }

    /**
     * 清除阅读进度
     */
    public void clearProgress(Uri bookUri) {
        List<ReadingProgress> progressList = getAllReadingProgress();
        progressList.removeIf(progress -> progress.getBookUri().equals(bookUri));
        
        String json = gson.toJson(progressList);
        preferences.edit().putString(KEY_PROGRESS_LIST, json).apply();
        
        Log.d(TAG, "清除阅读进度: " + bookUri);
    }
} 