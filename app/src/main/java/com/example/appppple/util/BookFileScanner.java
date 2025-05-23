package com.example.appppple.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BookFileScanner {
    private static final String TAG = "BookFileScanner";
    private final ContentResolver mResolver;

    public interface ScanResultCallback {
        void onScanComplete(List<Uri> bookUris);
        void onScanError(Exception e);
    }

    public BookFileScanner(ContentResolver resolver) {
        this.mResolver = resolver;
    }

    public void scanBooksAsync(ScanResultCallback callback) {
        new Thread(() -> {
            try {
                List<Uri> result = scanForBooks();
                callback.onScanComplete(result);
            } catch (Exception e) {
                callback.onScanError(e);
            }
        }).start();
    }

    private List<Uri> scanForBooks() {
        List<Uri> results = new ArrayList<>();
        Log.d(TAG, "开始扫描电子书文件...");

        String[] projection = {MediaStore.Files.FileColumns._ID};
        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " IN (?, ?)";
        String[] selectionArgs = {"application/epub+zip", "text/plain"};

        try (Cursor cursor = mResolver.query(
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                projection,
                selection,
                selectionArgs,
                null)) {
            while (cursor != null && cursor.moveToNext()) {
                long id = cursor.getLong(0);
                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), id);
                Log.d(TAG, "找到电子书文件: " + uri);
                results.add(uri);
            }
            Log.d(TAG, "扫描完成，共找到 " + results.size() + " 个文件");
        } catch (Exception e) {
            Log.e(TAG, "扫描错误: " + e.getMessage());
        }
        return results;
    }
}