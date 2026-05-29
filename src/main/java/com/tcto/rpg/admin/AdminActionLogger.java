package com.tcto.rpg.admin;

import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class AdminActionLogger {
    private static final Path LOG_PATH = Path.of("logs", "tctorpg-admin.log");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AdminActionLogger() {
    }

    public static void log(ServerPlayer player, String action) {
        String name = player == null ? "Console" : player.getGameProfile().getName();
        log(name, action);
    }

    public static void log(String actor, String action) {
        try {
            Files.createDirectories(LOG_PATH.getParent());
            String line = "[" + LocalDateTime.now().format(FORMATTER) + "] Admin " + actor + " " + action + System.lineSeparator();
            Files.writeString(LOG_PATH, line, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }

    public static Path logPath() {
        return LOG_PATH;
    }
}
