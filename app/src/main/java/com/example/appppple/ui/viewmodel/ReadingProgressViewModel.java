package com.example.appppple.ui.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appppple.domain.manager.ReadingProgressManager;
import com.example.appppple.domain.manager.ReadingProgressManager.ReadingProgress;

import java.util.List;

/**
 * 阅读进度 ViewModel
 */
public class ReadingProgressViewModel extends AndroidViewModel {
    private final ReadingProgressManager readingProgressManager;
    private final MutableLiveData<ReadingProgress> currentProgress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationResult = new MutableLiveData<>();
    private final MediatorLiveData<List<ReadingProgress>> allProgress = new MediatorLiveData<>();

    public ReadingProgressViewModel(@NonNull Application application) {
        super(application);
        readingProgressManager = ReadingProgressManager.getInstance(application);
        allProgress.addSource(readingProgressManager.getAllReadingProgress(), allProgress::setValue);
    }

    /**
     * 加载当前书籍的阅读进度
     */
    public void loadProgress(Uri bookUri) {
        readingProgressManager.getLastReadingProgress().observeForever(currentProgress::setValue);
    }

    /**
     * 保存阅读进度
     */
    public void saveProgress(String bookName, Uri bookUri, int currentPage, int totalPages) {
        readingProgressManager.saveProgress(bookName, bookUri, currentPage, totalPages)
            .observeForever(operationResult::setValue);
    }

    /**
     * 清除阅读进度
     */
    public void clearProgress(Uri bookUri) {
        readingProgressManager.clearProgress(bookUri).observeForever(operationResult::setValue);
    }

    /**
     * 获取当前阅读进度
     */
    public LiveData<ReadingProgress> getCurrentProgress() {
        return currentProgress;
    }

    /**
     * 获取操作结果
     */
    public LiveData<Boolean> getOperationResult() {
        return operationResult;
    }

    /**
     * 获取所有阅读进度
     */
    public LiveData<List<ReadingProgress>> getAllProgress() {
        return allProgress;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理观察者
        currentProgress.setValue(null);
        operationResult.setValue(null);
        allProgress.setValue(null);
    }
} 