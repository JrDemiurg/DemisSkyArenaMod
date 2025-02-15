package net.jrdemiurge.skyarena;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = SkyArena.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final List<List<Object>> defaultMobValues = new ArrayList<>();
    private static final List<String> defaultLootTables = new ArrayList<>();

    static {
        // мобы
        Map<String, Integer> mobMap = new HashMap<>();

        mobMap.put("minecraft:zombie", 20);
        mobMap.put("minecraft:skeleton", 30);
        mobMap.put("minecraft:blaze", 40);
        mobMap.put("minecraft:evoker", 80);
        mobMap.put("minecraft:husk", 25);
        mobMap.put("minecraft:illusioner", 45);
        mobMap.put("minecraft:slime", 20);
        mobMap.put("minecraft:magma_cube", 25);
        mobMap.put("minecraft:zombified_piglin", 35);
        mobMap.put("minecraft:pillager", 35);
        mobMap.put("minecraft:ravager", 150);
        mobMap.put("minecraft:stray", 35);
        mobMap.put("minecraft:vindicator", 50);
        mobMap.put("minecraft:warden", 2000);
        mobMap.put("minecraft:zoglin", 55);
        mobMap.put("minecraft:witch", 30);
        mobMap.put("minecraft:wither_skeleton", 45);

        if (ModList.get().isLoaded("born_in_chaos_v1")) {
            mobMap.put("born_in_chaos_v1:barrel_zombie", 35);
            mobMap.put("born_in_chaos_v1:bonescaller", 40);
            mobMap.put("born_in_chaos_v1:dark_vortex", 55);
            mobMap.put("born_in_chaos_v1:decaying_zombie", 25);
            mobMap.put("born_in_chaos_v1:dire_hound_leader", 200);
            mobMap.put("born_in_chaos_v1:door_knight", 40);
            mobMap.put("born_in_chaos_v1:dread_hound", 25);
            mobMap.put("born_in_chaos_v1:fallen_chaos_knight", 80);
            mobMap.put("born_in_chaos_v1:felsteed", 80);
            mobMap.put("born_in_chaos_v1:lifestealer_true_form", 220);
            mobMap.put("born_in_chaos_v1:mother_spider", 170);
            mobMap.put("born_in_chaos_v1:mrs_pumpkin", 30);
            mobMap.put("born_in_chaos_v1:nightmare_stalker", 120);
            mobMap.put("born_in_chaos_v1:seared_spirit", 65);
            mobMap.put("born_in_chaos_v1:siamese_skeletons", 25);
            mobMap.put("born_in_chaos_v1:skeleton_thrasher", 90);
            mobMap.put("born_in_chaos_v1:spirit_guide", 45);
            mobMap.put("born_in_chaos_v1:spiritof_chaos", 35);
            mobMap.put("born_in_chaos_v1:supreme_bonescaller", 130);
            mobMap.put("born_in_chaos_v1:swarmer", 60);
            mobMap.put("born_in_chaos_v1:zombie_bruiser", 80);
            mobMap.put("born_in_chaos_v1:zombie_clown", 55);
            mobMap.put("born_in_chaos_v1:zombie_fisherman", 25);
            mobMap.put("born_in_chaos_v1:zombie_lumberjack", 35);
        }

        if (ModList.get().isLoaded("iceandfire")) {
            mobMap.put("iceandfire:cyclops", 250);
            mobMap.put("iceandfire:stymphalian_bird", 40);
            mobMap.put("iceandfire:troll", 80);
        }

        if (ModList.get().isLoaded("mutantmonsters")) {
            mobMap.put("mutantmonsters:mutant_zombie", 200);
            mobMap.put("mutantmonsters:mutant_skeleton", 220);
        }
        // 1.0.3
        if (ModList.get().isLoaded("alexsmobs")) {
            mobMap.put("alexsmobs:mimicube", 40);
            mobMap.put("alexsmobs:murmur", 40);
            mobMap.put("alexsmobs:warped_mosco", 170);
            mobMap.put("alexsmobs:guster", 25);
            mobMap.put("alexsmobs:rocky_roller", 25);
        }
        // 1.0.4
        if (ModList.get().isLoaded("realmrpg_demons")) {
            mobMap.put("realmrpg_demons:ancient_demon_lord", 300);
            mobMap.put("realmrpg_demons:demon", 60);
            mobMap.put("realmrpg_demons:demon_lord", 170);
            mobMap.put("realmrpg_demons:imp", 30);
            mobMap.put("realmrpg_demons:imp_guard", 50);
        }

        if (ModList.get().isLoaded("realmrpg_wyrms")) {
            mobMap.put("realmrpg_wyrms:ender_wyrm", 55);
            mobMap.put("realmrpg_wyrms:red_wyrm", 45);
        }
        // 1.0.5
        if (ModList.get().isLoaded("cataclysm")) {
            mobMap.put("cataclysm:amethyst_crab", 250);
            mobMap.put("cataclysm:aptrgangr", 250);
            mobMap.put("cataclysm:coral_golem", 160);
            mobMap.put("cataclysm:coralssus", 180);
            mobMap.put("cataclysm:draugr", 30);
            mobMap.put("cataclysm:elite_draugr", 40);
            mobMap.put("cataclysm:ender_golem", 250);
            mobMap.put("cataclysm:royal_draugr", 40);
            mobMap.put("cataclysm:the_prowler", 250);
            mobMap.put("cataclysm:wadjet", 250);
        }
        // 1.0.6
        if (ModList.get().isLoaded("call_of_yucutan")) {
            mobMap.put("call_of_yucutan:undead_warrior", 25);
        }

        if (ModList.get().isLoaded("netherexp")) {
            mobMap.put("netherexp:ecto_slab", 25);
            mobMap.put("netherexp:banshee", 30);
            mobMap.put("netherexp:vessel", 30);
        }

        if (ModList.get().isLoaded("savage_and_ravage")) {
            mobMap.put("savage_and_ravage:trickster", 40);
            mobMap.put("savage_and_ravage:skeleton_villager", 30);
            mobMap.put("savage_and_ravage:iceologer", 50);
            mobMap.put("savage_and_ravage:griefer", 50);
            mobMap.put("savage_and_ravage:executioner", 60);
        }

        if (ModList.get().isLoaded("illagerinvasion")) {
            mobMap.put("illagerinvasion:provoker", 35);
            mobMap.put("illagerinvasion:basher", 50);
            mobMap.put("illagerinvasion:inquisitor", 130);
            mobMap.put("illagerinvasion:alchemist", 40);
            mobMap.put("illagerinvasion:necromancer", 80);
        }

        if (ModList.get().isLoaded("mowziesmobs")) {
            mobMap.put("mowziesmobs:naga", 60);
            mobMap.put("mowziesmobs:bluff", 85);
        }

        if (ModList.get().isLoaded("irons_spellbooks")) {
            mobMap.put("irons_spellbooks:citadel_keeper", 130);
            mobMap.put("irons_spellbooks:archevoker", 130);
            mobMap.put("irons_spellbooks:necromancer", 80);
            mobMap.put("irons_spellbooks:cryomancer", 130);
            mobMap.put("irons_spellbooks:pyromancer", 130);
            mobMap.put("irons_spellbooks:apothecarist", 130);
        }

        if (ModList.get().isLoaded("aquamirae")) {
            mobMap.put("aquamirae:tortured_soul", 40);
        }

        if (ModList.get().isLoaded("quark")) {
            mobMap.put("quark:forgotten", 90);
        }

        if (ModList.get().isLoaded("iter_rpg")) {
            mobMap.put("iter_rpg:earth_elemental", 55);
            mobMap.put("iter_rpg:water_elemental", 45);
            mobMap.put("iter_rpg:air_elemental", 55);
            mobMap.put("iter_rpg:fire_elemental", 55);
            mobMap.put("iter_rpg:void_elemental", 65);
        }

        if (ModList.get().isLoaded("alexscaves")) {
            mobMap.put("alexscaves:caniac", 40);
            mobMap.put("alexscaves:licowitch", 50);
            mobMap.put("alexscaves:deep_one_knight", 85);
            mobMap.put("alexscaves:deep_one_mage", 130);
            mobMap.put("alexscaves:brainiac", 100);
        }

        if (ModList.get().isLoaded("galosphere")) {
            mobMap.put("galosphere:berserker", 210);
        }

        if (ModList.get().isLoaded("caverns_and_chasms")) {
            mobMap.put("caverns_and_chasms:mime", 100);
        }

        mobMap.forEach((mobId, value) -> {
            List<Object> mobEntry = new ArrayList<>();
            mobEntry.add(mobId);  // ID моба
            mobEntry.add(value);   // Значение моба
            defaultMobValues.add(mobEntry);
        });
        // сундуки
        defaultLootTables.add("minecraft:chests/abandoned_mineshaft");
        defaultLootTables.add("minecraft:chests/ancient_city");
        defaultLootTables.add("minecraft:chests/ancient_city_ice_box");
        defaultLootTables.add("minecraft:chests/bastion_bridge");
        defaultLootTables.add("minecraft:chests/bastion_hoglin_stable"); //незерит
        defaultLootTables.add("minecraft:chests/bastion_other"); // незерит
        defaultLootTables.add("minecraft:chests/bastion_treasure"); // незерит много
        defaultLootTables.add("minecraft:chests/buried_treasure");
        defaultLootTables.add("minecraft:chests/desert_pyramid");
        defaultLootTables.add("minecraft:chests/end_city_treasure");
        defaultLootTables.add("minecraft:chests/igloo_chest");
        defaultLootTables.add("minecraft:chests/jungle_temple");
        defaultLootTables.add("minecraft:chests/nether_bridge");
        defaultLootTables.add("minecraft:chests/pillager_outpost");
        defaultLootTables.add("minecraft:chests/ruined_portal");
        defaultLootTables.add("minecraft:chests/shipwreck_map");
        defaultLootTables.add("minecraft:chests/shipwreck_supply");
        defaultLootTables.add("minecraft:chests/shipwreck_treasure");
        defaultLootTables.add("minecraft:chests/simple_dungeon");
        // defaultLootTables.add("minecraft:chests/spawn_bonus_chest");
        defaultLootTables.add("minecraft:chests/stronghold_corridor");
        defaultLootTables.add("minecraft:chests/stronghold_crossing");
        defaultLootTables.add("minecraft:chests/stronghold_library");
        defaultLootTables.add("minecraft:chests/underwater_ruin_big");
        defaultLootTables.add("minecraft:chests/underwater_ruin_small");
        // defaultLootTables.add("minecraft:chests/village/village_armorer"); // норм
        // defaultLootTables.add("minecraft:chests/village/village_butcher");
        // defaultLootTables.add("minecraft:chests/village/village_cartographer");
        // defaultLootTables.add("minecraft:chests/village/village_desert_house");
        // defaultLootTables.add("minecraft:chests/village/village_fisher");
        // defaultLootTables.add("minecraft:chests/village/village_fletcher");
        // defaultLootTables.add("minecraft:chests/village/village_mason");
        // defaultLootTables.add("minecraft:chests/village/village_plains_house");
        // defaultLootTables.add("minecraft:chests/village/village_savanna_house");
        // defaultLootTables.add("minecraft:chests/village/village_shepherd");
        // defaultLootTables.add("minecraft:chests/village/village_snowy_house"); // прикольно что там снежки, но так хрень
        // defaultLootTables.add("minecraft:chests/village/village_taiga_house");
        // defaultLootTables.add("minecraft:chests/village/village_tannery");
        defaultLootTables.add("minecraft:chests/village/village_temple"); // храм, там бывает благославление
        // defaultLootTables.add("minecraft:chests/village/village_toolsmith"); // тут железо только, но сойдёт
        // defaultLootTables.add("minecraft:chests/village/village_weaponsmith"); // норм
        defaultLootTables.add("minecraft:chests/woodland_mansion");

        if (ModList.get().isLoaded("born_in_chaos_v1")) {
            // defaultLootTables.add("minecraft:chests/basic_chest");
            defaultLootTables.add("minecraft:chests/chest_level_1");
            defaultLootTables.add("minecraft:chests/chest_level_2");
            defaultLootTables.add("minecraft:chests/chest_level_3");
            defaultLootTables.add("minecraft:chests/farm_drop");
            defaultLootTables.add("minecraft:chests/firewell_d");
            defaultLootTables.add("minecraft:chests/shater");
        }

        if (ModList.get().isLoaded("iceandfire")) {
            // defaultLootTables.add("iceandfire:chest/cyclops_cave");
            defaultLootTables.add("iceandfire:chest/fire_dragon_female_cave");
            defaultLootTables.add("iceandfire:chest/fire_dragon_male_cave");
            // defaultLootTables.add("iceandfire:chest/fire_dragon_roost");
            defaultLootTables.add("iceandfire:chest/graveyard");
            // defaultLootTables.add("iceandfire:chest/hydra_cave");
            defaultLootTables.add("iceandfire:chest/ice_dragon_female_cave");
            defaultLootTables.add("iceandfire:chest/ice_dragon_male_cave");
            // defaultLootTables.add("iceandfire:chest/ice_dragon_roost");
            defaultLootTables.add("iceandfire:chest/lightning_dragon_female_cave");
            defaultLootTables.add("iceandfire:chest/lightning_dragon_male_cave");
            // defaultLootTables.add("iceandfire:chest/lightning_dragon_roost");
            defaultLootTables.add("iceandfire:chest/mausoleum_chest");
            // defaultLootTables.add("iceandfire:chest/myrmex_desert_food_chest");
            // defaultLootTables.add("iceandfire:chest/myrmex_jungle_food_chest");
            // defaultLootTables.add("iceandfire:chest/myrmex_loot_chest");
            // defaultLootTables.add("iceandfire:chest/myrmex_trash_chest");
            // defaultLootTables.add("iceandfire:chest/village_scribe");
        }

        if (ModList.get().isLoaded("call_of_yucutan")) {
            defaultLootTables.add("call_of_yucutan:chest/crypt_chest_loot");
            defaultLootTables.add("call_of_yucutan:chest/overgrown_chest_loot");
        }
    }

    private static final ForgeConfigSpec.ConfigValue<Integer> STARTING_POINTS = BUILDER
            .comment("Starting points for the altar.")
            .define("startingPoints", 500);

    private static final ForgeConfigSpec.ConfigValue<Integer> POINTS_INCREASE = BUILDER
            .comment("The number of points to increase after each victory.")
            .define("pointsIncrease", 100);

    private static final ForgeConfigSpec.ConfigValue<Integer> MOB_COST_RATIO = BUILDER
            .comment("Maximum ratio of remaining points to mob cost for summoning.\n" +
                    "For example, with a value of 10 and remaining points of 500, mobs with a cost of 50 or less will not be summoned.\n" +
                    "If no mob can be summoned within 5 attempts, a random mob will be summoned regardless of its cost.")
            .define("mobCostRatio", 20);

    private static final ForgeConfigSpec.ConfigValue<Integer> BASE_SCALING_THRESHOLD = BUILDER
            .comment("Base cost threshold for scaling summoned mobs.\n" +
                    "If a mob has this cost or higher, it will always be included in the first summon wave.\n" +
                    "When remaining points exceed this value multiplied by mobCostRatio, mob stats and costs will start increasing.\n" +
                    "This ensures that difficulty increases by making mobs stronger rather than just summoning more of them.")
            .define("baseScalingThreshold", 120);


    private static final ForgeConfigSpec.ConfigValue<Double> SQUAD_SPAWN_CHANCE = BUILDER
            .comment("Chance for spawning a squad of mobs instead of a single mob.\n" +
                    "For example, a value of 0.35 means a 35% chance to spawn a squad.")
            .defineInRange("squadSpawnChance", 0.35, 0.0, 1.0);

    private static final ForgeConfigSpec.ConfigValue<Integer> SQUAD_SPAWN_SIZE = BUILDER
            .comment("Number of mobs in a squad when squad spawning occurs.\n" +
                    "For example, a value of 3 means 3 identical mobs will spawn.\n" +
                    "If there are not enough points to spawn the full squad, only as many mobs will spawn as the points allow.")
            .define("squadSpawnSize", 3);

    private static final ForgeConfigSpec.ConfigValue<Boolean> NIGHT_TIME = BUILDER
            .comment("Enables or disables setting the time to night when the battle starts.\n" +
                    "If true, the game will automatically switch to night time at the start of the battle.")
            .define("nightTime", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_RAIN = BUILDER
            .comment("Enables or disables starting rain when the battle begins.\n" +
                    "If true, it will start raining at the beginning of the battle.")
            .define("enableRain", false);

    private static final ForgeConfigSpec.BooleanValue ENABLE_MOB_ITEM_DROP = BUILDER
            .comment("Determines whether summoned mobs drop items upon death.\n" +
                    "If true, summoned mobs will drop their loot as usual.\n" +
                    "If false, no items will drop from summoned mobs.")
            .define("enableMobItemDrop", true);

    private static final ForgeConfigSpec.BooleanValue REQUIRE_EMPTY_CHEST = BUILDER
            .comment("Determines whether the reward key can only be used on empty chests.\n" +
                    "If true, the key cannot be applied to a chest that contains items.")
            .define("requireEmptyChest", false);

    private static final ForgeConfigSpec.BooleanValue ENABLE_LOSS_MESSAGE_LEAVE = BUILDER
            .comment("Should the defeat message be shown when the player leaves the arena?")
            .define("enableLossMessageLeave", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_LOSS_MESSAGE_DEATH = BUILDER
            .comment("Should the defeat message be shown when the player dies in battle?")
            .define("enableLossMessageDeath", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_UNCLAIMED_REWARD_MESSAGE = BUILDER
            .comment("Show an unclaimed reward message when any summoned mob has the Glowing effect.")
            .define("enableUnclaimedRewardMessage", true);

    private static final ForgeConfigSpec.BooleanValue INDIVIDUAL_PLAYER_STATS = BUILDER
            .comment("If true, each player has their own points and difficulty level,\n" +
                    "which are the same across all arenas.\n" +
                    "If false, each Altar Battle has its own points and difficulty level.")
            .define("individualPlayerStats", false);

    private static final ForgeConfigSpec.ConfigValue<List<List<Object>>> MOB_VALUES = BUILDER
            .comment("List of mobs with their values (e.g., [[\"minecraft:zombie\", 10]])")
            .define("mobValues", defaultMobValues);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOOT_TABLES = BUILDER
            .comment("A list of loot tables for the chest")
            .defineListAllowEmpty("lootTables", defaultLootTables, Config::validateLootTable);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static List<List<Object>> mobValues;
    public static List<ResourceLocation> lootTables;
    public static boolean isNightTime;
    public static int StartingPoints;
    public static int PointsIncrease;
    public static boolean enableRain;
    public static boolean enableMobItemDrop;
    public static int mobCostRatio;
    public static double SquadSpawnChance;
    public static int SquadSpawnSize;
    public static boolean requireEmptyChest;
    public static boolean enableLossMessageLeave;
    public static boolean enableLossMessageDeath;
    public static boolean enableUnclaimedRewardMessage;
    public static int baseScalingThreshold;
    public static boolean individualPlayerStats;


    private static boolean validateLootTable(final Object obj)
    {
        return obj instanceof final String lootTable && ResourceLocation.isValidResourceLocation(lootTable);
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        isNightTime = NIGHT_TIME.get();
        StartingPoints = STARTING_POINTS.get();
        PointsIncrease = POINTS_INCREASE.get();
        enableRain = ENABLE_RAIN.get();
        enableMobItemDrop = ENABLE_MOB_ITEM_DROP.get();
        mobCostRatio = MOB_COST_RATIO.get();
        SquadSpawnChance = SQUAD_SPAWN_CHANCE.get();
        SquadSpawnSize = SQUAD_SPAWN_SIZE.get();
        requireEmptyChest = REQUIRE_EMPTY_CHEST.get();
        enableLossMessageLeave = ENABLE_LOSS_MESSAGE_LEAVE.get();
        enableLossMessageDeath = ENABLE_LOSS_MESSAGE_DEATH.get();
        enableUnclaimedRewardMessage = ENABLE_UNCLAIMED_REWARD_MESSAGE.get();
        baseScalingThreshold = BASE_SCALING_THRESHOLD.get();
        individualPlayerStats = INDIVIDUAL_PLAYER_STATS.get();


        lootTables = LOOT_TABLES.get().stream()
                .map(ResourceLocation::new)
                .collect(Collectors.toList());

        mobValues = MOB_VALUES.get().stream()
                .filter(entry -> entry.size() == 2 && entry.get(0) instanceof String && entry.get(1) instanceof Integer)
                .filter(entry -> ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation((String) entry.get(0))))
                .collect(Collectors.toList());
    }
}
