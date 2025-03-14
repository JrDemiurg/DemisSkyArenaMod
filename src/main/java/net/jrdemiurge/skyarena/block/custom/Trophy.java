package net.jrdemiurge.skyarena.block.custom;

import net.jrdemiurge.skyarena.block.ModBlocks;
import net.jrdemiurge.skyarena.config.SkyArenaConfig;
import net.jrdemiurge.skyarena.config.TrophyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class Trophy extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public Trophy(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            String trophyKey = this.asItem().toString();

            TrophyConfig trophyConfig = SkyArenaConfig.configData.trophies.getOrDefault(trophyKey, SkyArenaConfig.DEFAULT_TROPHY);
            if (trophyConfig == null) {
                return InteractionResult.FAIL;
            }

            int cooldownTicks = trophyConfig.cooldown * 20;
            if (pPlayer.getCooldowns().isOnCooldown(this.asItem())) {
                float cooldownPercent = pPlayer.getCooldowns().getCooldownPercent(this.asItem(), 0.0F);
                int remainingCooldownTicks = (int) (cooldownPercent * cooldownTicks);
                int secondsLeft = remainingCooldownTicks / 20;

                pPlayer.displayClientMessage(Component.translatable("message.skyarena.cooldown_remaining", secondsLeft), true);
                return InteractionResult.FAIL;
            }

            pPlayer.getCooldowns().addCooldown(this.asItem(), cooldownTicks);

            for (Map.Entry<String, TrophyConfig.EffectConfig> entry : trophyConfig.effects.entrySet()) {
                MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(entry.getKey()));
                if (effect != null) {
                    TrophyConfig.EffectConfig effectConfig = entry.getValue();
                    pPlayer.addEffect(new MobEffectInstance(effect, effectConfig.duration * 20, effectConfig.amplifier, false, false));
                }
            }

            pLevel.playSound(null, pPos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 5.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 13, 16, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 0, 16, 16, 3);
    private static final VoxelShape SHAPE_WEST = Block.box(13, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 0, 0, 3, 16, 16);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);

        return switch (facing) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        pTooltip.add(Component.translatable("tooltip.skyarena.trophy"));
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }
}
