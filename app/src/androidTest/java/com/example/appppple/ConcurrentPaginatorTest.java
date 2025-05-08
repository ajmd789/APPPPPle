package com.example.appppple;

import android.os.Handler;
import android.os.Looper;

import com.example.appppple.ui.reader.ConcurrentPaginator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ConcurrentPaginatorTest {
    private ConcurrentPaginator paginator;
    private Handler mainHandler;
    private CountDownLatch latch;

    @Before
    public void setup() {
        mainHandler = new Handler(Looper.getMainLooper());
        latch = new CountDownLatch(1);
    }

    @Test
    public void testPagination() throws InterruptedException {
        // 准备测试数据
        String testText = "这是第一页的内容。\n这是第一页的第二行。\n\n" +
                "这是第二页的内容。\n这是第二页的第二行。\n\n" +
                "这是第三页的内容。\n这是第三页的第二行。";

        // 创建分页器
        paginator = new ConcurrentPaginator(testText, 100, 50, 2, 0);

        // 设置回调
        paginator.setOnBlockReadyListener(new ConcurrentPaginator.OnBlockReadyListener() {
            @Override
            public void onBlockReady(List<String> pages) {
                assertNotNull(pages);
                assertTrue(pages.size() > 0);
                latch.countDown();
            }
        });

        // 开始分页
        paginator.startPaging();

        // 等待分页完成
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue("分页未在预期时间内完成", completed);
    }

    @Test
    public void testEmptyText() throws InterruptedException {
        paginator = new ConcurrentPaginator("", 100, 50, 2, 0);

        paginator.setOnBlockReadyListener(new ConcurrentPaginator.OnBlockReadyListener() {
            @Override
            public void onBlockReady(List<String> pages) {
                assertNotNull(pages);
                assertEquals(0, pages.size());
                latch.countDown();
            }
        });

        paginator.startPaging();
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue("分页未在预期时间内完成", completed);
    }

    @Test
    public void testLargeText() throws InterruptedException {
        StringBuilder largeText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeText.append("这是第").append(i).append("行测试文本。\n");
        }

        paginator = new ConcurrentPaginator(largeText.toString(), 100, 50, 4, 0);

        paginator.setOnBlockReadyListener(new ConcurrentPaginator.OnBlockReadyListener() {
            @Override
            public void onBlockReady(List<String> pages) {
                assertNotNull(pages);
                assertTrue(pages.size() > 0);
                latch.countDown();
            }
        });

        paginator.startPaging();
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue("分页未在预期时间内完成", completed);
    }
} 