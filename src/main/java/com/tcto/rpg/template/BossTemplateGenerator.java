package com.tcto.rpg.template;

import com.google.gson.JsonObject;

public class BossTemplateGenerator {
    public JsonObject generate(String id, String ignored) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", id);
        json.addProperty("entity_type", "minecraft:zombie");
        json.addProperty("level", 10);
        json.addProperty("max_hp", 500);
        json.addProperty("attack", 18);
        json.addProperty("defense", 8);
        json.addProperty("exp", 200);
        json.addProperty("boss_bar_name", id);
        json.add("patterns", TemplateManager.array("basic_slam"));
        return json;
    }
}
