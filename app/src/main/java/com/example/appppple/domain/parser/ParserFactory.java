package com.example.appppple.domain.parser;

/**
 * 解析器工厂类
 */
public class ParserFactory {
    /**
     * 根据文件路径创建对应的解析器
     * @param filePath 文件路径
     * @return 对应的解析器实例
     * @throws UnsupportedOperationException 如果文件格式不支持
     */
    public static BookParser createParser(String filePath) {
        if (filePath.endsWith(".txt")) {
            return new TxtParser();
        } else if (filePath.endsWith(".epub")) {
            return new EpubParser();
        }
        throw new UnsupportedOperationException("Unsupported format");
    }
} 