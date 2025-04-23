package net.jrdemiurge.skyarena.item.custom;

import net.jrdemiurge.skyarena.Config;
import net.jrdemiurge.skyarena.config.SkyArenaConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RewardKeyItem extends Item {
    public RewardKeyItem(Properties pProperties) {
        super(pProperties);
    }

    public static final Set<BlockPos> keyedChests = new HashSet<>();

    public static final List<String> BLOCK_ID_BLACKLIST = List.of(
            "lootr:lootr_chest",
            "lootr:lootr_inventory",
            "lootr:lootr_trapped_chest"
    );

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos positionClicked = pContext.getClickedPos();
        Player player = pContext.getPlayer();
        InteractionHand hand = pContext.getHand();

        if (!level.isClientSide() && player != null) {
            BlockEntity blockEntity = level.getBlockEntity(positionClicked);

            BlockState clickedBlockState = level.getBlockState(positionClicked);
            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(clickedBlockState.getBlock());

            if (blockId != null && BLOCK_ID_BLACKLIST.contains(blockId.toString())) {
                return InteractionResult.PASS;
            }

            if (blockEntity instanceof ChestBlockEntity chestEntity) {

                if (Config.requireEmptyChest && !chestEntity.isEmpty()) {
                    return InteractionResult.FAIL;
                }

                chestEntity.clearContent();

                ItemStack itemStack = player.getItemInHand(hand);
                String itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();

                List<String> lootTables = SkyArenaConfig.configData.keys.getOrDefault(itemId, SkyArenaConfig.DEFAULT_KEY);

                LootDataManager lootManager = level.getServer().getLootData();
                List<ResourceLocation> validLootTables = lootTables.stream()
                        .map(ResourceLocation::new)
                        .filter(id -> lootManager.getLootTable(id) != LootTable.EMPTY)
                        .toList();
                
                if (!validLootTables.isEmpty()) {
                    ResourceLocation randomLootTable = validLootTables.get(new Random().nextInt(validLootTables.size()));
                    System.out.println("Loot table: " + randomLootTable);
                    chestEntity.setLootTable(randomLootTable, player.getRandom().nextLong());
                    chestEntity.unpackLootTable(player);
                }

                int keyCount = itemStack.getCount();

                player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 15);
                player.getItemInHand(hand).shrink(1);
                level.playSound(null, positionClicked, SoundEvents.AMETHYST_BLOCK_HIT  , SoundSource.PLAYERS, 5.0F, 1.0F);

                if (keyCount == 1){
                    keyedChests.add(positionClicked.immutable());
                }

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("tooltip.skyarena.reward_key"));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
