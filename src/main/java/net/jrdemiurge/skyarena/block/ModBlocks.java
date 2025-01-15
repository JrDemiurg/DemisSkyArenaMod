package net.jrdemiurge.skyarena.block;

import net.jrdemiurge.skyarena.SkyArena;
import net.jrdemiurge.skyarena.block.custom.*;
import net.jrdemiurge.skyarena.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SkyArena.MOD_ID);

    public static final RegistryObject<Block> ALTAR_BATTLE = registerBlock("altar_battle",
            () -> new AltarBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noOcclusion().
                    lightLevel((state) -> 5)
                    .strength(-1.0F, 3600000.0F)  // Устанавливает прочность, как у бедрока
            ));

    public static final RegistryObject<Block> ALTAR_BATTLE_TOP = registerBlock("altar_battle_top",
            () -> new AltarBlockTop(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noOcclusion().
                    lightLevel((state) -> 5)
                    .strength(-1.0F, 3600000.0F)  // Устанавливает прочность, как у бедрока
            ));

    public static final RegistryObject<Block> NETHERITE_TROPHY = registerBlock("netherite_trophy",
            () -> new NetheriteTrophy(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
                    .noOcclusion().
                    lightLevel((state) -> 5)
            ));

    public static final RegistryObject<Block> OAK_TROPHY = registerBlock("oak_trophy",
            () -> new OakTrophy(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .noOcclusion().
                    lightLevel((state) -> 5)
            ));

    public static final RegistryObject<Block> STONE_TROPHY = registerBlock("stone_trophy",
            () -> new StoneTrophy(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().
                    lightLevel((state) -> 5)
            ));

    public static final RegistryObject<Block> IRON_TROPHY = registerBlock("iron_trophy",
            () -> new IronTrophy(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noOcclusion().
                    lightLevel((state) -> 5)
            ));

    public static final RegistryObject<Block> GOLD_TROPHY = registerBlock("gold_trophy",
            () -> new GoldTrophy(BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK)
                    .noOcclusion().
                    lightLevel((state) -> 5)
            ));

    public static final RegistryObject<Block> DIAMOND_TROPHY = registerBlock("diamond_trophy",
            () -> new DiamondTrophy(BlockBehaviour.Properties.copy(Blocks.DIAMOND_BLOCK)
                    .noOcclusion().
                    lightLevel((state) -> 5)
            ));

    private static <T extends Block> RegistryObject <T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
