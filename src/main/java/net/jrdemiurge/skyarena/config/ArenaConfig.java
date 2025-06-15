package net.jrdemiurge.skyarena.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class ArenaConfig {
    public int startingPoints;
    public int pointsIncrease;
    public int mobSpawnRadius;
    public int mobCostRatio;
    public int baseScalingThreshold;
    public double mobStatGrowthCoefficient;
    public double squadSpawnChance;
    public int squadSpawnSize;
    public int spawnDistanceFromPlayer;
    public int battleLossDistance;
    public int mobTeleportDistance;
    public int rewardIncreaseInterval;
    public int maxDifficultyLevel;
    public boolean allowDifficultyReset;
    public boolean allowWaterAndAirSpawn;
    public boolean individualPlayerStats;
    public boolean nightTime;
    public boolean enableRain;
    public boolean enableMobItemDrop;
    public String reward;
    public LinkedHashMap<String, Integer> mobValues;
    public Map<Integer, PresetWave> presetWaves;
    public int mobGriefingProtectionRadius;
}
