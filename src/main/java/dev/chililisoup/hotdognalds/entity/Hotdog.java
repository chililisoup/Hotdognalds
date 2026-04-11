package dev.chililisoup.hotdognalds.entity;

import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class Hotdog extends Entity {
    public Hotdog(EntityType<Hotdog> type, Level level) {
        super(type, level);
    }

    @Nullable
    public static Hotdog create(
            ServerLevel serverLevel,
            Vec3 position,
            float rotation,
            EntitySpawnReason entitySpawnReason,
            ItemStack itemStack,
            @Nullable Player player
    ) {
        Consumer<Hotdog> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack, player);
        Hotdog balloon = ModEntityTypes.HOTDOG.create(serverLevel, consumer, BlockPos.containing(position), entitySpawnReason, true, true);
        if (balloon == null) return null;

        balloon.snapTo(position, rotation, 0);
        serverLevel.playSound(null, balloon.getX(), balloon.getY(), balloon.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
        balloon.gameEvent(GameEvent.ENTITY_PLACE, player);
        return balloon;
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder entityData) {

    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {

    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {

    }
}
