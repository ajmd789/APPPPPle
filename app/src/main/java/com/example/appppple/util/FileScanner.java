// FileScanner.java
package com.example.appppple.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class FileScanner {
    private static final String[] TARGET_MIME = {
            "application/epub+zip",
            "text/plain"
    };

    private final ContentResolver resolver;

    public FileScanner(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public List<Uri> scanForBooks() {
        Log.d("FileScanner", "开始扫描文件");
        List<Uri> results = new ArrayList<>();

        Cursor cursor = resolver.query(

                MediaStore.Files.getContentUri("external"),
                new String[] { MediaStore.Files.FileColumns._ID },
                MediaStore.Files.FileColumns.MIME_TYPE + " IN (?, ?)",
                TARGET_MIME,
                null);
        Log.d("FileScanner", "获得Cursor: " + (cursor != null));
        if (cursor != null) {
            Log.d("FileScanner", "找到文件数量: " + cursor.getCount());
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"), id);
                results.add(uri);
            }
            cursor.close();
        }
        Log.d("FileScanner", "扫描完成，找到文件数: " + results.size());
        return results;
    }
}