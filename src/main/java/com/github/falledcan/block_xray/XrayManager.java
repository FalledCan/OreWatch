package com.github.falledcan.block_xray;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 鉱石の位置に「発光する BlockDisplay」を出し、有効にした本人だけに見せる。
 * 発光の輪郭は壁越しにも見えるので、地中の鉱石の位置がわかる。
 */
public class XrayManager {

    /** 異常終了時などに残ったエンティティを掃除するための目印 */
    static final String ENTITY_TAG = "block_xray";

    // 本物のブロックとちらつかないよう、ほんの少しだけ大きくする
    private static final Transformation TRANSFORMATION = new Transformation(
            new Vector3f(-0.0025f, -0.0025f, -0.0025f), new AxisAngle4f(),
            new Vector3f(1.005f, 1.005f, 1.005f), new AxisAngle4f());

    private final Block_Xray plugin;
    private final Map<UUID, Session> sessions = new HashMap<>();
    private BukkitTask task;

    private int radius;
    private int maxDisplays;

    XrayManager(Block_Xray plugin) {
        this.plugin = plugin;
    }

    private static class Session {
        final Set<OreType> ores = EnumSet.noneOf(OreType.class);
        final Map<Location, BlockDisplay> displays = new HashMap<>();
    }

    void start() {
        radius = Math.max(1, plugin.getConfig().getInt("radius", 16));
        maxDisplays = Math.max(1, plugin.getConfig().getInt("max-displays", 1500));
        long interval = Math.max(1, plugin.getConfig().getLong("update-interval", 20));

        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 1L, interval);
    }

    void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (Session session : sessions.values()) {
            clear(session);
        }
        sessions.clear();
    }

    /** 前回サーバーが落ちた時などに残ってしまった表示を消す */
    void removeLeftovers() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(BlockDisplay.class)) {
                if (entity.getScoreboardTags().contains(ENTITY_TAG)) {
                    entity.remove();
                }
            }
        }
    }

    boolean isEnabled(Player player, OreType type) {
        Session session = sessions.get(player.getUniqueId());
        return session != null && session.ores.contains(type);
    }

    /** @return 切り替え後に有効なら true */
    boolean toggle(Player player, OreType type) {
        Session session = sessions.computeIfAbsent(player.getUniqueId(), k -> new Session());
        boolean enabled = !session.ores.remove(type);
        if (enabled) {
            session.ores.add(type);
        }
        update(player, session);
        return enabled;
    }

    void enableAll(Player player) {
        Session session = sessions.computeIfAbsent(player.getUniqueId(), k -> new Session());
        session.ores.addAll(EnumSet.allOf(OreType.class));
        update(player, session);
    }

    void disable(Player player) {
        Session session = sessions.remove(player.getUniqueId());
        if (session != null) {
            clear(session);
        }
    }

    /** ブロックが壊されたら、その場所の表示をすぐに消す */
    void onBlockRemoved(Block block) {
        Location key = block.getLocation();
        for (Session session : sessions.values()) {
            BlockDisplay display = session.displays.remove(key);
            if (display != null) {
                display.remove();
            }
        }
    }

    private void updateAll() {
        Iterator<Map.Entry<UUID, Session>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Session> entry = it.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                clear(entry.getValue());
                it.remove();
                continue;
            }
            update(player, entry.getValue());
        }
    }

    private void update(Player player, Session session) {
        if (session.ores.isEmpty()) {
            clear(session);
            return;
        }

        Location center = player.getLocation();
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int minY = Math.max(world.getMinHeight(), cy - radius);
        int maxY = Math.min(world.getMaxHeight() - 1, cy + radius);
        int radiusSq = radius * radius;

        Set<Location> found = new HashSet<>();

        scan:
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                int dxz = (x - cx) * (x - cx) + (z - cz) * (z - cz);
                if (dxz > radiusSq || !world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    if (dxz + (y - cy) * (y - cy) > radiusSq) {
                        continue;
                    }
                    Block block = world.getBlockAt(x, y, z);
                    OreType type = OreType.of(block.getType());
                    if (type == null || !session.ores.contains(type)) {
                        continue;
                    }

                    Location key = block.getLocation();
                    found.add(key);
                    BlockDisplay display = session.displays.get(key);
                    if (display == null || !display.isValid()) {
                        session.displays.put(key, spawn(player, block, type));
                    }
                    if (found.size() >= maxDisplays) {
                        break scan;
                    }
                }
            }
        }

        // 掘られた・範囲外に出た・無効にした鉱石の表示を消す
        Iterator<Map.Entry<Location, BlockDisplay>> it = session.displays.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Location, BlockDisplay> entry = it.next();
            if (!found.contains(entry.getKey())) {
                entry.getValue().remove();
                it.remove();
            }
        }
    }

    private BlockDisplay spawn(Player viewer, Block block, OreType type) {
        BlockDisplay display = block.getWorld().spawn(block.getLocation(), BlockDisplay.class);
        // 同じ tick 内で非表示にするので、他のプレイヤーには一度も送られない
        display.setVisibleByDefault(false);
        display.setPersistent(false);
        display.addScoreboardTag(ENTITY_TAG);
        display.setBlock(block.getBlockData());
        display.setTransformation(TRANSFORMATION);
        display.setBrightness(new Display.Brightness(15, 15));
        display.setGlowing(true);
        display.setGlowColorOverride(type.glowColor);
        viewer.showEntity(plugin, display);
        return display;
    }

    private void clear(Session session) {
        for (BlockDisplay display : session.displays.values()) {
            display.remove();
        }
        session.displays.clear();
    }
}
