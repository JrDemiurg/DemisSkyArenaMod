package net.jrdemiurge.skyarena.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class FullProtectionZones {

    private static final Map<ResourceKey<Level>, Map<BlockPos, Integer>> FULL_PROTECTION_ZONES = new HashMap<>();

    private FullProtectionZones() {}

    public static void add(Level level, BlockPos altarPos, int radius) {
        FULL_PROTECTION_ZONES
                .computeIfAbsent(level.dimension(), k -> new HashMap<>())
                .put(altarPos, radius);
    }

    public static void remove(Level level, BlockPos altarPos) {
        var perDim = FULL_PROTECTION_ZONES.get(level.dimension());
        if (perDim == null) return;
        perDim.remove(altarPos);
        if (perDim.isEmpty()) {
            FULL_PROTECTION_ZONES.remove(level.dimension());
        }
    }

    public static boolean isInZone(Level level, BlockPos pos) {
        var perDim = FULL_PROTECTION_ZONES.get(level.dimension());
        if (perDim == null || perDim.isEmpty()) return false;
        for (var e : perDim.entrySet()) {
            if (e.getKey().closerThan(pos, e.getValue())) return true;
        }
        return false;
    }
}
