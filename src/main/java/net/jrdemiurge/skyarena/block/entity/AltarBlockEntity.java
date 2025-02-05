package net.jrdemiurge.skyarena.block.entity;

import net.jrdemiurge.skyarena.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AltarBlockEntity extends BlockEntity {

    private static final Map<Player, BlockPos> activeAltarBlocks = new HashMap<>();
    private Player activatingPlayer;
    public final List<Entity> summonedMobs = new ArrayList<>();
    private boolean battlePhaseActive = false;
    private int remainingPoints;
    private boolean isNewBlock = true;
    private boolean PlayerDeath = false;
    private int DeathDelay = 0;
    private ResourceLocation recordMusic;
    private int BattleDelay = 0;
    private int glowingCounter = 0;
    private int difficultyLevel = 1;
    private String arenaType = "sky_arena";
    private int spawnRadius = 21;

    public AltarBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntity.ALTAR_BLOCK_ENTITY.get(), pPos, pBlockState);
        this.remainingPoints = Config.StartingPoints;
        this.isNewBlock = true;
    }

    public String getArenaType() {
        return arenaType;
    }

    public void setArenaType(String arenaType) {
        switch (arenaType) {
            case "sky_arena":
                this.spawnRadius = 21;
                this.arenaType = arenaType;
                break;
            case "ice_arena":
                this.spawnRadius = 37;
                this.arenaType = arenaType;
                break;
            default:
                this.spawnRadius = 21;
                this.arenaType = "sky_arena";
                break;
        }
        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }

    public int getSpawnRadius() {
        return spawnRadius;
    }

    public boolean isNewBlock() {
        return isNewBlock;
    }

    public void setNewBlock(boolean isNewBlock) {
        this.isNewBlock = isNewBlock;

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }

    public void addSummonedMob(Entity mob) {
        summonedMobs.add(mob);
    }

    public boolean canSummonMobs() {
        summonedMobs.removeIf(Entity::isRemoved);
        return summonedMobs.isEmpty();
    }

    public void toggleBattlePhase() {
        battlePhaseActive = !battlePhaseActive;
    }

    public boolean isBattlePhaseActive() {
        return battlePhaseActive;
    }

    public void recordAltarActivation(Player player, BlockPos pos) {
        activeAltarBlocks.put(player, pos); // Сохраняем активированный блок для игрока
        activatingPlayer = player;
    }

    public static BlockPos getAltarPosForPlayer(Player player) {
        return activeAltarBlocks.get(player); // Получаем позицию блока для игрока
    }

    public void removeAltarActivationForPlayer(/*Player player*/) {
        activeAltarBlocks.remove(activatingPlayer); // Удаляем игрока из активных блоков
    }

    public int getRemainingPoints() {
        return remainingPoints;
    }

    public void addPoints(int points) {
        this.remainingPoints += points;

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }

    public void setPoints(int points) {
        this.remainingPoints = points;

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        removeSummonedMobs();
    }

    public void removeSummonedMobs() {
        for (Entity mob : summonedMobs) {
            if (mob != null && mob.isAlive()) {
                mob.remove(Entity.RemovalReason.DISCARDED);
            }
        }
        summonedMobs.clear();
        // Убираем стену
        /*if (this.level != null) {
            BlockState blockState = this.getBlockState();
            Direction direction = blockState.getValue(HorizontalDirectionalBlock.FACING); // Направление вашего блока
            createWall(this.level, this.getBlockPos(), direction, Blocks.AIR); // Заменяем стену на воздух
        }*/
    }

    /*public void createWall(Level pLevel, BlockPos altarPos, Direction direction, Block blockType) {
        if (!(Config.createBarrierWall)) {
            return;
        }
        BlockPos wallStart;
        BlockPos wallEnd;

        // Определяем координаты стены в зависимости от направления
        switch (direction) {
            case NORTH -> {
                wallStart = altarPos.offset(-1, 0, -22); // Стена для севера
                wallEnd = altarPos.offset(1, 14, -22);
            }
            case SOUTH -> {
                wallStart = altarPos.offset(-1, 0, 22); // Стена для юга
                wallEnd = altarPos.offset(1, 14, 22);
            }
            case WEST -> {
                wallStart = altarPos.offset(-22, 0, -1); // Стена для запада
                wallEnd = altarPos.offset(-22, 14, 1);
            }
            case EAST -> {
                wallStart = altarPos.offset(22, 0, -1); // Стена для востока
                wallEnd = altarPos.offset(22, 14, 1);
            }
            default -> { // На случай неизвестного направления (по умолчанию север)
                wallStart = altarPos.offset(-1, 0, -19);
                wallEnd = altarPos.offset(1, 14, -19);
            }
        }

        // Создаем стену с указанным типом блока
        for (int x = wallStart.getX(); x <= wallEnd.getX(); x++) {
            for (int y = wallStart.getY(); y <= wallEnd.getY(); y++) {
                for (int z = wallStart.getZ(); z <= wallEnd.getZ(); z++) {
                    BlockPos targetPos = new BlockPos(x, y, z);
                    pLevel.setBlock(targetPos, blockType.defaultBlockState(), 3);
                }
            }
        }
    }*/

    public ResourceLocation getRecordMusic() {
        return recordMusic;
    }

    public void setRecordMusic(ResourceLocation recordMusic) {
        this.recordMusic = recordMusic;

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void increaseDifficultyLevel() {
        difficultyLevel++;

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("RemainingPoints", this.remainingPoints);
        pTag.putBoolean("IsNewBlock", this.isNewBlock);
        pTag.putInt("DifficultyLevel", this.difficultyLevel);
        pTag.putString("ArenaType", this.arenaType);
        pTag.putInt("SpawnRadius", this.spawnRadius);
        if (this.recordMusic != null) {
            pTag.putString("RecordMusic", this.recordMusic.toString());
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("RemainingPoints")) {
            this.remainingPoints = pTag.getInt("RemainingPoints");
        }
        if (pTag.contains("IsNewBlock")) {
            this.isNewBlock = pTag.getBoolean("IsNewBlock");
        }
        if (pTag.contains("RecordMusic")) {
            recordMusic = new ResourceLocation(pTag.getString("RecordMusic"));
        }
        if (pTag.contains("DifficultyLevel")) {
            this.difficultyLevel = pTag.getInt("DifficultyLevel");
        }
        if (pTag.contains("ArenaType")) {
            this.arenaType = pTag.getString("ArenaType");
        }
        if (pTag.contains("SpawnRadius")) {
            this.spawnRadius = pTag.getInt("SpawnRadius");
        }
    }

    public void setPlayerDeath(boolean death) {
        PlayerDeath = death;
    }

    public int getBattleDelay() {
        return BattleDelay;
    }

    public void setBattleDelay(int battleDelay) {
        this.BattleDelay = battleDelay;
    }
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (activatingPlayer != null && battlePhaseActive) {
            double distance = activatingPlayer.distanceToSqr(pPos.getX(), pPos.getY(), pPos.getZ());

            if (distance > 100 * 100) { // Проверяем расстояние (100 блоков в квадрате)
                removeSummonedMobs(); // Удаляем все призванные мобы
                toggleBattlePhase();
                removeAltarActivationForPlayer(/*player*/);
                PlayerDeath = false;
                DeathDelay = 0;
            }
        }

        if (PlayerDeath){
            DeathDelay++;

            if (DeathDelay > 10) {
                // Проверяем, использовался ли тотем бессмертия
                if (/*activatingPlayer.getLastDamageSource() != null && */activatingPlayer.getHealth() > 0) {
                    // Если здоровье игрока больше 0, это значит, что тотем активировался
                    PlayerDeath = false;
                    DeathDelay = 0;
                    return; // Выходим из метода, мобы не исчезают
                }
                removeSummonedMobs(); // Удаляем все призванные мобы
                toggleBattlePhase();
                removeAltarActivationForPlayer(/*player*/);
                PlayerDeath = false;
                DeathDelay = 0;
            }
        }
        if (BattleDelay > 0) {
            BattleDelay--;
        }
    }

    public void applyGlowEffectToSummonedMobs(Player pPlayer) {
        glowingCounter++;

        if (glowingCounter > 4) {
            int duration = 12000; // Длительность эффекта в тиках (10 минут)
            int amplifier = 0; // Усиление эффекта (0 — базовый уровень)
            glowingCounter = 0;

            for (Entity mob : summonedMobs) {
                if (mob instanceof LivingEntity) {
                    LivingEntity livingMob = (LivingEntity) mob;
                    livingMob.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, amplifier));
                }
            }
            pPlayer.displayClientMessage(Component.translatable("message.skyarena.unclaimed_reward"), false);
        }
    }
}
