package com.tcto.rpg.client.cache;

public class FloatingDamageEntry {
    private final String text;
    private int ticksLeft;

    public FloatingDamageEntry(String text, int ticksLeft) {
        this.text = text;
        this.ticksLeft = ticksLeft;
    }

    public String text() {
        return text;
    }

    public int ticksLeft() {
        return ticksLeft;
    }

    public void tick() {
        ticksLeft = Math.max(0, ticksLeft - 1);
    }
}

