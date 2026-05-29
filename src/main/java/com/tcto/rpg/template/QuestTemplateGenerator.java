package com.tcto.rpg.template;

import com.google.gson.JsonObject;

public class QuestTemplateGenerator {
    public JsonObject generate(String id, String type) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", id);
        json.addProperty("type", type == null || type.isBlank() ? "normal" : type);
        json.addProperty("start_npc", "");
        json.addProperty("complete_npc", "");
        json.add("prerequisites", TemplateManager.array());
        json.add("objectives", TemplateManager.array());
        json.add("rewards", TemplateManager.array());
        json.addProperty("repeatable", false);
        return json;
    }
}
