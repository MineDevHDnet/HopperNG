package net.minedevhd.minehopper.model;

public enum FilterMode {
    OFF, MATERIAL, EXACT;
    public static FilterMode parse(String value) {
        if (value == null) return OFF;
        try { return valueOf(value.toUpperCase()); }
        catch (IllegalArgumentException ignored) { return OFF; }
    }
}
