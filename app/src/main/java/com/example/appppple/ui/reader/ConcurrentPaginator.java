package com.example.appppple.ui.reader;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 支持大文本多线程分块分页、首屏优先渲染的工具类
 * —— 逐行注释版
 */
public class ConcurrentPaginator {

    /**
     * 分块分页完成的回调接口
     */
    public interface OnBlockReadyListener {
        /**
         * 某个块分页完成时回调
         * @param blockIndex  块索引
         * @param pages       当前块分页结果
         * @param isFirstBlock 是否为首屏优先块
         */
        void onBlockReady(int blockIndex, List<String> pages, boolean isFirstBlock);

        /**
         * 全部块分页完成时回调
         * @param allPages 所有页合并后的结果
         */
        void onAllBlocksReady(List<String> allPages);
    }

    // 原始待分页文本
    private final String rawText;
    // 单页字符数
    private final int charsPerPage;
    // 单块分块大小
    private final int blockSize;
    // 线程池线程数
    private final int threadPoolSize;
    // 优先处理块的索引（如首屏/进度块）
    private final int priorityBlockIndex;
    // 分块分页的线程池
    private final ExecutorService executor;
    // 主线程handler，用于回调刷新UI
    private final Handler mainHandler;
    // 每个块分页结果缓存（线程安全）
    private final ConcurrentHashMap<Integer, List<String>> blockPagesMap = new ConcurrentHashMap<>();
    // 块总数
    private int totalBlocks = 0;
    // 已完成块数量
    private volatile int blocksReady = 0;
    // 分块完成回调
    private OnBlockReadyListener blockReadyListener;

    /**
     * 构造方法
     * @param rawText           原始文本
     * @param charsPerPage      单页字符数
     * @param blockSize         单块字符数
     * @param threadPoolSize    线程池大小
     * @param priorityBlockIndex 优先处理块索引
     */
    public ConcurrentPaginator(String rawText, int charsPerPage, int blockSize, int threadPoolSize, int priorityBlockIndex) {
        this.rawText = rawText;
        this.charsPerPage = charsPerPage;
        this.blockSize = blockSize;
        this.threadPoolSize = threadPoolSize;
        this.priorityBlockIndex = priorityBlockIndex;
        this.executor = Executors.newFixedThreadPool(threadPoolSize); // 创建线程池
        this.mainHandler = new Handler(Looper.getMainLooper()); // 主线程handler
    }

    /**
     * 设置分块分页完成的回调
     * @param listener 回调接口
     */
    public void setOnBlockReadyListener(OnBlockReadyListener listener) {
        this.blockReadyListener = listener;
    }

    /**
     * 启动并发分页主流程
     */
    public void startPaging() {
        // 1. 先将原始文本切分成多个块
        List<Block> blocks = splitTextToBlocks(rawText, blockSize);
        totalBlocks = blocks.size();

        // 2. 构建分页任务列表
        List<Future<?>> futures = new ArrayList<>();
        // 2.1 优先提交首屏块任务
        if (priorityBlockIndex >= 0 && priorityBlockIndex < blocks.size()) {
            futures.add(executor.submit(() -> pageBlock(blocks.get(priorityBlockIndex), true)));
        }
        // 2.2 其他块并发提交
        for (int i = 0; i < blocks.size(); i++) {
            if (i == priorityBlockIndex) continue; // 跳过已提交的优先块
            final int idx = i;
            futures.add(executor.submit(() -> pageBlock(blocks.get(idx), false)));
        }

        // 3. 等待所有分页任务完成后，主线程合并最终结果
        executor.submit(() -> {
            try {
                for (Future<?> f : futures) f.get(); // 阻塞直到所有任务完成
                List<String> allPages = collectAllPages(blocks.size());
                // 回调通知全部完成
                if (blockReadyListener != null) {
                    mainHandler.post(() -> blockReadyListener.onAllBlocksReady(allPages));
                }
            } catch (Exception ignored) {}
        });
    }

    /**
     * 对单个块进行分页，并缓存结果
     * @param block         待分页的块
     * @param isFirstBlock  是否为首屏优先块
     */
    private void pageBlock(Block block, boolean isFirstBlock) {
        List<String> pages = new ArrayList<>();
        String text = block.text;
        int len = text.length();
        // 按单页字符数分页
        for (int i = 0; i < len; i += charsPerPage) {
            int end = Math.min(i + charsPerPage, len);
            pages.add(text.substring(i, end));
        }
        // 缓存分页结果
        blockPagesMap.put(block.index, pages);
        blocksReady++;
        // 分块完成回调主线程
        if (blockReadyListener != null) {
            mainHandler.post(() -> blockReadyListener.onBlockReady(block.index, pages, isFirstBlock));
        }
    }

    /**
     * 将原始文本切分为多个块
     * @param text       原始文本
     * @param blockSize  单块大小
     * @return 块列表
     */
    private List<Block> splitTextToBlocks(String text, int blockSize) {
        List<Block> blocks = new ArrayList<>();
        int len = text.length();
        int index = 0;
        for (int i = 0; i < len; i += blockSize) {
            int end = Math.min(i + blockSize, len);
            blocks.add(new Block(index++, text.substring(i, end)));
        }
        return blocks;
    }

    /**
     * 合并所有块的分页结果
     * @param blockCount 块总数
     * @return 合并后的页列表
     */
    private List<String> collectAllPages(int blockCount) {
        List<String> allPages = new ArrayList<>();
        for (int i = 0; i < blockCount; i++) {
            List<String> pages = blockPagesMap.get(i);
            if (pages != null) {
                allPages.addAll(pages);
            }
        }
        return allPages;
    }

    /**
     * 单个块的数据结构
     */
    public static class Block {
        public final int index; // 块索引
        public final String text; // 块内容
        public Block(int index, String text) {
            this.index = index;
            this.text = text;
        }
    }

    /**
     * 停止分页并关闭线程池
     */
    public void shutdown() {
        executor.shutdownNow();
    }
} 