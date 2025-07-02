package net.jrdemiurge.skyarena.item.custom;

import net.jrdemiurge.skyarena.mixin.EyeOfEnderAccessor;
import net.jrdemiurge.skyarena.util.ModTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class SkyEyeItem extends Item {

    public SkyEyeItem(Properties pProperties) {
        super(pProperties);
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack $$3 = pPlayer.getItemInHand(pHand);
        if (pLevel instanceof ServerLevel) {
            ServerLevel $$5 = (ServerLevel)pLevel;
            BlockPos $$6 = $$5.findNearestMapStructure(ModTags.EYE_OF_SKY_LOCATED, pPlayer.blockPosition(), 100, false);
            if ($$6 != null) {
                EyeOfEnder $$7 = new EyeOfEnder(pLevel, pPlayer.getX(), pPlayer.getY(0.5), pPlayer.getZ());
                $$7.setItem($$3);
                $$7.signalTo($$6);
                ((EyeOfEnderAccessor) $$7).setSurviveAfterDeath(true);
                pLevel.gameEvent(GameEvent.PROJECTILE_SHOOT, $$7.position(), GameEvent.Context.of(pPlayer));
                pLevel.addFreshEntity($$7);
                if (pPlayer instanceof ServerPlayer) {
                    CriteriaTriggers.USED_ENDER_EYE.trigger((ServerPlayer)pPlayer, $$6);
                }

                pLevel.playSound((Player)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
                pLevel.levelEvent((Player)null, 1003, pPlayer.blockPosition(), 0);
                if (!pPlayer.getAbilities().instabuild) {
                    $$3.shrink(1);
                }

                pPlayer.awardStat(Stats.ITEM_USED.get(this));
                pPlayer.swing(pHand, true);
                return InteractionResultHolder.success($$3);
            }
        }

        return InteractionResultHolder.consume($$3);
        }
}
