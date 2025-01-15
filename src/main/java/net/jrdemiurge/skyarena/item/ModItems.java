package net.jrdemiurge.skyarena.item;

import net.jrdemiurge.skyarena.SkyArena;
import net.jrdemiurge.skyarena.item.custom.RewardKeyItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SkyArena.MOD_ID);

    public static final RegistryObject<Item> FOREST_KEY = ITEMS.register("forest_key",
            () -> new RewardKeyItem(new Item.Properties()));

    public static final RegistryObject<Item> CRIMSON_KEY = ITEMS.register("crimson_key",
            () -> new RewardKeyItem(new Item.Properties()));

    public static final RegistryObject<Item> DESERT_KEY = ITEMS.register("desert_key",
            () -> new RewardKeyItem(new Item.Properties()));

    public static final RegistryObject<Item> ICE_KEY = ITEMS.register("ice_key",
            () -> new RewardKeyItem(new Item.Properties()));

    public static final RegistryObject<Item> ENDER_KEY = ITEMS.register("ender_key",
            () -> new RewardKeyItem(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
