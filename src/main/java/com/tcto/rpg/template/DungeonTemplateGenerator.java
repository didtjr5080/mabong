package com.tcto.rpg.template;

import com.google.gson.JsonObject;

public class DungeonTemplateGenerator {
    public JsonObject generate(String id, String ignored) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", id);
        json.addProperty("recommended_level", 5);
        json.addProperty("entry_cost", 0);
        json.add("required_quests", TemplateManager.array());
        json.add("monsters", TemplateManager.array());
        json.addProperty("boss_id", "");
        json.add("clear_rewards", TemplateManager.array());
        json.addProperty("daily_entry_limit", 0);
        json.addProperty("party_allowed", true);
        return json;
    }
}
