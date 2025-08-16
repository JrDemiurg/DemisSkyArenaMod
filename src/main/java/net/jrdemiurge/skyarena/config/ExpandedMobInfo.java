package net.jrdemiurge.skyarena.config;

public class ExpandedMobInfo {
    public String mobId;
    public int cost;
    public double squadSpawnChance;
    public int squadSpawnSize;
    public double statMultiplierCoefficient;
    public double mobSpawnChance;

    public ExpandedMobInfo(String mobId, int cost, double squadSpawnChance, int squadSpawnSize,
                           double statMultiplierCoefficient, double mobSpawnChance) {
        this.mobId = mobId;
        this.cost = cost;
        this.squadSpawnChance = squadSpawnChance;
        this.squadSpawnSize = squadSpawnSize;
        this.statMultiplierCoefficient = statMultiplierCoefficient;
        this.mobSpawnChance = mobSpawnChance;
    }
}
