package de.hdskins.labymod.shared.utils;

public enum Side {

    LEFT(-1),
    RIGHT(1);

    private final int modifier;

    Side(int modifier) {
        this.modifier = modifier;
    }

    public int getModifier() {
        return this.modifier;
    }
}
