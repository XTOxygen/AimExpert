package com.endwithme.aimexpert.crosshair;

import java.util.EnumMap;
import java.util.Map;

public final class DefaultPatterns
{
    private static final Map<CrosshairType, CrosshairPattern> PATTERNS = new EnumMap<CrosshairType, CrosshairPattern>(CrosshairType.class);

    private DefaultPatterns()
    {
    }

    public static CrosshairPattern get(CrosshairType type)
    {
        CrosshairPattern pattern = PATTERNS.get(type);
        return pattern == null ? PATTERNS.get(CrosshairType.NORMAL) : pattern;
    }

    private static void put(CrosshairType type, String... rows)
    {
        PATTERNS.put(type, new CrosshairPattern(rows));
    }

    static
    {
        put(CrosshairType.NORMAL,
                "...#...",
                "...#...",
                "...#...",
                "###.###",
                "...#...",
                "...#...",
                "...#...");

        put(CrosshairType.FRIENDLY,
                "..#.#..",
                ".#...#.",
                "#.....#",
                ".......",
                "#.....#",
                ".#...#.",
                "..#.#..");

        put(CrosshairType.HOSTILE,
                "#.....#",
                "##...##",
                ".##.##.",
                "...#...",
                ".##.##.",
                "##...##",
                "#.....#");

        put(CrosshairType.FOV,
                "##...##",
                "#.....#",
                "#.....#",
                "...#...",
                "#.....#",
                "#.....#",
                "##...##");

        put(CrosshairType.ATTACK,
                "...#...",
                ".#.#.#.",
                "..#.#..",
                "#.....#",
                "..#.#..",
                ".#.#.#.",
                "...#...");
    }
}
