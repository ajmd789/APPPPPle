package com.example.appppple.domain.parser;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.appppple.domain.model.Book;
import com.example.appppple.domain.model.Chapter;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * TXT 文件解析器
 */
public class TxtParser implements BookParser {
    private static final String TAG = "TxtParser";
    private static final int BUFFER_SIZE = 8192; // 8KB 缓冲区
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB 最大文件大小
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final int MEMORY_LOG_INTERVAL = 1000000; // 每读取 1MB 输出一次内存信息

    /**
     * 输出当前内存使用情况
     */
    private void logMemoryUsage(String stage) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        Log.d(TAG, String.format("内存使用情况 [%s] - 已用: %.2fMB, 空闲: %.2fMB, 总分配: %.2fMB, 最大可用: %.2fMB",
                stage,
                usedMemory / (1024.0 * 1024.0),
                freeMemory / (1024.0 * 1024.0),
                totalMemory / (1024.0 * 1024.0),
                maxMemory / (1024.0 * 1024.0)));
    }

    @Override
    public Book parse(Context context, Uri uri) throws IOException, SecurityException, IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("Context 不能为空");
        }
        if (uri == null) {
            throw new IllegalArgumentException("URI 不能为空");
        }

        // 输出初始内存使用情况
        logMemoryUsage("开始解析前");

        // 检查文件大小
        checkFileSize(context, uri);

        Book book = new Book();
        String fileName = getFileNameFromUri(context, uri);
        book.setTitle(fileName.replace(".txt", ""));
        
        List<Chapter> chapters = new ArrayList<>();
        
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("无法打开文件流");
            }
            
            // 使用 ByteArrayOutputStream 缓存内容
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytesRead = 0;
            long lastMemoryLogBytes = 0;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                
                // 每读取 1MB 输出一次内存信息
                if (totalBytesRead - lastMemoryLogBytes >= MEMORY_LOG_INTERVAL) {
                    logMemoryUsage("读取中 - 已读取: " + (totalBytesRead / (1024 * 1024)) + "MB");
                    lastMemoryLogBytes = totalBytesRead;
                }
                
                // 检查是否超过最大文件大小
                if (totalBytesRead > MAX_FILE_SIZE) {
                    throw new IOException("文件大小超过限制（50MB）");
                }
            }
            
            // 输出读取完成后的内存使用情况
            logMemoryUsage("文件读取完成");
            
            // 获取文件内容字节数组
            byte[] contentBytes = baos.toByteArray();
            
            // 输出内容转换前的内存使用情况
            logMemoryUsage("内容转换前");
            
            // 检测文件编码
            String encoding = detectEncoding(contentBytes);
            Log.d(TAG, "检测到的文件编码: " + encoding);
            
            // 使用检测到的编码读取内容
            String content = new String(contentBytes, Charset.forName(encoding));
            
            // 输出内容转换后的内存使用情况
            logMemoryUsage("内容转换后");
            
            // 将整个文件内容作为一个章节
            Chapter chapter = new Chapter("正文", content);
            chapters.add(chapter);
            book.setChapters(chapters);
            
            // 输出最终内存使用情况
            logMemoryUsage("解析完成");
            
        } catch (IOException e) {
            Log.e(TAG, "解析 TXT 文件失败", e);
            throw new IOException("解析 TXT 文件失败: " + e.getMessage());
        }
        
        return book;
    }

    /**
     * 使用 juniversalchardet 检测文件编码
     */
    private String detectEncoding(byte[] content) {
        UniversalDetector detector = new UniversalDetector(null);
        
        // 处理文件内容
        detector.handleData(content, 0, content.length);
        detector.dataEnd();
        
        String encoding = detector.getDetectedCharset();
        detector.reset();
        
        if (encoding == null) {
            Log.w(TAG, "无法检测文件编码，使用默认编码: " + DEFAULT_ENCODING);
            return DEFAULT_ENCODING;
        }
        
        // 确保返回的编码名称是有效的
        try {
            Charset.forName(encoding);
            return encoding;
        } catch (Exception e) {
            Log.w(TAG, "检测到的编码 " + encoding + " 无效，使用默认编码", e);
            return DEFAULT_ENCODING;
        }
    }

    @Override
    public String[] getSupportedMimeTypes() {
        return new String[]{"text/plain"};
    }

    @Override
    public boolean canParse(Context context, Uri uri) {
        if (context == null || uri == null) {
            return false;
        }
        
        try {
            String mimeType = context.getContentResolver().getType(uri);
            return mimeType != null && mimeType.equals("text/plain");
        } catch (Exception e) {
            Log.e(TAG, "检查文件类型失败", e);
            return false;
        }
    }

    /**
     * 检查文件大小是否超过限制
     */
    private void checkFileSize(Context context, Uri uri) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("无法打开文件流");
            }
            
            long fileSize = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileSize += bytesRead;
                if (fileSize > MAX_FILE_SIZE) {
                    throw new IOException("文件大小超过限制（50MB）");
                }
            }
        }
    }

    /**
     * 从 URI 中获取文件名
     */
    private String getFileNameFromUri(Context context, Uri uri) {
        String fileName = null;
        
        try {
            // 尝试从 URI 获取文件名
            if (uri.getScheme() != null && uri.getScheme().equals("content")) {
                try (android.database.Cursor cursor = context.getContentResolver().query(
                        uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if (nameIndex != -1) {
                            fileName = cursor.getString(nameIndex);
                        }
                    }
                }
            }
            
            // 如果无法从 URI 获取，尝试从路径获取
            if (fileName == null) {
                String path = uri.getPath();
                if (path != null) {
                    int lastSlash = path.lastIndexOf('/');
                    if (lastSlash != -1) {
                        fileName = path.substring(lastSlash + 1);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取文件名失败", e);
        }
        
        return fileName != null ? fileName : "未知文件";
    }
} 