package com.example.appppple;

import android.content.Context;
import android.net.Uri;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.appppple.domain.manager.BookmarkManager;
import com.example.appppple.domain.model.Bookmark;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class BookmarkManagerTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private BookmarkManager bookmarkManager;
    private Context context;
    private static final String testBookName = "测试书籍";
    private static final Uri testUri = Uri.parse("content://test/book");

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        bookmarkManager = BookmarkManager.getInstance(context);
    }

    @Test
    public void testAddAndGetBookmark() throws InterruptedException {
        // 添加书签
        bookmarkManager.toggleBookmark(testBookName, testUri, 1)
            .observeForever(success -> assertTrue(success));

        // 获取书签列表
        CountDownLatch latch = new CountDownLatch(1);
        bookmarkManager.getBookmarksForBook(testUri).observeForever(bookmarks -> {
            assertNotNull(bookmarks);
            assertEquals(1, bookmarks.size());
            assertEquals(1, bookmarks.get(0).getPage());
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testRemoveBookmark() throws InterruptedException {
        // 先添加书签
        bookmarkManager.toggleBookmark(testBookName, testUri, 1)
            .observeForever(success -> assertTrue(success));

        // 再删除书签
        bookmarkManager.toggleBookmark(testBookName, testUri, 1)
            .observeForever(success -> assertTrue(success));

        // 检查书签是否已删除
        CountDownLatch latch = new CountDownLatch(1);
        bookmarkManager.getBookmarksForBook(testUri).observeForever(bookmarks -> {
            assertNotNull(bookmarks);
            assertTrue(bookmarks.isEmpty());
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testIsBookmarked() throws InterruptedException {
        // 添加书签
        bookmarkManager.toggleBookmark(testBookName, testUri, 1)
            .observeForever(success -> assertTrue(success));

        // 检查书签状态
        CountDownLatch latch1 = new CountDownLatch(1);
        bookmarkManager.isBookmarked(testUri, 1).observeForever(isBookmarked -> {
            assertTrue(isBookmarked);
            latch1.countDown();
        });
        assertTrue(latch1.await(5, TimeUnit.SECONDS));

        CountDownLatch latch2 = new CountDownLatch(1);
        bookmarkManager.isBookmarked(testUri, 2).observeForever(isBookmarked -> {
            assertFalse(isBookmarked);
            latch2.countDown();
        });
        assertTrue(latch2.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testGetAllBookmarks() throws InterruptedException {
        // 添加多个书签
        bookmarkManager.toggleBookmark(testBookName, testUri, 1)
            .observeForever(success -> assertTrue(success));
        bookmarkManager.toggleBookmark(testBookName, testUri, 2)
            .observeForever(success -> assertTrue(success));
        bookmarkManager.toggleBookmark(testBookName, testUri, 3)
            .observeForever(success -> assertTrue(success));

        // 获取所有书签
        CountDownLatch latch = new CountDownLatch(1);
        bookmarkManager.getAllBookmarks().observeForever(bookmarks -> {
            assertNotNull(bookmarks);
            assertEquals(3, bookmarks.size());
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
} 