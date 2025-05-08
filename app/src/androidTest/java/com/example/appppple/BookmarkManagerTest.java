package com.example.appppple;

import android.content.Context;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.appppple.domain.manager.BookmarkManager;
import com.example.appppple.domain.model.Bookmark;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BookmarkManagerTest {
    private BookmarkManager bookmarkManager;
    private Context context;
    private Uri testUri;
    private String testBookName;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        bookmarkManager = BookmarkManager.getInstance(context);
        testUri = Uri.parse("content://test/book1");
        testBookName = "测试书籍";
    }

    @Test
    public void testAddAndGetBookmark() {
        // 添加书签
        bookmarkManager.toggleBookmark(testBookName, testUri, 1);
        
        // 获取书签列表
        List<Bookmark> bookmarks = bookmarkManager.getBookmarksForBook(testUri);
        
        // 验证
        assertNotNull(bookmarks);
        assertEquals(1, bookmarks.size());
        assertEquals(testBookName, bookmarks.get(0).getBookName());
        assertEquals(testUri, bookmarks.get(0).getBookUri());
        assertEquals(1, bookmarks.get(0).getPage());
    }

    @Test
    public void testRemoveBookmark() {
        // 添加书签
        bookmarkManager.toggleBookmark(testBookName, testUri, 1);
        
        // 移除书签
        bookmarkManager.toggleBookmark(testBookName, testUri, 1);
        
        // 获取书签列表
        List<Bookmark> bookmarks = bookmarkManager.getBookmarksForBook(testUri);
        
        // 验证
        assertNotNull(bookmarks);
        assertTrue(bookmarks.isEmpty());
    }

    @Test
    public void testIsBookmarked() {
        // 添加书签
        bookmarkManager.toggleBookmark(testBookName, testUri, 1);
        
        // 验证书签状态
        assertTrue(bookmarkManager.isBookmarked(testUri, 1));
        assertFalse(bookmarkManager.isBookmarked(testUri, 2));
    }

    @Test
    public void testMultipleBookmarks() {
        // 添加多个书签
        bookmarkManager.toggleBookmark(testBookName, testUri, 1);
        bookmarkManager.toggleBookmark(testBookName, testUri, 2);
        bookmarkManager.toggleBookmark(testBookName, testUri, 3);
        
        // 获取书签列表
        List<Bookmark> bookmarks = bookmarkManager.getBookmarksForBook(testUri);
        
        // 验证
        assertNotNull(bookmarks);
        assertEquals(3, bookmarks.size());
        
        // 验证页码顺序
        assertEquals(1, bookmarks.get(0).getPage());
        assertEquals(2, bookmarks.get(1).getPage());
        assertEquals(3, bookmarks.get(2).getPage());
    }
} 