package com.tcto.rpg.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class AdminPermissionService {
    public static final String DEFAULT_OWNER_NAME = "stone_0401";
    private static final Path OPERATORS_PATH = Path.of("config", "tctorpg", "operators.json");
    private static final Gson GSON = new Gson();
    private static final Map<AdminPermissionLevel, Set<String>> permissions = new EnumMap<>(AdminPermissionLevel.class);

    static {
        for (AdminPermissionLevel level : AdminPermissionLevel.values()) {
            permissions.put(level, new HashSet<>());
        }
    }

    private AdminPermissionService() {
    }

    public static void load() {
        ensureDefaultFile();
        for (Set<String> users : permissions.values()) {
            users.clear();
        }
        permissions.get(AdminPermissionLevel.OWNER).add(normalize(DEFAULT_OWNER_NAME));

        try {
            JsonObject root = JsonParser.parseString(Files.readString(OPERATORS_PATH)).getAsJsonObject();
            loadArray(root, "owners", AdminPermissionLevel.OWNER);
            loadArray(root, "admins", AdminPermissionLevel.ADMIN);
            loadArray(root, "designers", AdminPermissionLevel.DESIGNER);
            loadArray(root, "balancers", AdminPermissionLevel.BALANCER);
            loadArray(root, "moderators", AdminPermissionLevel.MODERATOR);
            loadArray(root, "testers", AdminPermissionLevel.TESTER);
        } catch (Exception ignored) {
            permissions.get(AdminPermissionLevel.OWNER).add(normalize(DEFAULT_OWNER_NAME));
        }
    }

    public static boolean has(ServerPlayer player, AdminPermissionLevel required) {
        return levelOf(player).includes(required);
    }

    public static AdminPermissionLevel levelOf(ServerPlayer player) {
        String name = normalize(player.getGameProfile().getName());
        if (name.equals(normalize(DEFAULT_OWNER_NAME))) {
            return AdminPermissionLevel.OWNER;
        }
        for (int i = AdminPermissionLevel.values().length - 1; i >= 0; i--) {
            AdminPermissionLevel level = AdminPermissionLevel.values()[i];
            if (permissions.get(level).contains(name)) {
                return level;
            }
        }
        return AdminPermissionLevel.NONE;
    }

    public static Path operatorsPath() {
        return OPERATORS_PATH;
    }

    private static void ensureDefaultFile() {
        if (Files.exists(OPERATORS_PATH)) {
            return;
        }
        try {
            Files.createDirectories(OPERATORS_PATH.getParent());
            JsonObject root = new JsonObject();
            root.add("owners", GSON.toJsonTree(new String[] { DEFAULT_OWNER_NAME }));
            root.add("admins", GSON.toJsonTree(new String[0]));
            root.add("designers", GSON.toJsonTree(new String[0]));
            root.add("balancers", GSON.toJsonTree(new String[0]));
            root.add("moderators", GSON.toJsonTree(new String[0]));
            root.add("testers", GSON.toJsonTree(new String[0]));
            Files.writeString(OPERATORS_PATH, GSON.toJson(root));
        } catch (IOException ignored) {
        }
    }

    private static void loadArray(JsonObject root, String key, AdminPermissionLevel level) {
        if (!root.has(key) || !root.get(key).isJsonArray()) {
            return;
        }
        root.getAsJsonArray(key).forEach(element -> permissions.get(level).add(normalize(element.getAsString())));
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
