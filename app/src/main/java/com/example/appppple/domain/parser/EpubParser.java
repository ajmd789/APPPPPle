// domain/parser/EpubParser.java
package com.example.appppple.domain.parser;

import com.example.appppple.domain.model.Book;
import com.example.appppple.domain.model.Chapter;

import org.jsoup.Jsoup;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * EPUB文件解析器
 */
public class EpubParser implements BookParser {
    @Override
    public com.example.appppple.domain.model.Book parse(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("文件不存在");
        }

        // 使用Epub库解析
        nl.siegmann.epublib.domain.Book epubBook;
        EpubReader epubReader = new EpubReader();
        try (InputStream inputStream = new FileInputStream(file)) {
            epubBook = epubReader.readEpub(inputStream);
        }
        
        com.example.appppple.domain.model.Book book = new com.example.appppple.domain.model.Book();
        book.setTitle(epubBook.getTitle() != null ? epubBook.getTitle() : "未知标题");
        
        // 解析章节
        List<Chapter> chapters = new ArrayList<>();
        if (epubBook.getTableOfContents() != null) {
            for (TOCReference tocRef : epubBook.getTableOfContents().getTocReferences()) {
                if (tocRef != null) {
                    String title = tocRef.getTitle() != null ? tocRef.getTitle() : "未知章节";
                    String content = parseEpubContent(tocRef.getResource());
                    Chapter chapter = new Chapter(title, content);
                    chapters.add(chapter);
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
        
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            String rawContent = new String(data, resource.getInputEncoding());
            return Jsoup.parse(rawContent).text();
        }
    }
}