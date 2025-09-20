package net.jrdemiurge.skyarena.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class BossBarHideZones {

    private static final Map<ResourceKey<Level>, Map<BlockPos, Integer>> BOSS_BAR_HIDE_ZONES = new HashMap<>();

    private BossBarHideZones() {}

    public static void add(Level level, BlockPos altarPos, int radius) {
        BOSS_BAR_HIDE_ZONES
                .computeIfAbsent(level.dimension(), k -> new HashMap<>())
                .put(altarPos, radius);
    }

    public static void remove(Level level, BlockPos altarPos) {
        var perDim = BOSS_BAR_HIDE_ZONES.get(level.dimension());
        if (perDim == null) return;
        perDim.remove(altarPos);
        if (perDim.isEmpty()) {
            BOSS_BAR_HIDE_ZONES.remove(level.dimension());
        }
    }

    public static boolean isInZone(Level level, BlockPos pos) {
        var perDim = BOSS_BAR_HIDE_ZONES.get(level.dimension());
        if (perDim == null || perDim.isEmpty()) return false;
        for (var e : perDim.entrySet()) {
            if (e.getKey().closerThan(pos, e.getValue())) return true;
        }
        return false;
    }
}
