package com.tcto.rpg.template;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class TemplateManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, String> FOLDERS = Map.of(
        "skill", "skills",
        "monster", "monsters",
        "boss", "bosses",
        "quest", "quests",
        "dungeon", "dungeons",
        "item", "items",
        "job", "jobs",
        "event", "events"
    );

    private final Path root;

    public TemplateManager(Path root) {
        this.root = root;
    }

    public Path createTemplate(String kind, String id, String option) throws IOException {
        String folder = FOLDERS.get(kind);
        if (folder == null) {
            throw new IllegalArgumentException("Unknown template kind: " + kind);
        }
        Path path = root.resolve("default").resolve(folder).resolve(id + ".json");
        if (Files.exists(path)) {
            throw new IOException("Template already exists: " + path);
        }
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(template(kind, id, option)));
        return path;
    }

    static JsonObject template(String kind, String id, String option) {
        return switch (kind) {
            case "skill" -> new SkillTemplateGenerator().generate(id, option);
            case "monster" -> new MonsterTemplateGenerator().generate(id, option);
            case "boss" -> new BossTemplateGenerator().generate(id, option);
            case "quest" -> new QuestTemplateGenerator().generate(id, option);
            case "dungeon" -> new DungeonTemplateGenerator().generate(id, option);
            case "item" -> new ItemTemplateGenerator().generate(id, option);
            case "job" -> new JobTemplateGenerator().generate(id, option);
            case "event" -> new EventTemplateGenerator().generate(id, option);
            default -> throw new IllegalArgumentException(kind);
        };
    }

    static JsonArray array(String... values) {
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }
}
