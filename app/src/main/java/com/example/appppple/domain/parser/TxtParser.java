package com.example.appppple.domain.parser;

import com.example.appppple.domain.model.Book;
import com.example.appppple.domain.model.Chapter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TXT文件解析器
 */
public class TxtParser implements BookParser {
    private static final String CHAPTER_PATTERN = "^第[零一二三四五六七八九十百千]+章\\s+.*";
    
    @Override
    public Book parse(File file) throws IOException {
        Book book = new Book();
        book.setTitle(file.getName());
        
        // 读取文件内容
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();
        
        // 分章节处理
        List<Chapter> chapters = splitChapters(content.toString());
        book.setChapters(chapters);
        return book;
    }
    
    /**
     * 基于正则表达式分章
     */
    private List<Chapter> splitChapters(String content) {
        List<Chapter> chapters = new ArrayList<>();
        String[] lines = content.split("\n");
        
        Chapter currentChapter = null;
        for (String line : lines) {
            if (line.matches(CHAPTER_PATTERN)) {
                if (currentChapter != null) {
                    chapters.add(currentChapter);
                }
                currentChapter = new Chapter(line.trim(), "");
            } else if (currentChapter != null) {
                currentChapter.appendContent(line + "\n");
            }
        }
        if (currentChapter != null) {
            chapters.add(currentChapter);
        }
        return chapters;
    }
} 