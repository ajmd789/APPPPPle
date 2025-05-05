// domain/parser/EpubParser.java
package com.example.appppple.domain.parser;

import com.example.appppple.domain.model.Book;
import com.example.appppple.domain.model.Chapter;

import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EPUB文件解析器
 */
public class EpubParser implements BookParser {
    @Override
    public Book parse(File file) throws IOException {
        // 使用Epub库解析
        EpubBook epubBook = EpubReader.readEpub(file);
        
        Book book = new Book();
        book.setTitle(epubBook.getTitle());
        
        // 解析章节
        List<Chapter> chapters = new ArrayList<>();
        for (EpubChapter epubChapter : epubBook.getChapters()) {
            Chapter chapter = new Chapter(
                epubChapter.getTitle(),
                parseEpubContent(epubChapter.getContent())
            );
            chapters.add(chapter);
        }
        book.setChapters(chapters);
        return book;
    }
    
    private String parseEpubContent(String rawContent) {
        // 清理HTML标签等处理
        return Jsoup.parse(rawContent).text();
    }
}