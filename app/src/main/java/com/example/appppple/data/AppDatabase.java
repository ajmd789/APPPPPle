package com.example.appppple.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.appppple.data.converter.UriConverter;
import com.example.appppple.data.dao.BookmarkDao;
import com.example.appppple.data.dao.ReadingProgressDao;
import com.example.appppple.data.entity.BookmarkEntity;
import com.example.appppple.data.entity.ReadingProgressEntity;

/**
 * 应用数据库
 */
@Database(
    entities = {
        BookmarkEntity.class,
        ReadingProgressEntity.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters({UriConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "appppple.db";
    private static volatile AppDatabase instance;

    public abstract BookmarkDao bookmarkDao();
    public abstract ReadingProgressDao readingProgressDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                DATABASE_NAME
            ).build();
        }
        return instance;
    }
} 