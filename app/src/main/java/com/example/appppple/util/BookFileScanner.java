// File: BookFileScanner.java
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
                Log.e(TAG, "Scan failed", e);
                callback.onScanError(e);
            }
        }).start();
    }

    private List<Uri> scanForBooks() {
        List<Uri> results = new ArrayList<>();
        String[] projection = {MediaStore.Files.FileColumns._ID};
        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " IN (?, ?)";
        String[] selectionArgs = {"application/epub+zip", "text/plain"};

        try (Cursor cursor = mResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null)) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    Uri uri = ContentUris.withAppendedId(
                            MediaStore.Files.getContentUri("external"), id);
                    results.add(uri);
                }
            }
        }
        return results;
    }
}