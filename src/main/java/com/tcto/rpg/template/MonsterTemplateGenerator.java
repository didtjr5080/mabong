package com.tcto.rpg.template;

import com.google.gson.JsonObject;

public class MonsterTemplateGenerator {
    public JsonObject generate(String id, String regionId) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", id);
        json.addProperty("entity_type", "minecraft:zombie");
        json.addProperty("region_id", regionId == null || regionId.isBlank() ? "beginner_forest" : regionId);
        json.addProperty("level", 1);
        json.addProperty("max_hp", 40);
        json.addProperty("attack", 4);
        json.addProperty("defense", 1);
        json.addProperty("magic_resistance", 0);
        json.addProperty("exp", 8);
        json.add("drop_table", TemplateManager.array());
        return json;
    }
}
