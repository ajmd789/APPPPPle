package com.example.appppple.domain.parser;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * 解析器工厂类
 */
public class ParserFactory {
    private static final String TAG = "ParserFactory";
    private static final int BUFFER_SIZE = 8192; // 8KB 缓冲区
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB 最大文件大小

    /**
     * 根据文件类型创建对应的解析器
     * @param context 上下文
     * @param uri 文件 URI
     * @return 对应的解析器实例
     * @throws IllegalArgumentException 如果文件类型不支持
     * @throws IOException 如果文件大小超过限制
     */
    public static BookParser createParser(Context context, Uri uri) throws IOException {
        if (context == null) {
            throw new IllegalArgumentException("Context 不能为空");
        }
        if (uri == null) {
            throw new IllegalArgumentException("URI 不能为空");
        }

        // 检查文件大小
        checkFileSize(context, uri);

        String mimeType = getMimeType(context, uri);
        if (mimeType == null) {
            throw new IllegalArgumentException("无法获取文件类型");
        }

        switch (mimeType) {
            case "application/epub+zip":
                return new EpubParser();
            case "text/plain":
                return new TxtParser();
            default:
                throw new IllegalArgumentException("不支持的文件类型: " + mimeType);
        }
    }

    /**
     * 获取文件的 MIME 类型
     * @param context 上下文
     * @param uri 文件 URI
     * @return MIME 类型
     */
    private static String getMimeType(Context context, Uri uri) {
        String mimeType = null;
        
        try {
            // 尝试从 URI 获取 MIME 类型
            if (uri.getScheme() != null && uri.getScheme().equals("content")) {
                mimeType = context.getContentResolver().getType(uri);
            }
            
            // 如果无法从 URI 获取，尝试从文件扩展名获取
            if (mimeType == null) {
                String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                if (extension != null) {
                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取 MIME 类型失败", e);
        }
        
        return mimeType;
    }

    /**
     * 检查文件大小是否超过限制
     */
    private static void checkFileSize(Context context, Uri uri) throws IOException {
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
} 