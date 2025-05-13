package com.example.appppple.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.appppple.data.entity.BookmarkEntity;

import java.util.List;

/**
 * 书签数据访问对象
 */
@Dao
public interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE bookUri = :bookUri")
    List<BookmarkEntity> getBookmarksForBook(String bookUri);

    @Query("SELECT * FROM bookmarks WHERE bookUri = :bookUri AND page = :page")
    BookmarkEntity getBookmark(String bookUri, int page);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BookmarkEntity bookmark);

    @Update
    void update(BookmarkEntity bookmark);

    @Delete
    void delete(BookmarkEntity bookmark);

    @Query("DELETE FROM bookmarks WHERE bookUri = :bookUri AND page = :page")
    void deleteBookmark(String bookUri, int page);

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    List<BookmarkEntity> getAllBookmarks();
} 