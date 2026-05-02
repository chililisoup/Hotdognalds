package dev.chililisoup.hotdognalds.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FoodEntity extends Entity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(FoodEntity.class, EntityDataSerializers.ITEM_STACK);

    protected final InterpolationHandler interpolation = new InterpolationHandler(this);

    public <T extends FoodEntity> FoodEntity(EntityType<T> type, Level level) {
        super(type, level);
    }

    public static <T extends FoodEntity> @Nullable T create(
            EntityType<T> type,
            ServerLevel serverLevel,
            Vec3 position,
            float rotation,
            EntitySpawnReason entitySpawnReason,
            ItemStack itemStack,
            @Nullable Player player
    ) {
        T foodEntity = type.create(
                serverLevel,
                EntityType.createDefaultStackConfig(serverLevel, itemStack, player),
                BlockPos.containing(position),
                entitySpawnReason,
                true,
                true
        );
        if (foodEntity == null) return null;

        foodEntity.setItem(itemStack.copyWithCount(1));
        foodEntity.snapTo(position, rotation, 0);
        foodEntity.playPlaceSound();
        foodEntity.gameEvent(GameEvent.ENTITY_PLACE, player);
        return foodEntity;
    }

    protected void playPlaceSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 0.75F, 1F);
    }

    protected void playTakeSound() {
        this.playSound(SoundEvents.PAINTING_BREAK, 0.75F, 1F);
    }

    @Override
    public @NotNull InterpolationHandler getInterpolation() {
        return this.interpolation;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) return;

        if (this.isInterpolating()) this.getInterpolation().interpolate();

        if (this.isLocalInstanceAuthoritative()) {
            Vec3 deltaMovement = this.getDeltaMovement()
                    .scale(0.95)
                    .add(0, -this.getGravity(), 0);
            if (this.onGround()) deltaMovement = deltaMovement.multiply(0.8, 1, 0.8);
            this.setDeltaMovement(deltaMovement);
            this.move(MoverType.SELF, deltaMovement);
        }

        if (this.level() instanceof ServerLevel serverLevel && this.isInWall())
            this.hurtServer(serverLevel, this.damageSources().inWall(), 1.0F);

        this.applyEffectsFromBlocks();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.08;
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 location) {
        if (!this.isRemoved() && player.level() instanceof ServerLevel serverLevel) {
            this.kill(serverLevel);
            this.markHurt();

            ItemStack item = this.getItem();
            if (player.getItemInHand(hand).isEmpty()) player.setItemInHand(hand, item);
            else player.addItem(item);

            this.playTakeSound();
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    public void doCookEffect() {
        if (this.getInBlockState().getBlock() instanceof BaseFireBlock) return;
        if (this.level().isClientSide()) this.doClientCookEffect();
        else if (this.random.nextFloat() > 0.95F)
            this.playSound(SoundEvents.GENERIC_BURN, 0.125F, 0.5F);
    }

    protected void doClientCookEffect() {}

    protected void placeFire(@NotNull ServerLevel level) {
        BlockPos blockPos = this.blockPosition();
        if (BaseFireBlock.canBePlacedAt(level, blockPos, Direction.DOWN)) {
            level.setBlock(blockPos, BaseFireBlock.getState(level, blockPos), 11);
            this.playSound(SoundEvents.FIRECHARGE_USE);
            level.gameEvent(this, GameEvent.BLOCK_PLACE, blockPos);
        }
    }

    protected abstract Item getDefaultItem();

    protected ItemStack getItemRaw() {
        return this.getEntityData().get(DATA_ITEM);
    }

    protected ItemStack getItem() {
        return this.getItemRaw().copy();
    }

    protected void setItem(ItemStack stack) {
        this.getEntityData().set(DATA_ITEM, stack);
    }

    public boolean hasFoil() {
        return this.getItemRaw().hasFoil();
    }

    @Override
    protected @NotNull Component getTypeName() {
        return this.getItem().getItemName();
    }

    @Override
    public ItemStack getPickResult() {
        return this.getItem().copy();
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder entityData) {
        entityData.define(DATA_ITEM, new ItemStack(this.getDefaultItem()));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("Item", ItemStack.CODEC, this.getItem());
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        this.setItem(input.read("Item", ItemStack.CODEC).orElseGet(() -> new ItemStack(this.getDefaultItem())));
    }
}
