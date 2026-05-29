package com.tcto.rpg.contentpack;

import java.nio.file.Path;
import java.util.List;

public record ContentPack(String id, String name, String version, boolean enabled, int priority, List<String> authors, String description, Path root) {
}
