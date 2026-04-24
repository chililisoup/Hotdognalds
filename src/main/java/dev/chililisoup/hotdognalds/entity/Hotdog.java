package dev.chililisoup.hotdognalds.entity;

import dev.chililisoup.hotdognalds.item.HotdogContents;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModEntityDataSerializers;
import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.ARGB;
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

public class Hotdog extends Entity implements CondimentCollector {
    private static final EntityDataAccessor<HotdogContents> DATA_CONTENTS = SynchedEntityData.defineId(
            Hotdog.class, ModEntityDataSerializers.HOTDOG_CONTENTS
    );

    protected final InterpolationHandler interpolation = new InterpolationHandler(this);

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
        Hotdog hotdog = ModEntityTypes.HOTDOG.create(
                serverLevel,
                consumer,
                BlockPos.containing(position),
                entitySpawnReason,
                true,
                true
        );
        if (hotdog == null) return null;

        hotdog.setContents(itemStack.getOrDefault(ModComponents.HOTDOG_CONTENTS, HotdogContents.DOG));
        hotdog.snapTo(position, rotation, 0);
        hotdog.playPlaceSound();
        hotdog.gameEvent(GameEvent.ENTITY_PLACE, player);
        return hotdog;
    }

    private void playPlaceSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 0.75F, 1F);
    }

    private void playTakeSound() {
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
            this.setDeltaMovement(deltaMovement);
            this.move(MoverType.SELF, deltaMovement);
        } else this.setDeltaMovement(Vec3.ZERO);

        this.applyEffectsFromBlocks();
    }

    public void doCookEffect() {
        if (this.getInBlockState().getBlock() instanceof BaseFireBlock) return;

        if (this.level().isClientSide()) {
            float halfWidth = this.hasBun() ? 0.10375F : 0.0725F;
            float length = this.hasBun() ? 0.375F : 0.5F;

            float xd = this.random.nextBoolean() ? -halfWidth : halfWidth;
            float zd = (this.random.nextFloat() - 0.5F) * length;

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
        boolean offHand = hand == InteractionHand.OFF_HAND;

        if (!this.hasDog()) {
            HotdogContents handContents = handStack.get(ModComponents.HOTDOG_CONTENTS);
            if (handContents != null && handContents.hasDog() && !handContents.hasBun()) {
                if (!this.isRemoved() && !player.level().isClientSide()) {
                    this.setMutable(handContents.toMutable().bunCookAmt(
                            this.getContents().bunCookAmt().orElse(0F)
                    ));
                    handStack.consume(1, player);

                    this.playPlaceSound();
                }

                return InteractionResult.SUCCESS_SERVER;
            }
        } else if (this.hasBun() && player.isShiftKeyDown()) {
            ItemStack hotdogStack = this.getMutable().takeBun().toImmutable().getRoundedItemStack();
            if ((!offHand && handStack.isEmpty()) || ItemStack.isSameItemSameComponents(handStack, hotdogStack)) {
                if (!this.isRemoved() && !player.level().isClientSide()) {
                    this.setMutable(this.getMutable().takeDog());

                    if (handStack.isEmpty()) {
                        if (offHand) player.addItem(hotdogStack);
                        else player.setItemInHand(hand, hotdogStack);
                    } else player.addItem(hotdogStack);

                    this.playTakeSound();
                }

                return InteractionResult.SUCCESS_SERVER;
            }
        }

        ItemStack hotdogStack = this.getItemStack();
        if (!handStack.isEmpty() && !ItemStack.isSameItemSameComponents(handStack, hotdogStack))
            return InteractionResult.PASS;

        if (!this.isRemoved() && player.level() instanceof ServerLevel serverLevel) {
            this.kill(serverLevel);
            this.markHurt();

            if (handStack.isEmpty()) player.setItemInHand(hand, hotdogStack);
            else player.addItem(hotdogStack);

            this.playTakeSound();
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    public HotdogContents getContents() {
        return this.getEntityData().get(DATA_CONTENTS);
    }

    public void setContents(HotdogContents contents) {
        this.getEntityData().set(DATA_CONTENTS, contents);
    }

    public HotdogContents.Mutable getMutable() {
        return this.getContents().toMutable();
    }

    public void setMutable(HotdogContents.Mutable mutable) {
        this.setContents(mutable.toImmutable());
    }

    public boolean hasDog() {
        return this.getContents().hasDog();
    }

    public boolean hasBun() {
        return this.getContents().hasBun();
    }

    public void setCookAmt(float cookAmt) {
        this.setMutable(this.getMutable().cookAmt(cookAmt));
    }

    public void setBunCookAmt(float bunCookAmt) {
        this.setMutable(this.getMutable().bunCookAmt(bunCookAmt));
    }

    @Override
    public void collectCondiment(int color) {
        HotdogContents contents = this.getContents();
        if (!contents.hasDog()) return;

        int sauceAmount = contents.sauceAmount();
        this.setMutable(contents.toMutable().sauce(
                Math.min(sauceAmount + 1, 3),
                sauceAmount > 0 ?
                        ARGB.average(color, contents.sauceColor()) :
                        color
        ));
    }

    private void placeFire(@NotNull ServerLevel level) {
        BlockPos blockPos = this.blockPosition();
        if (BaseFireBlock.canBePlacedAt(level, blockPos, Direction.DOWN)) {
            level.setBlock(blockPos, BaseFireBlock.getState(level, blockPos), 11);
            this.playSound(SoundEvents.FIRECHARGE_USE);
            level.gameEvent(this, GameEvent.BLOCK_PLACE, blockPos);
        }
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float damage) {
        if (this.isInvulnerableToBase(source)) return false;
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING) && source.getEntity() instanceof Mob)
            return false;
        if (this.isRemoved()) return true;

        if (source.is(DamageTypes.HOT_FLOOR)) {
            HotdogContents contents = this.getContents();
            if (contents.cookAmt().isPresent()) {
                float cookAmt = contents.cookAmt().get();
                if (cookAmt < 3F) {
                    this.setCookAmt(
                            cookAmt + 0.001F * damage * (contents.hasBun() ? 0.5F : 1F)
                    );
                } else if (this.random.nextFloat() > 0.99F) this.placeFire(level);
            }

            if (contents.bunCookAmt().isPresent()) {
                float bunCookAmt = contents.bunCookAmt().get();
                if (bunCookAmt < 3F) this.setBunCookAmt(bunCookAmt + 0.002F * damage);
                else if (this.random.nextFloat() > 0.98F) this.placeFire(level);
            }
        } else if (source.is(DamageTypeTags.IS_FIRE)) {
            if (this.hasDog()) this.setCookAmt(3F);
            if (this.hasBun()) this.setBunCookAmt(3F);
        } else {
            this.kill(level);
            this.markHurt();

            if (!source.isCreativePlayer()) {
                ItemStack drop = this.getItemStack();
                Block.popResource(this.level(), this.blockPosition(), drop);
            }
            this.playTakeSound();
        }

        return true;
    }

    private ItemStack getItemStack() {
        return this.getContents().getRoundedItemStack();
    }

    @Override
    protected @NotNull Component getTypeName() {
        return this.getItemStack().getHoverName();
    }

    @Override
    public ItemStack getPickResult() {
        return this.getItemStack();
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder entityData) {
        entityData.define(DATA_CONTENTS, HotdogContents.DOG);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        this.setContents(input.read("HotdogContents", HotdogContents.CODEC).orElse(HotdogContents.DOG));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
        output.store("HotdogContents", HotdogContents.CODEC, this.getContents());
    }
}
