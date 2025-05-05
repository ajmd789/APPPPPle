package com.example.appppple.domain.parser;

public class ParserFactory {
    public static BookParser createParser(String filePath) {
        if (filePath.endsWith(".txt")) {
            return new TxtParser();
        } else if (filePath.endsWith(".epub")) {
            return new EpubParser();
        }
        throw new UnsupportedOperationException("Unsupported format");
    }
} 