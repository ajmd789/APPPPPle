package com.example.appppple.domain.parser;

import com.example.appppple.domain.model.Book;

import java.io.File;
import java.io.IOException;

/**
 * 书籍解析器接口
 */
public interface BookParser {
    /**
     * 解析书籍文件
     * @param file 书籍文件
     * @return 解析后的书籍对象
     * @throws IOException 如果解析过程中发生IO错误
     */
    Book parse(File file) throws IOException;
} 