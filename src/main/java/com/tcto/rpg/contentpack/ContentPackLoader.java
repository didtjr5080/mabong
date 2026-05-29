package com.tcto.rpg.contentpack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ContentPackLoader {
    public List<ContentPack> load(Path contentPacksRoot) throws IOException {
        List<ContentPack> packs = new ArrayList<>();
        if (!Files.exists(contentPacksRoot)) {
            return packs;
        }
        try (var paths = Files.list(contentPacksRoot)) {
            for (Path root : paths.filter(Files::isDirectory).toList()) {
                Path packJson = root.resolve("pack.json");
                if (!Files.exists(packJson)) {
                    continue;
                }
                JsonObject json = JsonParser.parseString(Files.readString(packJson)).getAsJsonObject();
                packs.add(new ContentPack(
                    get(json, "id", root.getFileName().toString()),
                    get(json, "name", root.getFileName().toString()),
                    get(json, "version", "1.0.0"),
                    !json.has("enabled") || json.get("enabled").getAsBoolean(),
                    json.has("priority") ? json.get("priority").getAsInt() : 0,
                    List.of(),
                    get(json, "description", ""),
                    root
                ));
            }
        }
        return packs;
    }

    private static String get(JsonObject json, String key, String fallback) {
        return json.has(key) ? json.get(key).getAsString() : fallback;
    }
}
