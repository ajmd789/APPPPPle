package com.example.appppple.data.repository;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appppple.data.AppDatabase;
import com.example.appppple.data.dao.ReadingProgressDao;
import com.example.appppple.data.entity.ReadingProgressEntity;
import com.example.appppple.domain.manager.ReadingProgressManager.ReadingProgress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 阅读进度仓库
 */
public class ReadingProgressRepository {
    private static ReadingProgressRepository instance;
    private final ReadingProgressDao readingProgressDao;
    private final ExecutorService executorService;

    private ReadingProgressRepository(Context context) {
        readingProgressDao = AppDatabase.getInstance(context).readingProgressDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized ReadingProgressRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ReadingProgressRepository(context.getApplicationContext());
        }
        return instance;
    }

    public LiveData<ReadingProgress> getProgress(Uri bookUri) {
        MutableLiveData<ReadingProgress> result = new MutableLiveData<>();
        executorService.execute(() -> {
            ReadingProgressEntity entity = readingProgressDao.getProgress(bookUri.toString());
            if (entity != null) {
                result.postValue(new ReadingProgress(
                    entity.getBookName(),
                    entity.getBookUri(),
                    entity.getCurrentPage(),
                    entity.getTotalPages()
                ));
            } else {
                result.postValue(null);
            }
        });
        return result;
    }

    public LiveData<Boolean> saveProgress(String bookName, Uri bookUri, int currentPage, int totalPages) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        executorService.execute(() -> {
            try {
                // 先删除旧的阅读进度
                readingProgressDao.deleteProgress(bookUri.toString());

                // 然后插入新的阅读进度
                ReadingProgressEntity entity = new ReadingProgressEntity(
                    bookName,
                    bookUri,
                    currentPage,
                    totalPages
                );
                readingProgressDao.insert(entity);
                result.postValue(true);
            } catch (Exception e) {
                result.postValue(false);
            }
        });
        return result;
    }

    public LiveData<List<ReadingProgress>> getAllProgress() {
        MutableLiveData<List<ReadingProgress>> result = new MutableLiveData<>();
        executorService.execute(() -> {
            List<ReadingProgressEntity> entities = readingProgressDao.getAllProgress();
            List<ReadingProgress> progressList = new ArrayList<>();
            for (ReadingProgressEntity entity : entities) {
                progressList.add(new ReadingProgress(
                    entity.getBookName(),
                    entity.getBookUri(),
                    entity.getCurrentPage(),
                    entity.getTotalPages()
                ));
            }
            result.postValue(progressList);
        });
        return result;
    }

    public LiveData<ReadingProgress> getLastProgress() {
        MutableLiveData<ReadingProgress> result = new MutableLiveData<>();
        executorService.execute(() -> {
            ReadingProgressEntity entity = readingProgressDao.getLastProgress();
            if (entity != null) {
                result.postValue(new ReadingProgress(
                    entity.getBookName(),
                    entity.getBookUri(),
                    entity.getCurrentPage(),
                    entity.getTotalPages()
                ));
            } else {
                result.postValue(null);
            }
        });
        return result;
    }

    public LiveData<Boolean> clearProgress(Uri bookUri) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        executorService.execute(() -> {
            try {
                readingProgressDao.deleteProgress(bookUri.toString());
                result.postValue(true);
            } catch (Exception e) {
                result.postValue(false);
            }
        });
        return result;
    }
} 