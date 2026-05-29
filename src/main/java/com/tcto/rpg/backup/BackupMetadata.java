package com.tcto.rpg.backup;

import java.nio.file.Path;
import java.time.LocalDateTime;

public record BackupMetadata(String id, Path path, LocalDateTime createdAt, long sizeBytes) {
}
