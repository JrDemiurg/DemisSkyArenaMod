package net.jrdemiurge.skyarena.block.entity;

import net.jrdemiurge.skyarena.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
    private boolean PlayerDeath = false;
    private int DeathDelay = 0;
    private int BattleDelay = 0;
    private int glowingCounter = 0;
    private String arenaType = "sky_arena";
    private int spawnRadius = 21;
    private boolean firstMessageSent = false;
    // музыка
    public ItemStack recordItem = ItemStack.EMPTY;
    private boolean isPlayingMusic = false;
    private long musicEndTick = 0;
    private long musicTickCount = 0;
    // сложность
    private int remainingPoints;
    private boolean isNewBlock = true;
    private int difficultyLevel = 1;
    private int battleDifficultyLevel = 1;

    private static final Map<String, Integer> playerPoints = new HashMap<>();
    private static final Map<String, Integer> playerDifficulty = new HashMap<>();

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

    public int getRemainingPoints(Player player) {
        if (Config.individualPlayerStats) {
            return playerPoints.getOrDefault(player.getGameProfile().getName(), Config.StartingPoints);
        }
        return remainingPoints;
    }

    public void addPoints(int points) {
        this.remainingPoints += points;

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }

    public void addPoints(Player player, int points) {
        if (Config.individualPlayerStats) {
            playerPoints.put(player.getGameProfile().getName(),
                    getRemainingPoints(player) + points);
        } else {
            this.remainingPoints += points;
        }

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

    public void setPoints(Player player, int points) {
        if (Config.individualPlayerStats) {
            playerPoints.put(player.getGameProfile().getName(), points);
        } else {
            this.remainingPoints = points;
        }

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
    }

    public void setRecordItem(ItemStack stack) {
        if (stack.getItem() instanceof RecordItem) {
            this.recordItem = stack.copy();

            if (this.level != null) {
                this.level.blockEntityChanged(this.getBlockPos());
            }
        }
    }

    public void clearRecordItem() {
        this.recordItem = ItemStack.EMPTY;

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public int getDifficultyLevel(Player player) {
        if (Config.individualPlayerStats) {
            return playerDifficulty.getOrDefault(player.getGameProfile().getName(), 1);
        }
        return difficultyLevel;
    }

    public void increaseDifficultyLevel() {
        difficultyLevel++;

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }

    public void increaseDifficultyLevel(Player player) {
        if (Config.individualPlayerStats) {
            playerDifficulty.put(player.getGameProfile().getName(),
                    getDifficultyLevel(player) + 1);
        } else {
            this.difficultyLevel++;
        }

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }

    public void setDifficultyLevel(Player player, int level) {
        if (Config.individualPlayerStats) {
            playerDifficulty.put(player.getGameProfile().getName(), level);
        } else {
            this.difficultyLevel = level;
        }

        if (this.level != null) {
            this.level.blockEntityChanged(this.getBlockPos());
        }
    }


    public int getBattleDifficultyLevel() {
        return battleDifficultyLevel;
    }

    public void setBattleDifficultyLevel(int battleDifficultyLevel) {
        this.battleDifficultyLevel = battleDifficultyLevel;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("RemainingPoints", this.remainingPoints);
        pTag.putBoolean("IsNewBlock", this.isNewBlock);
        pTag.putInt("DifficultyLevel", this.difficultyLevel);
        pTag.putString("ArenaType", this.arenaType);
        pTag.putInt("SpawnRadius", this.spawnRadius);
        if (!this.recordItem.isEmpty()) {
            pTag.put("RecordItem", this.recordItem.save(new CompoundTag()));
        }

        if (!playerPoints.isEmpty()) {
            CompoundTag playersTag = new CompoundTag();
            for (Map.Entry<String, Integer> entry : playerPoints.entrySet()) {
                playersTag.putInt(entry.getKey(), entry.getValue());
            }
            pTag.put("PlayerPoints", playersTag);
        }

        if (!playerDifficulty.isEmpty()) {
            CompoundTag difficultyTag = new CompoundTag();
            for (Map.Entry<String, Integer> entry : playerDifficulty.entrySet()) {
                difficultyTag.putInt(entry.getKey(), entry.getValue());
            }
            pTag.put("PlayerDifficulty", difficultyTag);
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
        if (pTag.contains("DifficultyLevel")) {
            this.difficultyLevel = pTag.getInt("DifficultyLevel");
        }
        if (pTag.contains("ArenaType")) {
            this.arenaType = pTag.getString("ArenaType");
        }
        if (pTag.contains("SpawnRadius")) {
            this.spawnRadius = pTag.getInt("SpawnRadius");
        }
        if (pTag.contains("RecordItem")) {
            this.recordItem = ItemStack.of(pTag.getCompound("RecordItem"));
        }
        if (pTag.contains("PlayerPoints")) {
            CompoundTag playersTag = pTag.getCompound("PlayerPoints");
            for (String key : playersTag.getAllKeys()) {
                playerPoints.put(key, playersTag.getInt(key));
            }
        }
        if (pTag.contains("PlayerDifficulty")) {
            CompoundTag difficultyTag = pTag.getCompound("PlayerDifficulty");
            for (String key : difficultyTag.getAllKeys()) {
                playerDifficulty.put(key, difficultyTag.getInt(key));
            }
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

    public void startMusic() {
        if (this.level != null && !recordItem.isEmpty() && !isPlayingMusic) {
            this.musicTickCount = this.level.getGameTime();
            RecordItem record = (RecordItem) recordItem.getItem();
            this.musicEndTick = this.musicTickCount + record.getLengthInTicks() + 20L;
            this.isPlayingMusic = true;
            this.level.levelEvent(null, 1010, this.getBlockPos(), Item.getId(recordItem.getItem())); // Используем предмет пластинки
            this.setChanged();
        }
    }

    public void stopMusic() {
        if (this.level != null && isPlayingMusic) {
            this.isPlayingMusic = false;
            this.level.levelEvent(1011, this.getBlockPos(), 0); // Останавливаем музыку
            this.setChanged();
        }
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (this.isPlayingMusic && this.level != null && !recordItem.isEmpty()) {

            if (this.musicTickCount >= this.musicEndTick) {
                stopMusic();
                startMusic();
            }
            ++this.musicTickCount;
        }

        if (activatingPlayer != null && battlePhaseActive) {
            double distance = activatingPlayer.distanceToSqr(pPos.getX(), pPos.getY(), pPos.getZ());

            if (distance > 60 * 60) {
                removeSummonedMobs();
                toggleBattlePhase();
                this.stopMusic();
                removeAltarActivationForPlayer(/*player*/);
                PlayerDeath = false;
                DeathDelay = 0;

                if (activatingPlayer instanceof ServerPlayer serverPlayer) {
                    if (Config.enableLossMessageLeave) {
                        Component title = Component.translatable("message.skyarena.battle_failed").withStyle(ChatFormatting.DARK_RED);
                        Component subtitle = Component.translatable("message.skyarena.left_arena").withStyle(ChatFormatting.WHITE);
                        serverPlayer.connection.send(new ClientboundSetTitleTextPacket(title));
                        serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                        serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 10)); // Время появления и исчезновения
                    }
                }
            }
        }

        if (PlayerDeath){
            DeathDelay++;

            if (DeathDelay > 10) {
                // Проверяем, использовался ли тотем бессмертия
                if (activatingPlayer.getHealth() > 0) {
                    PlayerDeath = false;
                    DeathDelay = 0;
                    return;
                }
                removeSummonedMobs();
                toggleBattlePhase();
                this.stopMusic();
                removeAltarActivationForPlayer(/*player*/);
                PlayerDeath = false;
                DeathDelay = 0;

                if (activatingPlayer instanceof ServerPlayer serverPlayer) {
                    if (Config.enableLossMessageDeath) {
                        Component title = Component.translatable("message.skyarena.wasted").withStyle(ChatFormatting.WHITE);
                        Component subtitle = Component.translatable("message.skyarena.battle_failed").withStyle(ChatFormatting.DARK_RED);
                        serverPlayer.connection.send(new ClientboundSetTitleTextPacket(title)); // Верхняя строка
                        serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(subtitle)); // Нижняя строка
                        serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 10)); // Время появления и исчезновения
                    }
                }
            }
        }

        if (BattleDelay > 0) {
            BattleDelay--;
        }

        if (activatingPlayer != null && battlePhaseActive) {
            summonedMobs.removeIf(Entity::isRemoved);
            for (Entity entity : summonedMobs) {

                if (entity instanceof NeutralMob neutralMob) {
                    if (!(activatingPlayer.isCreative() || activatingPlayer.isSpectator())) {
                        double mobDistance = activatingPlayer.distanceToSqr(entity.getX(), entity.getY(), entity.getZ());
                        if (mobDistance <= 16 * 16) {
                            if (neutralMob.getTarget() == null || !neutralMob.getTarget().isAlive()) {
                                neutralMob.setTarget(activatingPlayer);
                            }
                        }
                    }
                }

                double mobDistanceToAltar = entity.distanceToSqr(pPos.getX(), pPos.getY(), pPos.getZ());
                if (mobDistanceToAltar > 60 * 60) {
                    List<BlockPos> spawnPositions = findValidSpawnPositions(pLevel, pPos, spawnRadius, activatingPlayer);

                    if (!spawnPositions.isEmpty()) {
                        BlockPos teleportPos = spawnPositions.get(pLevel.random.nextInt(spawnPositions.size()));
                        entity.teleportTo(teleportPos.getX() + 0.5, teleportPos.getY(), teleportPos.getZ() + 0.5);
                    }
                    continue;
                }
            }
        }
    }

    public List<BlockPos> findValidSpawnPositions(Level level, BlockPos center, int radius, Player player) {
        List<BlockPos> validPositions = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos currentPos = center.offset(x, 0, z);

                if (x * x + z * z <= radius * radius && //делаем область кругом, чтобы обрезать углы за ареной
                        level.isEmptyBlock(currentPos) &&
                        level.isEmptyBlock(currentPos.above()) &&
                        level.isEmptyBlock(currentPos.above(2)) &&
                        level.isEmptyBlock(currentPos.north()) && level.isEmptyBlock(currentPos.south()) &&
                        level.isEmptyBlock(currentPos.east()) && level.isEmptyBlock(currentPos.west()) &&
                        !level.isEmptyBlock(currentPos.below()) &&
                        player.blockPosition().distSqr(currentPos) > 10 * 10) {
                    validPositions.add(currentPos);
                }
            }
        }

        return validPositions;
    }

    public void setGlowingCounter(int glowingCounter) {
        this.glowingCounter = glowingCounter;
    }

    public void applyGlowEffectToSummonedMobs(Player pPlayer) {
        glowingCounter++;

        if (glowingCounter > 4) {
            int duration = 12000; // Длительность эффекта в тиках (10 минут)
            int amplifier = 0; // Усиление эффекта (0 — базовый уровень)
            glowingCounter = 0;

            int affectedMobs = 0;

            for (Entity mob : summonedMobs) {
                if (mob instanceof LivingEntity) {
                    LivingEntity livingMob = (LivingEntity) mob;
                    livingMob.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, amplifier, false, false));
                    affectedMobs++;
                }
            }
            if (Config.enableUnclaimedRewardMessage) {
                if (!firstMessageSent) {
                    pPlayer.displayClientMessage(Component.translatable("message.skyarena.unclaimed_reward"), false);
                    firstMessageSent = true;
                } else {
                    pPlayer.displayClientMessage(Component.translatable("message.skyarena.mobs_glowing", affectedMobs), true);
                }
            }
        }
    }
}
