package net.jrdemiurge.skyarena.config;

import java.util.LinkedHashMap;
import java.util.List;

public class ArenaConfig {
    public int mobSpawnRadius;
    public int spawnDistanceFromPlayer;
    public int battleLossDistance;
    public int mobTeleportDistance;
    public int mobGriefingProtectionRadius;
    public int fullProtectionRadius;
    public int bossBarHideRadius;
    public int mobCostRatio;

    public boolean allowDifficultyReset;
    public boolean allowWaterAndAirSpawn;
    public boolean individualPlayerStats;
    public boolean setNight;
    public boolean setRain;
    public boolean disableMobItemDrop;

    public int startingPoints;
    public double startingStatMultiplier;

    public boolean resetDifficultyOnDefeat;
    public boolean autoWaveRun;

    public double pointsCoefficientPer1000BlocksFromWorldCenter;
    public double statMultiplierCoefficientPer1000BlocksFromWorldCenter;
    public double lootTableCountCoefficientPer1000BlocksFromWorldCenter;

    public List<DifficultyLevelRange> difficultyLevelRanges;
    public LinkedHashMap<String, MobGroup> mobGroups;

    public LinkedHashMap<Integer, PresetWave> presetWaves;
}
