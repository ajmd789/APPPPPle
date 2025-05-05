package com.example.appppple.domain.parser;

import java.io.File;
import java.io.IOException;

public interface BookParser {
    Book parse(File file) throws IOException;
} 