package dev.chililisoup.hotdognalds.entity;

import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import dev.chililisoup.hotdognalds.reg.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class Hotdog extends Entity {
    private static final EntityDataAccessor<Float> DATA_COOK_AMT = SynchedEntityData.defineId(Hotdog.class, EntityDataSerializers.FLOAT);

    protected InterpolationHandler interpolation = new InterpolationHandler(this);

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
        Hotdog hotdog = ModEntityTypes.HOTDOG.create(serverLevel, consumer, BlockPos.containing(position), entitySpawnReason, true, true);
        if (hotdog == null) return null;

        hotdog.setCookAmt(itemStack.getOrDefault(ModComponents.COOK_AMOUNT, 0F));
        hotdog.snapTo(position, rotation, 0);
        hotdog.playSound(SoundEvents.SLIME_BLOCK_PLACE, 0.75F, 1F);
        hotdog.gameEvent(GameEvent.ENTITY_PLACE, player);
        return hotdog;
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
            this.setDeltaMovement(deltaMovement);
            this.move(MoverType.SELF, deltaMovement);
        } else this.setDeltaMovement(Vec3.ZERO);

        this.applyEffectsFromBlocks();
    }

    public void doCookEffect() {
        if (this.getInBlockState().getBlock() instanceof BaseFireBlock) return;

        if (this.level().isClientSide()) {
            float xd = this.random.nextBoolean() ? -0.0625F : 0.0625F;
            float zd = (this.random.nextFloat() - 0.5F) * 0.5F;

            float angle = Mth.PI * this.getYRot() / 180F;
            float sin = Mth.sin(angle);
            float cos = Mth.cos(angle);

            float xn = xd * cos - zd * sin;
            float zn = xd * sin + zd * cos;

            this.level().addParticle(ParticleTypes.SMALL_FLAME, this.getX() + xn, this.getY(), this.getZ() + zn, 0.0, 0.0, 0.0);
        } else if (this.random.nextFloat() > 0.95F)
            this.playSound(SoundEvents.GENERIC_BURN, 0.125F, 0.5F);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.08;
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 location) {
        if (player.isSpectator()) return InteractionResult.SUCCESS;

        ItemStack handStack = player.getItemInHand(hand);
        ItemStack hotdogStack = this.getItemStack();
        if (!handStack.isEmpty() && !ItemStack.isSameItemSameComponents(handStack, hotdogStack))
            return InteractionResult.PASS;

        if (!this.isRemoved() && player.level() instanceof ServerLevel serverLevel) {
            this.kill(serverLevel);
            this.markHurt();

            if (handStack.isEmpty()) player.setItemInHand(hand, hotdogStack);
            else handStack.grow(1);
            this.playSound(SoundEvents.SLIME_BLOCK_BREAK, 0.75F, 1F);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    public float getCookAmt() {
        return Math.clamp(this.getEntityData().get(DATA_COOK_AMT), 0F, 3F);
    }

    public void setCookAmt(float cookAmt) {
        this.getEntityData().set(DATA_COOK_AMT, Math.clamp(cookAmt, 0F, 3F));
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float damage) {
        if (this.isInvulnerableToBase(source)) return false;
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING) && source.getEntity() instanceof Mob)
            return false;
        if (this.isRemoved()) return true;

        if (source.is(DamageTypes.HOT_FLOOR)) {
            float cookAmt = this.getCookAmt();
            if (cookAmt < 3F) this.setCookAmt(cookAmt + 0.001F * damage);
            else if (this.random.nextFloat() > 0.99F) {
                BlockPos blockPos = this.blockPosition();
                if (BaseFireBlock.canBePlacedAt(level, blockPos, Direction.DOWN)) {
                    level.setBlock(blockPos, BaseFireBlock.getState(level, blockPos), 11);
                    this.playSound(SoundEvents.FIRECHARGE_USE);
                    level.gameEvent(this, GameEvent.BLOCK_PLACE, blockPos);
                }
            }
        } else if (source.is(DamageTypeTags.IS_FIRE)) {
            this.setCookAmt(3F);
        } else {
            this.kill(level);
            this.markHurt();

            ItemStack drop = this.getItemStack();
            Block.popResource(this.level(), this.blockPosition(), drop);
            this.playSound(SoundEvents.SLIME_BLOCK_BREAK, 0.75F, 1F);
        }

        return true;
    }

    private ItemStack getItemStack() {
        ItemStack result = ModItems.HOTDOG.getDefaultInstance();

        float cookAmt = this.getCookAmt();
        if (cookAmt >= 1F && cookAmt <= 2F) cookAmt = 1F;
        else cookAmt = Mth.floor(cookAmt * 4F) / 4F;
        result.set(ModComponents.COOK_AMOUNT, cookAmt);

        return result;
    }

    @Override
    public ItemStack getPickResult() {
        return this.getItemStack();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder entityData) {
        entityData.define(DATA_COOK_AMT, 0F);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        this.setCookAmt(input.getFloatOr("CookAmt", 0F));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
        output.putFloat("CookAmt", this.getCookAmt());
    }
}
