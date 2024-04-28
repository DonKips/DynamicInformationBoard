package me.looks.dynamicinformationboard.utils;

import org.bukkit.Bukkit;

public enum ServerVersion {

    v1_13_R1(13),
    v1_13_R2(13),
    v1_14_R1(14),
    v1_15_R1(15),

    v1_16_R1(16),
    v1_16_R2(16),
    v1_16_R3(16),
    v1_17_R1(17),
    v1_18_R1(18),
    v1_18_R2(18),
    v1_19_R1(19),
    v1_19_R2(19),
    v1_19_R3(19),
    v1_20_R1(20),
    v1_20_R2(20),
    v1_20_R3(20);

    public static final ServerVersion CURRENT;

    static {
        CURRENT = getCurrentVersion(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);
    }

    private static ServerVersion getCurrentVersion(String version) {
        if (version == null) {
            return null;
        }

        for (ServerVersion value : ServerVersion.values()) {
            if (value.name().equalsIgnoreCase(version)) {
                return value;
            }
        }
        return null;
    }

    public static boolean is(int minor) {
        return CURRENT.getMinor() == minor;
    }

    public static boolean is(ServerVersion version) {
        return CURRENT == version;
    }

    public static boolean after(int minor) {
        return CURRENT.getMinor() > minor;
    }

    public static boolean after(ServerVersion version) {
        return CURRENT.ordinal() > version.ordinal();
    }

    public static boolean afterOrEqual(int minor) {
        return CURRENT.getMinor() >= minor;
    }

    public static boolean afterOrEqual(ServerVersion version) {
        return CURRENT.ordinal() >= version.ordinal();
    }

    public static boolean before(int minor) {
        return CURRENT.getMinor() < minor;
    }

    public static boolean before(ServerVersion version) {
        return CURRENT.ordinal() < version.ordinal();
    }

    public static boolean beforeOrEqual(int minor) {
        return CURRENT.getMinor() <= minor;
    }

    public static boolean beforeOrEqual(ServerVersion version) {
        return CURRENT.ordinal() <= version.ordinal();
    }

    public static boolean supportsHex() {
        return afterOrEqual(16);
    }

    public static String getCurrent() {
        if (CURRENT == null) return "";
        return CURRENT.name();
    }

    private final int minor;

    ServerVersion(int minor) {
        this.minor = minor;
    }

    public int getMinor() {
        return minor;
    }
}
