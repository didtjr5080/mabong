package com.tcto.rpg.contentpack;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ContentPackManager {
    private final ContentPackLoader loader = new ContentPackLoader();
    private final ContentPackPriorityResolver resolver = new ContentPackPriorityResolver();

    public List<ContentPack> loadEnabled(Path root) throws IOException {
        return resolver.resolve(loader.load(root));
    }
}
