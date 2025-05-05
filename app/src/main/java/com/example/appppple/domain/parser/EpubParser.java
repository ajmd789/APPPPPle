// domain/parser/EpubParser.java
package com.example.appppple.domain.parser;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.appppple.domain.model.Book;
import com.example.appppple.domain.model.Chapter;

import org.jsoup.Jsoup;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * EPUB文件解析器
 */
public class EpubParser implements BookParser {
    private static final String TAG = "EpubParser";
    private static final int BUFFER_SIZE = 8192; // 8KB 缓冲区
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB 最大文件大小

    @Override
    public Book parse(Context context, Uri uri) throws IOException, SecurityException, IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("Context 不能为空");
        }
        if (uri == null) {
            throw new IllegalArgumentException("URI 不能为空");
        }

        // 检查文件大小
        checkFileSize(context, uri);

        // 使用Epub库解析
        nl.siegmann.epublib.domain.Book epubBook;
        EpubReader epubReader = new EpubReader();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("无法打开文件流");
            }
            epubBook = epubReader.readEpub(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "解析 EPUB 文件失败", e);
            throw new IOException("解析 EPUB 文件失败: " + e.getMessage());
        }
        
        if (epubBook == null) {
            throw new IOException("EPUB 文件解析失败");
        }
        
        Book book = new Book();
        book.setTitle(epubBook.getTitle() != null ? epubBook.getTitle() : "未知标题");
        
        // 解析章节
        List<Chapter> chapters = new ArrayList<>();
        if (epubBook.getTableOfContents() != null) {
            for (TOCReference tocRef : epubBook.getTableOfContents().getTocReferences()) {
                if (tocRef != null) {
                    try {
                        String title = tocRef.getTitle() != null ? tocRef.getTitle() : "未知章节";
                        String content = parseEpubContent(tocRef.getResource());
                        Chapter chapter = new Chapter(title, content);
                        chapters.add(chapter);
                    } catch (IOException e) {
                        Log.e(TAG, "解析章节失败: " + tocRef.getTitle(), e);
                        // 继续处理其他章节
                    }
                }
            }
        }
        book.setChapters(chapters);
        return book;
    }
    
    private String parseEpubContent(Resource resource) throws IOException {
        if (resource == null) {
            return "";
        }
        
        StringBuilder content = new StringBuilder();
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                content.append(new String(buffer, 0, bytesRead, resource.getInputEncoding()));
            }
        }
        
        return Jsoup.parse(content.toString()).text();
    }

    @Override
    public String[] getSupportedMimeTypes() {
        return new String[]{"application/epub+zip"};
    }

    @Override
    public boolean canParse(Context context, Uri uri) {
        if (context == null || uri == null) {
            return false;
        }
        
        try {
            String mimeType = context.getContentResolver().getType(uri);
            return mimeType != null && mimeType.equals("application/epub+zip");
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
}