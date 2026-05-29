package com.tcto.rpg.admin;

import java.util.UUID;

public record AdminSession(UUID playerId, String playerName, AdminPermissionLevel level) {
}
