package net.jrdemiurge.skyarena;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;


@Mod.EventBusSubscriber(modid = SkyArena.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue REQUIRE_EMPTY_CHEST = BUILDER
            .comment("""
                    Description for customizing the skyarena.json config can be found at the following link:
                    https://github.com/JrDemiurg/DemisSkyArenaMod/wiki


                    If true, the reward key can only be used on empty chests.
                    If false, the chest's contents will be cleared before being filled with loot.""")
            .define("requireEmptyChest", false);

    private static final ForgeConfigSpec.BooleanValue ENABLE_LOSS_MESSAGE_LEAVE = BUILDER
            .comment("\nIf true, a defeat message will be shown when the player leaves the arena.")
            .define("enableLossMessageLeave", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_LOSS_MESSAGE_DEATH = BUILDER
            .comment("\nIf true, a defeat message will be shown when the player dies in battle.")
            .define("enableLossMessageDeath", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_UNCLAIMED_REWARD_MESSAGE = BUILDER
            .comment("""
                    
                    If true, when mobs receive the Glowing effect, a message will appear recommending leaving the arena to restart the battle.
                    The message appears only once per game session.""")
            .define("enableUnclaimedRewardMessage", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean requireEmptyChest;
    public static boolean enableLossMessageLeave;
    public static boolean enableLossMessageDeath;
    public static boolean enableUnclaimedRewardMessage;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        requireEmptyChest = REQUIRE_EMPTY_CHEST.get();
        enableLossMessageLeave = ENABLE_LOSS_MESSAGE_LEAVE.get();
        enableLossMessageDeath = ENABLE_LOSS_MESSAGE_DEATH.get();
        enableUnclaimedRewardMessage = ENABLE_UNCLAIMED_REWARD_MESSAGE.get();
    }
}
