package com.tcto.rpg.template;

import com.google.gson.JsonObject;

public class EventTemplateGenerator {
    public JsonObject generate(String id, String type) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", id);
        json.addProperty("enabled", true);
        json.addProperty("type", type == null || type.isBlank() ? "scheduled_modifier" : type);
        JsonObject conditions = new JsonObject();
        conditions.add("regions", TemplateManager.array());
        conditions.add("jobs", TemplateManager.array());
        json.add("conditions", conditions);
        JsonObject modifiers = new JsonObject();
        modifiers.addProperty("exp_multiplier", 1.0);
        modifiers.addProperty("drop_rate_multiplier", 1.0);
        json.add("modifiers", modifiers);
        json.add("rewards", TemplateManager.array());
        return json;
    }
}
