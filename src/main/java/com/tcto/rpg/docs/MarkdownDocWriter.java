package com.tcto.rpg.docs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MarkdownDocWriter {
    public void write(Path file, String title, String body) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, "# " + title + System.lineSeparator() + System.lineSeparator() + body + System.lineSeparator());
    }
}
