package com.tcto.rpg.common.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class RpgDataManager extends SimpleJsonResourceReloadListener {
    public static final RpgDataManager INSTANCE = new RpgDataManager();

    private final Map<String, JobDefinition> jobs = new HashMap<>();
    private final Map<String, SkillDefinition> skills = new HashMap<>();
    private final Map<String, EquipmentDefinition> equipment = new HashMap<>();
    private final Map<String, MonsterDefinition> monsters = new HashMap<>();
    private final Map<String, BossDefinition> bosses = new HashMap<>();
    private final Map<String, QuestDefinition> quests = new HashMap<>();
    private final ExpTable expTable = new ExpTable();

    private RpgDataManager() {
        super(new Gson(), "rpg");
    }

    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        jobs.clear();
        skills.clear();
        equipment.clear();
        monsters.clear();
        bosses.clear();
        quests.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            ResourceLocation location = entry.getKey();
            String path = location.getPath();
            if (!entry.getValue().isJsonObject()) {
                continue;
            }
            JsonObject json = entry.getValue().getAsJsonObject();
            if (path.startsWith("jobs/")) {
                String id = resolveId(location.getNamespace(), path.substring("jobs/".length()));
                jobs.put(id, JobDefinition.fromJson(id, json));
            } else if (path.startsWith("skills/")) {
                String id = resolveId(location.getNamespace(), path.substring("skills/".length()));
                skills.put(id, SkillDefinition.fromJson(id, json));
            } else if (path.startsWith("items/")) {
                String id = resolveId(location.getNamespace(), path.substring("items/".length()));
                equipment.put(id, EquipmentDefinition.fromJson(id, json));
            } else if (path.startsWith("monsters/")) {
                String id = resolveId(location.getNamespace(), path.substring("monsters/".length()));
                monsters.put(id, MonsterDefinition.fromJson(id, json));
            } else if (path.startsWith("bosses/")) {
                String id = resolveId(location.getNamespace(), path.substring("bosses/".length()));
                bosses.put(id, BossDefinition.fromJson(id, json));
            } else if (path.startsWith("quests/")) {
                String id = resolveId(location.getNamespace(), path.substring("quests/".length()));
                quests.put(id, QuestDefinition.fromJson(id, json));
            } else if (path.equals("exp_table")) {
                expTable.loadFromJson(json);
            }
        }
        loadExternalContentPacks(Path.of("content_packs"));
    }

    private static String resolveId(String namespace, String path) {
        return namespace + ":" + path;
    }

    public JobDefinition getJob(String id) {
        return jobs.get(id);
    }

    public SkillDefinition getSkill(String id) {
        return skills.get(id);
    }

    public Collection<SkillDefinition> skills() {
        return skills.values();
    }

    public EquipmentDefinition getEquipment(String id) {
        return equipment.get(id);
    }

    public MonsterDefinition getMonster(String id) {
        return monsters.get(id);
    }

    public BossDefinition getBoss(String id) {
        return bosses.get(id);
    }

    public QuestDefinition getQuest(String id) {
        return quests.get(id);
    }

    public ExpTable getExpTable() {
        return expTable;
    }

    public void loadExternalContentPacks(Path root) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> packs = Files.list(root)) {
            packs.filter(Files::isDirectory)
                .sorted()
                .forEach(this::loadExternalPack);
        } catch (IOException ignored) {
            // Runtime content packs are optional; validation reports detailed file errors.
        }
    }

    private void loadExternalPack(Path packRoot) {
        loadExternalCategory(packRoot.resolve("jobs"), "jobs");
        loadExternalCategory(packRoot.resolve("skills"), "skills");
        loadExternalCategory(packRoot.resolve("items"), "items");
        loadExternalCategory(packRoot.resolve("quests"), "quests");
    }

    private void loadExternalCategory(Path folder, String category) {
        if (!Files.isDirectory(folder)) {
            return;
        }
        try (Stream<Path> files = Files.walk(folder)) {
            files.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".json"))
                .forEach(path -> loadExternalJson(folder, path, category));
        } catch (IOException ignored) {
            // Keep reload resilient; validate is the authoritative diagnostics path.
        }
    }

    private void loadExternalJson(Path folder, Path file, String category) {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (!element.isJsonObject()) {
                return;
            }
            String name = folder.relativize(file).toString().replace('\\', '/');
            if (name.endsWith(".json")) {
                name = name.substring(0, name.length() - ".json".length());
            }
            String id = "tctorpg:" + name;
            JsonObject json = element.getAsJsonObject();
            switch (category) {
                case "jobs" -> jobs.put(id, JobDefinition.fromJson(id, json));
                case "skills" -> skills.put(id, SkillDefinition.fromJson(id, json));
                case "items" -> equipment.put(id, EquipmentDefinition.fromJson(id, json));
                case "quests" -> quests.put(id, QuestDefinition.fromJson(id, json));
                default -> {
                }
            }
        } catch (Exception ignored) {
            // Invalid files are reported by /tctorpg validate.
        }
    }
}

