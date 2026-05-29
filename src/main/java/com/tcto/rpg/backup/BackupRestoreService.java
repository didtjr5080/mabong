package com.tcto.rpg.backup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BackupRestoreService {
    private final Path backupRoot;

    public BackupRestoreService(Path backupRoot) {
        this.backupRoot = backupRoot;
    }

    public Path resolveBackup(String backupId) throws IOException {
        Path path = backupRoot.resolve(backupId + ".zip");
        if (!Files.exists(path)) {
            throw new IOException("Backup not found: " + backupId);
        }
        return path;
    }
}
