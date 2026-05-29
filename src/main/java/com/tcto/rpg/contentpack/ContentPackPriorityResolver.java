package com.tcto.rpg.contentpack;

import java.util.Comparator;
import java.util.List;

public class ContentPackPriorityResolver {
    public List<ContentPack> resolve(List<ContentPack> packs) {
        return packs.stream()
            .filter(ContentPack::enabled)
            .sorted(Comparator.comparingInt(ContentPack::priority))
            .toList();
    }
}
