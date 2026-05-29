package com.tcto.rpg.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class JsonUtil {
    private JsonUtil() {
    }

    public static String getString(JsonObject json, String key, String fallback) {
        JsonElement element = json.get(key);
        return element != null ? element.getAsString() : fallback;
    }

    public static int getInt(JsonObject json, String key, int fallback) {
        JsonElement element = json.get(key);
        return element != null ? element.getAsInt() : fallback;
    }

    public static double getDouble(JsonObject json, String key, double fallback) {
        JsonElement element = json.get(key);
        return element != null ? element.getAsDouble() : fallback;
    }
}

