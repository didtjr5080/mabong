package com.tcto.rpg.validation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class DataValidator {
    private final ReferenceValidator references = new ReferenceValidator();

    public ValidationResult validateAll(Path dataRoot, Path contentPacksRoot) {
        ValidationResult result = new ValidationResult();
        validateTree(dataRoot, result);
        validateTree(contentPacksRoot, result);
        references.validate(result);
        return result;
    }

    public ValidationResult validateCategory(Path dataRoot, String category) {
        ValidationResult result = new ValidationResult();
        validateTree(dataRoot.resolve(category), result);
        return result;
    }

    private void validateTree(Path root, ValidationResult result) {
        if (!Files.exists(root)) {
            result.warn(root.toString(), "Path does not exist.");
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(path -> path.toString().endsWith(".json")).forEach(path -> validateFile(root, path, result));
        } catch (IOException ex) {
            result.error(root.toString(), ex.getMessage());
        }
    }

    private void validateFile(Path root, Path path, ValidationResult result) {
        String relative = root.relativize(path).toString().replace('\\', '/');
        result.checkedFile();
        JsonObject json;
        try {
            json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        } catch (Exception ex) {
            result.error(relative, "Invalid JSON: " + ex.getMessage());
            return;
        }
        require(json, "id", relative, result);
        validateNumericRanges(json, relative, result);
        references.accept(relative, json);
    }

    private static void require(JsonObject json, String key, String file, ValidationResult result) {
        if (!json.has(key) || json.get(key).isJsonNull() || json.get(key).getAsString().isBlank()) {
            result.error(file, "Missing required field: " + key);
        }
    }

    private static void validateNumericRanges(JsonObject json, String file, ValidationResult result) {
        Map<String, Double> zeroToOne = new HashMap<>();
        for (String key : json.keySet()) {
            if (key.endsWith("chance") || key.endsWith("rate") || key.endsWith("probability")) {
                try {
                    zeroToOne.put(key, json.get(key).getAsDouble());
                } catch (Exception ignored) {
                }
            }
            if ((key.endsWith("price") || key.endsWith("cost")) && json.get(key).isJsonPrimitive()) {
                try {
                    if (json.get(key).getAsDouble() < 0) {
                        result.error(file, key + " cannot be negative.");
                    }
                } catch (Exception ignored) {
                }
            }
        }
        zeroToOne.forEach((key, value) -> {
            if (value < 0.0 || value > 1.0) {
                result.error(file, key + " must be between 0 and 1.");
            }
        });
    }
}
