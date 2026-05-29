package com.tcto.rpg.common.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

public final class ModConfigs {
    private ModConfigs() {
    }

    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, ServerCommonConfig.SPEC, "tctorpg/server-common.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientUiConfig.SPEC, "tctorpg/client-ui.toml");
    }
}

