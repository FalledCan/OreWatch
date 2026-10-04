package com.github.falledcan.block_xray;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public enum OreType {

    COAL("coal", "c", ChatColor.DARK_GRAY, Color.fromRGB(0x3A3A3A),
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE),
    COPPER("copper", "cu", ChatColor.GOLD, Color.fromRGB(0xE77C56),
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE),
    IRON("iron", "i", ChatColor.GRAY, Color.fromRGB(0xD8AF93),
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE),
    GOLD("gold", "g", ChatColor.YELLOW, Color.YELLOW,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE),
    REDSTONE("redstone", "r", ChatColor.RED, Color.RED,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE),
    LAPIS("lapis", "l", ChatColor.BLUE, Color.fromRGB(0x1F4FD8),
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE),
    DIAMOND("diamond", "d", ChatColor.AQUA, Color.AQUA,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE),
    EMERALD("emerald", "e", ChatColor.GREEN, Color.LIME,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE),
    QUARTZ("quartz", "q", ChatColor.WHITE, Color.WHITE,
            Material.NETHER_QUARTZ_ORE),
    DEBRIS("debris", "n", ChatColor.DARK_PURPLE, Color.FUCHSIA,
            Material.ANCIENT_DEBRIS);

    private static final Map<Material, OreType> BY_MATERIAL = new EnumMap<>(Material.class);

    static {
        for (OreType type : values()) {
            for (Material material : type.materials) {
                BY_MATERIAL.put(material, type);
            }
        }
    }

    final String id;
    final String alias;
    final ChatColor chatColor;
    final Color glowColor;
    private final Material[] materials;

    OreType(String id, String alias, ChatColor chatColor, Color glowColor, Material... materials) {
        this.id = id;
        this.alias = alias;
        this.chatColor = chatColor;
        this.glowColor = glowColor;
        this.materials = materials;
    }

    static OreType of(Material material) {
        return BY_MATERIAL.get(material);
    }

    /** "diamond" のような名前と、旧コマンドの "d" のような短縮形の両方を受け付ける */
    static OreType fromName(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        for (OreType type : values()) {
            if (type.id.equals(n) || type.alias.equals(n)) {
                return type;
            }
        }
        return null;
    }
}
