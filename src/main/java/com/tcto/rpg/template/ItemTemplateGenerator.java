package com.tcto.rpg.template;

import com.google.gson.JsonObject;

public class ItemTemplateGenerator {
    public JsonObject generate(String id, String type) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", id);
        String itemType = type == null || type.isBlank() ? "weapon" : type;
        json.addProperty("type", "equipment");
        json.addProperty("slot", itemType.equals("armor") ? "chestplate" : "weapon");
        json.addProperty("weapon_type", itemType.equals("armor") ? "" : itemType);
        json.addProperty("rarity", "common");
        JsonObject requirements = new JsonObject();
        requirements.addProperty("level", 1);
        requirements.add("jobs", TemplateManager.array());
        requirements.addProperty("tier", 0);
        requirements.add("stats", new JsonObject());
        json.add("requirements", requirements);
        JsonObject stats = new JsonObject();
        stats.addProperty("physical_attack", 4);
        json.add("stats", stats);
        json.add("effects", TemplateManager.array());
        return json;
    }
}
