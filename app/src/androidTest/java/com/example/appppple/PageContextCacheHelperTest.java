package com.example.appppple;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.appppple.ui.reader.PageContextCacheHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PageContextCacheHelperTest {
    private Context context;
    private String testBookId;
    private List<String> testPageList;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        testBookId = "test_book_1";
        testPageList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            testPageList.add("第" + (i + 1) + "页内容");
        }
    }

    @Test
    public void testSaveAndLoadPageContext() {
        // 保存页面上下文
        PageContextCacheHelper.savePageContext(context, testBookId, 5, 100, testPageList);

        // 加载页面上下文
        PageContextCacheHelper.CacheResult result = PageContextCacheHelper.loadPageContext(context, testBookId);

        // 验证结果
        assertNotNull(result);
        assertEquals(5, result.getCurrentPage());
        assertEquals(100, result.getTotalPages());
        assertNotNull(result.getPageList());
        assertEquals(10, result.getPageList().size());
        assertEquals("第1页内容", result.getPageList().get(0));
    }

    @Test
    public void testClearPageContext() {
        // 保存页面上下文
        PageContextCacheHelper.savePageContext(context, testBookId, 5, 100, testPageList);

        // 清除页面上下文
        PageContextCacheHelper.clearPageContext(context, testBookId);

        // 尝试加载已清除的上下文
        PageContextCacheHelper.CacheResult result = PageContextCacheHelper.loadPageContext(context, testBookId);

        // 验证结果
        assertNull(result);
    }

    @Test
    public void testEmptyPageList() {
        // 保存空页面列表
        PageContextCacheHelper.savePageContext(context, testBookId, 1, 10, new ArrayList<>());

        // 加载页面上下文
        PageContextCacheHelper.CacheResult result = PageContextCacheHelper.loadPageContext(context, testBookId);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCurrentPage());
        assertEquals(10, result.getTotalPages());
        assertNotNull(result.getPageList());
        assertTrue(result.getPageList().isEmpty());
    }

    @Test
    public void testLargePageList() {
        // 创建大量页面
        List<String> largePageList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largePageList.add("第" + (i + 1) + "页内容");
        }

        // 保存页面上下文
        PageContextCacheHelper.savePageContext(context, testBookId, 50, 100, largePageList);

        // 加载页面上下文
        PageContextCacheHelper.CacheResult result = PageContextCacheHelper.loadPageContext(context, testBookId);

        // 验证结果
        assertNotNull(result);
        assertEquals(50, result.getCurrentPage());
        assertEquals(100, result.getTotalPages());
        assertNotNull(result.getPageList());
        assertEquals(100, result.getPageList().size());
    }
} 