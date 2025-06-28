package net.jrdemiurge.skyarena.config;

public class ExpandedMobInfo {
    public String mobId;
    public int cost;
    public double squadSpawnChance;
    public int squadSpawnSize;
    public double additionalStatMultiplier;
    public double mobSpawnChance;

    public ExpandedMobInfo(String mobId, int cost, double squadSpawnChance, int squadSpawnSize,
                           double additionalStatMultiplier, double mobSpawnChance) {
        this.mobId = mobId;
        this.cost = cost;
        this.squadSpawnChance = squadSpawnChance;
        this.squadSpawnSize = squadSpawnSize;
        this.additionalStatMultiplier = additionalStatMultiplier;
        this.mobSpawnChance = mobSpawnChance;
    }
}
