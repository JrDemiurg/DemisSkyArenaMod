package net.jrdemiurge.skyarena.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkyArenaConfig {
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("skyarena.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static ModConfig configData;

    public static final ArenaConfig DEFAULT_ARENA = createDefaultArena();
    public static final List<String> DEFAULT_KEY = createDefaultKey();
    public static final TrophyConfig DEFAULT_TROPHY = createDefaultTrophy();


    public static void loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            loadFromFile();
        } else {
            copyDefaultConfig();
        }

        if (SkyArenaConfig.configData == null) {
            Logger LOGGER = LogUtils.getLogger();
            LOGGER.error("SkyArenaConfig.configData is null! Config not loaded properly.");

            configData = new ModConfig();
            configData.arenas = Map.of("default", DEFAULT_ARENA);
            configData.keys = Map.of("default", DEFAULT_KEY);
            configData.trophies = Map.of("default", DEFAULT_TROPHY);
        }
    }

    private static void loadFromFile() {
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            configData = GSON.fromJson(reader, ModConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyDefaultConfig() {
        try (InputStream in = SkyArenaConfig.class.getResourceAsStream("/assets/skyarena/config/skyarena.json")) {
            if (in == null) {
                System.err.println("Не найден конфиг в ресурсах мода!");
                return;
            }

            Files.copy(in, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            loadFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArenaConfig createDefaultArena() {
        ArenaConfig defaultArena = new ArenaConfig();
        defaultArena.startingPoints = 500;
        defaultArena.pointsIncrease = 100;
        defaultArena.mobSpawnRadius = 36;
        defaultArena.mobCostRatio = 20;
        defaultArena.baseScalingThreshold = 120;
        defaultArena.mobStatGrowthCoefficient = 1.0;
        defaultArena.squadSpawnChance = 0.35;
        defaultArena.squadSpawnSize = 3;
        defaultArena.spawnDistanceFromPlayer = 10;
        defaultArena.battleLossDistance = 60;
        defaultArena.mobTeleportDistance = 50;
        defaultArena.rewardIncreaseInterval = 10;
        defaultArena.maxDifficultyLevel = 0;
        defaultArena.allowDifficultyReset = true;
        defaultArena.allowWaterAndAirSpawn = false;
        defaultArena.individualPlayerStats = false;
        defaultArena.setNight = true;
        defaultArena.setRain = false;
        defaultArena.disableMobItemDrop = true;
        defaultArena.reward = "skyarena:battle_rewards/crimson_key";
        defaultArena.mobValues = new LinkedHashMap<>();

        defaultArena.mobValues.put("minecraft:zombie", 20);
        defaultArena.mobValues.put("minecraft:skeleton", 30);
        defaultArena.mobValues.put("minecraft:blaze", 40);
        defaultArena.mobValues.put("minecraft:evoker", 80);
        defaultArena.mobValues.put("minecraft:husk", 25);
        defaultArena.mobValues.put("minecraft:illusioner", 45);
        defaultArena.mobValues.put("minecraft:slime", 20);
        defaultArena.mobValues.put("minecraft:magma_cube", 25);
        defaultArena.mobValues.put("minecraft:zombified_piglin", 35);
        defaultArena.mobValues.put("minecraft:pillager", 35);
        defaultArena.mobValues.put("minecraft:ravager", 150);
        defaultArena.mobValues.put("minecraft:stray", 35);
        defaultArena.mobValues.put("minecraft:vindicator", 50);
        defaultArena.mobValues.put("minecraft:warden", 2000);
        defaultArena.mobValues.put("minecraft:zoglin", 55);
        defaultArena.mobValues.put("minecraft:witch", 30);
        defaultArena.mobValues.put("minecraft:wither_skeleton", 45);

        defaultArena.mobValues.put("born_in_chaos_v1:barrel_zombie", 35);
        defaultArena.mobValues.put("born_in_chaos_v1:bonescaller", 40);
        defaultArena.mobValues.put("born_in_chaos_v1:dark_vortex", 55);
        defaultArena.mobValues.put("born_in_chaos_v1:decaying_zombie", 25);
        defaultArena.mobValues.put("born_in_chaos_v1:dire_hound_leader", 200);
        defaultArena.mobValues.put("born_in_chaos_v1:door_knight", 40);
        defaultArena.mobValues.put("born_in_chaos_v1:dread_hound", 25);
        defaultArena.mobValues.put("born_in_chaos_v1:fallen_chaos_knight", 80);
        defaultArena.mobValues.put("born_in_chaos_v1:felsteed", 80);
        defaultArena.mobValues.put("born_in_chaos_v1:lifestealer_true_form", 220);
        defaultArena.mobValues.put("born_in_chaos_v1:mother_spider", 170);
        defaultArena.mobValues.put("born_in_chaos_v1:mrs_pumpkin", 30);
        defaultArena.mobValues.put("born_in_chaos_v1:nightmare_stalker", 120);
        defaultArena.mobValues.put("born_in_chaos_v1:seared_spirit", 65);
        defaultArena.mobValues.put("born_in_chaos_v1:siamese_skeletons", 25);
        defaultArena.mobValues.put("born_in_chaos_v1:skeleton_thrasher", 90);
        defaultArena.mobValues.put("born_in_chaos_v1:spirit_guide", 45);
        defaultArena.mobValues.put("born_in_chaos_v1:spiritof_chaos", 35);
        defaultArena.mobValues.put("born_in_chaos_v1:supreme_bonescaller", 130);
        defaultArena.mobValues.put("born_in_chaos_v1:swarmer", 60);
        defaultArena.mobValues.put("born_in_chaos_v1:zombie_bruiser", 80);
        defaultArena.mobValues.put("born_in_chaos_v1:zombie_clown", 55);
        defaultArena.mobValues.put("born_in_chaos_v1:zombie_fisherman", 25);
        defaultArena.mobValues.put("born_in_chaos_v1:zombie_lumberjack", 35);

        defaultArena.mobValues.put("iceandfire:cyclops", 250);
        defaultArena.mobValues.put("iceandfire:stymphalian_bird", 40);
        defaultArena.mobValues.put("iceandfire:troll", 80);

        defaultArena.mobValues.put("mutantmonsters:mutant_zombie", 200);
        defaultArena.mobValues.put("mutantmonsters:mutant_skeleton", 220);

        defaultArena.mobValues.put("alexsmobs:mimicube", 40);
        defaultArena.mobValues.put("alexsmobs:murmur", 40);
        defaultArena.mobValues.put("alexsmobs:warped_mosco", 170);
        defaultArena.mobValues.put("alexsmobs:guster", 25);
        defaultArena.mobValues.put("alexsmobs:rocky_roller", 25);

        defaultArena.mobValues.put("realmrpg_wyrms:ender_wyrm", 55);
        defaultArena.mobValues.put("realmrpg_wyrms:red_wyrm", 45);

        defaultArena.mobValues.put("cataclysm:amethyst_crab", 250);
        defaultArena.mobValues.put("cataclysm:aptrgangr", 250);
        defaultArena.mobValues.put("cataclysm:coral_golem", 160);
        defaultArena.mobValues.put("cataclysm:coralssus", 180);
        defaultArena.mobValues.put("cataclysm:draugr", 30);
        defaultArena.mobValues.put("cataclysm:elite_draugr", 40);
        defaultArena.mobValues.put("cataclysm:ender_golem", 250);
        defaultArena.mobValues.put("cataclysm:royal_draugr", 40);
        defaultArena.mobValues.put("cataclysm:the_prowler", 250);
        defaultArena.mobValues.put("cataclysm:wadjet", 250);

        defaultArena.mobValues.put("call_of_yucutan:undead_warrior", 25);

        defaultArena.mobValues.put("netherexp:ecto_slab", 25);
        defaultArena.mobValues.put("netherexp:banshee", 30);
        defaultArena.mobValues.put("netherexp:vessel", 30);

        defaultArena.mobValues.put("savage_and_ravage:trickster", 40);
        defaultArena.mobValues.put("savage_and_ravage:skeleton_villager", 30);
        defaultArena.mobValues.put("savage_and_ravage:iceologer", 50);
        defaultArena.mobValues.put("savage_and_ravage:griefer", 50);
        defaultArena.mobValues.put("savage_and_ravage:executioner", 60);

        defaultArena.mobValues.put("illagerinvasion:provoker", 35);
        defaultArena.mobValues.put("illagerinvasion:basher", 50);
        defaultArena.mobValues.put("illagerinvasion:inquisitor", 130);
        defaultArena.mobValues.put("illagerinvasion:alchemist", 40);
        defaultArena.mobValues.put("illagerinvasion:necromancer", 80);

        defaultArena.mobValues.put("mowziesmobs:naga", 60);
        defaultArena.mobValues.put("mowziesmobs:bluff", 85);

        defaultArena.mobValues.put("irons_spellbooks:citadel_keeper", 130);
        defaultArena.mobValues.put("irons_spellbooks:archevoker", 130);
        defaultArena.mobValues.put("irons_spellbooks:necromancer", 80);
        defaultArena.mobValues.put("irons_spellbooks:cryomancer", 130);
        defaultArena.mobValues.put("irons_spellbooks:pyromancer", 130);
        defaultArena.mobValues.put("irons_spellbooks:apothecarist", 130);

        defaultArena.mobValues.put("aquamirae:tortured_soul", 40);

        defaultArena.mobValues.put("quark:forgotten", 90);

        defaultArena.mobValues.put("iter_rpg:earth_elemental", 55);
        defaultArena.mobValues.put("iter_rpg:water_elemental", 45);
        defaultArena.mobValues.put("iter_rpg:air_elemental", 55);
        defaultArena.mobValues.put("iter_rpg:fire_elemental", 55);
        defaultArena.mobValues.put("iter_rpg:void_elemental", 65);

        defaultArena.mobValues.put("alexscaves:caniac", 40);
        defaultArena.mobValues.put("alexscaves:licowitch", 50);
        defaultArena.mobValues.put("alexscaves:deep_one_knight", 85);
        defaultArena.mobValues.put("alexscaves:deep_one_mage", 130);
        defaultArena.mobValues.put("alexscaves:brainiac", 100);

        defaultArena.mobValues.put("galosphere:berserker", 210);

        defaultArena.mobValues.put("caverns_and_chasms:mime", 100);

        defaultArena.mobValues.put("eeeabsmobs:corpse_warlock", 250);
        defaultArena.mobValues.put("eeeabsmobs:guling_sentinel_heavy", 250);
        defaultArena.mobValues.put("eeeabsmobs:immortal_executioner", 160);

        return defaultArena;
    }

    private static List<String> createDefaultKey() {
        List<String> defaultLootTables = List.of(
                "minecraft:chests/abandoned_mineshaft",
                "minecraft:chests/ancient_city",
                "minecraft:chests/ancient_city_ice_box",
                "minecraft:chests/bastion_bridge",
                "minecraft:chests/bastion_hoglin_stable",
                "minecraft:chests/bastion_other",
                "minecraft:chests/bastion_treasure",
                "minecraft:chests/buried_treasure",
                "minecraft:chests/desert_pyramid",
                "minecraft:chests/end_city_treasure",
                "minecraft:chests/igloo_chest",
                "minecraft:chests/jungle_temple",
                "minecraft:chests/nether_bridge",
                "minecraft:chests/pillager_outpost",
                "minecraft:chests/ruined_portal",
                "minecraft:chests/shipwreck_map",
                "minecraft:chests/shipwreck_supply",
                "minecraft:chests/shipwreck_treasure",
                "minecraft:chests/simple_dungeon",
                "minecraft:chests/stronghold_library",
                "minecraft:chests/underwater_ruin_big",
                "minecraft:chests/underwater_ruin_small",
                "minecraft:chests/village/village_temple",
                "minecraft:chests/woodland_mansion",
                "minecraft:chests/chest_level_2",
                "minecraft:chests/chest_level_3",
                "minecraft:chests/firewell_d",
                "minecraft:chests/shater",
                "iceandfire:chest/fire_dragon_female_cave",
                "iceandfire:chest/fire_dragon_male_cave",
                "iceandfire:chest/ice_dragon_female_cave",
                "iceandfire:chest/ice_dragon_male_cave",
                "iceandfire:chest/lightning_dragon_female_cave",
                "iceandfire:chest/lightning_dragon_male_cave",
                "iceandfire:chest/mausoleum_chest"
        );
        return defaultLootTables;
    }

    private static TrophyConfig createDefaultTrophy() {
        TrophyConfig defaultTrophy = new TrophyConfig();
        defaultTrophy.cooldown = 0;

        defaultTrophy.effects = new HashMap<>();

        TrophyConfig.EffectConfig hasteEffect = new TrophyConfig.EffectConfig();
        hasteEffect.duration = 1800;
        hasteEffect.amplifier = 0;

        defaultTrophy.effects.put("minecraft:haste", hasteEffect);

        return defaultTrophy;
    }
}
