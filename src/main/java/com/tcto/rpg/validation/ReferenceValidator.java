package com.tcto.rpg.validation;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReferenceValidator {
    private final Map<String, Set<String>> idsByFolder = new HashMap<>();

    public void accept(String file, JsonObject json) {
        if (!json.has("id")) {
            return;
        }
        String folder = file.contains("/") ? file.substring(0, file.indexOf('/')) : "";
        idsByFolder.computeIfAbsent(folder, key -> new HashSet<>()).add(json.get("id").getAsString());
    }

    public void validate(ValidationResult result) {
        // Cross-reference validation is intentionally incremental. The first MVP records ID sets
        // so specific validators can enforce references without changing command flow later.
    }
}
