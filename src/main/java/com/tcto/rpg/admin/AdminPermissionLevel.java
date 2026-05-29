package com.tcto.rpg.admin;

public enum AdminPermissionLevel {
    NONE("tctorpg.operator.none"),
    TESTER("tctorpg.operator.tester"),
    MODERATOR("tctorpg.operator.moderator"),
    BALANCER("tctorpg.operator.balancer"),
    DESIGNER("tctorpg.operator.designer"),
    ADMIN("tctorpg.operator.admin"),
    OWNER("tctorpg.operator.owner");

    private final String permissionId;

    AdminPermissionLevel(String permissionId) {
        this.permissionId = permissionId;
    }

    public String permissionId() {
        return permissionId;
    }

    public boolean includes(AdminPermissionLevel required) {
        return ordinal() >= required.ordinal();
    }
}
