package com.example.appppple.domain.parser;

import android.content.Context;
import android.net.Uri;

import com.example.appppple.domain.model.Book;

import java.io.IOException;

/**
 * 书籍解析器接口
 */
public interface BookParser {
    /**
     * 解析书籍文件
     * @param context 上下文
     * @param uri 文件 URI
     * @return 解析后的书籍对象
     * @throws IOException 如果解析过程中发生 IO 错误
     * @throws SecurityException 如果没有文件访问权限
     * @throws IllegalArgumentException 如果文件格式不正确
     */
    Book parse(Context context, Uri uri) throws IOException, SecurityException, IllegalArgumentException;

    /**
     * 获取解析器支持的文件类型
     * @return 支持的文件类型数组
     */
    String[] getSupportedMimeTypes();

    /**
     * 检查文件是否可以被解析
     * @param context 上下文
     * @param uri 文件 URI
     * @return 是否支持解析
     */
    boolean canParse(Context context, Uri uri);
} 