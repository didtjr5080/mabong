package com.tcto.rpg.common.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.HashMap;
import java.util.Map;

public class RpgDataManager extends SimpleJsonResourceReloadListener {
    public static final RpgDataManager INSTANCE = new RpgDataManager();

    private final Map<String, JobDefinition> jobs = new HashMap<>();
    private final Map<String, SkillDefinition> skills = new HashMap<>();
    private final Map<String, EquipmentDefinition> equipment = new HashMap<>();
    private final Map<String, MonsterDefinition> monsters = new HashMap<>();
    private final Map<String, BossDefinition> bosses = new HashMap<>();
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
            } else if (path.equals("exp_table")) {
                expTable.loadFromJson(json);
            }
        }
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

    public EquipmentDefinition getEquipment(String id) {
        return equipment.get(id);
    }

    public MonsterDefinition getMonster(String id) {
        return monsters.get(id);
    }

    public BossDefinition getBoss(String id) {
        return bosses.get(id);
    }

    public ExpTable getExpTable() {
        return expTable;
    }
}

