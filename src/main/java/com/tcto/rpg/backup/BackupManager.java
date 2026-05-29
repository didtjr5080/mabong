package com.tcto.rpg.backup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupManager {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");
    private final Path backupRoot;

    public BackupManager(Path backupRoot) {
        this.backupRoot = backupRoot;
    }

    public BackupMetadata create() throws IOException {
        Files.createDirectories(backupRoot);
        String id = LocalDateTime.now().format(FORMATTER);
        Path target = backupRoot.resolve(id + ".zip");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(target))) {
            addIfExists(zip, Path.of("data", "tctorpg", "rpg"));
            addIfExists(zip, Path.of("content_packs"));
            addIfExists(zip, Path.of("config", "tctorpg"));
            addIfExists(zip, Path.of("saves", "players"));
            addIfExists(zip, Path.of("operator"));
            addIfExists(zip, Path.of("logs", "tctorpg-admin.log"));
        }
        return new BackupMetadata(id, target, LocalDateTime.now(), Files.size(target));
    }

    public List<BackupMetadata> list() throws IOException {
        List<BackupMetadata> backups = new ArrayList<>();
        if (!Files.exists(backupRoot)) {
            return backups;
        }
        try (var paths = Files.list(backupRoot)) {
            paths.filter(path -> path.toString().endsWith(".zip")).forEach(path -> {
                try {
                    String fileName = path.getFileName().toString();
                    backups.add(new BackupMetadata(fileName.substring(0, fileName.length() - 4), path, LocalDateTime.now(), Files.size(path)));
                } catch (IOException ignored) {
                }
            });
        }
        return backups;
    }

    private static void addIfExists(ZipOutputStream zip, Path source) throws IOException {
        if (!Files.exists(source)) {
            return;
        }
        if (Files.isRegularFile(source)) {
            addFile(zip, source, source);
            return;
        }
        try (var paths = Files.walk(source)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                addFile(zip, source, path);
            }
        }
    }

    private static void addFile(ZipOutputStream zip, Path root, Path file) throws IOException {
        String name = root.getParent() == null ? root.relativize(file).toString() : root.getParent().relativize(file).toString();
        zip.putNextEntry(new ZipEntry(name.replace('\\', '/')));
        Files.copy(file, zip);
        zip.closeEntry();
    }
}
