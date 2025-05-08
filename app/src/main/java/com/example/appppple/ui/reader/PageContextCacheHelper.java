package com.example.appppple.ui.reader;

import android.content.Context;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 支持缓存当前页、总页数及10页上下文的磁盘工具
 */
public class PageContextCacheHelper {

    private static final String CACHE_DIR = "reading_cache";

    /**
     * 保存10页上下文+页码信息到磁盘
     * @param context 应用上下文
     * @param bookId  唯一id
     * @param currentPage 当前页号
     * @param totalPages  总页数
     * @param pageList     10页内容
     */
    public static void savePageContext(Context context, String bookId, int currentPage, int totalPages, List<String> pageList) {
        File dir = new File(context.getFilesDir(), CACHE_DIR);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, bookId + ".cache");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            // 第一行保存元信息
            bw.write(currentPage + "," + totalPages);
            bw.newLine();
            for (String page : pageList) {
                bw.write(page.replace("\n", "\\n"));
                bw.newLine();
            }
        } catch (IOException e) {
            // 可加日志
        }
    }

    /**
     * 加载缓存，返回[当前页, 总页数, 10页内容]
     */
    public static CacheResult loadPageContext(Context context, String bookId) {
        File dir = new File(context.getFilesDir(), CACHE_DIR);
        File file = new File(dir, bookId + ".cache");
        int currentPage = 0;
        int totalPages = 0;
        List<String> result = new ArrayList<>();
        if (!file.exists()) return new CacheResult(currentPage, totalPages, result);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String meta = br.readLine();
            if (meta != null && meta.contains(",")) {
                String[] arr = meta.split(",");
                try {
                    currentPage = Integer.parseInt(arr[0]);
                    totalPages = Integer.parseInt(arr[1]);
                } catch (NumberFormatException ignored) {}
            }
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line.replace("\\n", "\n"));
            }
        } catch (IOException e) {
            // 可加日志
        }
        return new CacheResult(currentPage, totalPages, result);
    }

    public static void clearPageContext(Context context, String bookId) {
        File dir = new File(context.getFilesDir(), CACHE_DIR);
        File file = new File(dir, bookId + ".cache");
        if (file.exists()) file.delete();
    }

    public static class CacheResult {
        public final int currentPage;
        public final int totalPages;
        public final List<String> pages;
        public CacheResult(int currentPage, int totalPages, List<String> pages) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.pages = pages;
        }
    }
} 