package com.tcto.rpg.template;

import com.google.gson.JsonObject;

public class JobTemplateGenerator {
    public JsonObject generate(String id, String ignored) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", id);
        json.addProperty("description", "");
        json.addProperty("tier", 0);
        json.add("parent_jobs", TemplateManager.array());
        json.add("next_jobs", TemplateManager.array());
        json.add("skills", TemplateManager.array());
        json.add("usable_weapons", TemplateManager.array());
        return json;
    }
}
