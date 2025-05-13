package com.example.appppple.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.appppple.data.entity.ReadingProgressEntity;

import java.util.List;

/**
 * 阅读进度数据访问对象
 */
@Dao
public interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE bookUri = :bookUri")
    ReadingProgressEntity getProgress(String bookUri);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ReadingProgressEntity progress);

    @Update
    void update(ReadingProgressEntity progress);

    @Delete
    void delete(ReadingProgressEntity progress);

    @Query("DELETE FROM reading_progress WHERE bookUri = :bookUri")
    void deleteProgress(String bookUri);

    @Query("SELECT * FROM reading_progress ORDER BY lastReadTime DESC")
    List<ReadingProgressEntity> getAllProgress();

    @Query("SELECT * FROM reading_progress ORDER BY lastReadTime DESC LIMIT 1")
    ReadingProgressEntity getLastProgress();
} 