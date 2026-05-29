package com.tcto.rpg.template;

import com.google.gson.JsonObject;

public class ItemTemplateGenerator {
    public JsonObject generate(String id, String type) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", id);
        json.addProperty("type", type == null || type.isBlank() ? "material" : type);
        json.addProperty("rarity", "common");
        json.addProperty("required_level", 1);
        json.add("required_jobs", TemplateManager.array());
        json.addProperty("buy_price", 0);
        json.addProperty("sell_price", 0);
        json.addProperty("droppable", true);
        json.addProperty("event_only", false);
        return json;
    }
}
