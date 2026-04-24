package dev.chililisoup.hotdognalds.entity;

import dev.chililisoup.hotdognalds.item.CupContents;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModEntityDataSerializers;
import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class Cup extends Entity implements CondimentCollector {
    private static final EntityDataAccessor<CupContents> DATA_CONTENTS = SynchedEntityData.defineId(
            Cup.class, ModEntityDataSerializers.CUP_CONTENTS
    );

    protected final InterpolationHandler interpolation = new InterpolationHandler(this);

    private float lastFillLevel = 0F;
    private float fillLevel = 0F;

    public Cup(EntityType<Cup> type, Level level) {
        super(type, level);
    }

    @Nullable
    public static Cup create(
            ServerLevel serverLevel,
            Vec3 position,
            float rotation,
            EntitySpawnReason entitySpawnReason,
            ItemStack itemStack,
            @Nullable Player player
    ) {
        Consumer<Cup> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack, player);
        Cup cup = ModEntityTypes.CUP.create(
                serverLevel,
                consumer,
                BlockPos.containing(position),
                entitySpawnReason,
                true,
                true
        );
        if (cup == null) return null;

        cup.setContents(itemStack.getOrDefault(ModComponents.CUP_CONTENTS, CupContents.EMPTY));
        cup.snapTo(position, rotation, 0);
        cup.playPlaceSound();
        cup.gameEvent(GameEvent.ENTITY_PLACE, player);
        return cup;
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
        if (this.level().isClientSide()) {
            if (this.firstTick) {
                this.fillLevel = this.getContents().fillLevel();
                this.lastFillLevel = this.fillLevel;
            } else {
                this.lastFillLevel = this.fillLevel;
                this.fillLevel = Mth.lerp(0.25F, this.fillLevel, this.getContents().fillLevel());
            }
        }

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

    @Override
    protected double getDefaultGravity() {
        return 0.08;
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 location) {
        if (player.isSpectator()) return InteractionResult.SUCCESS;

        if (!this.isRemoved() && player.level() instanceof ServerLevel serverLevel) {
            this.kill(serverLevel);
            this.markHurt();

            ItemStack cupStack = this.getItemStack();
            if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).isEmpty())
                player.setItemInHand(hand, cupStack);
            else player.addItem(cupStack);

            this.playTakeSound();
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float damage) {
        if (this.isInvulnerableToBase(source)) return false;
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING) && source.getEntity() instanceof Mob)
            return false;
        if (this.isRemoved()) return true;

        this.kill(level);
        this.markHurt();

        if (!source.isCreativePlayer()) {
            ItemStack drop = this.getItemStack();
            Block.popResource(this.level(), this.blockPosition(), drop);
        }
        this.playTakeSound();

        return true;
    }

    public CupContents getContents() {
        return this.getEntityData().get(DATA_CONTENTS);
    }

    public void setContents(CupContents contents) {
        this.getEntityData().set(DATA_CONTENTS, contents);
    }

    public float getFillLevel(float partialTick) {
        return this.firstTick ?
                this.getContents().fillLevel() :
                Mth.lerp(partialTick, this.lastFillLevel, this.fillLevel);
    }

    private void mixDrink(float amount, int color) {
        CupContents contents = this.getContents();
        if (contents.isFull()) return;

        float fillLevel = Math.min(contents.fillLevel() + amount, 1F);
        int drinkColor = ARGB.srgbLerp(
                contents.fillLevel() / fillLevel,
                color,
                contents.drinkColor()
        );
        this.setContents(new CupContents(fillLevel, drinkColor));
    }

    @Override
    public void collectCondiment(int color) {
        this.mixDrink(0.25F, color);
    }

    private ItemStack getItemStack() {
        return this.getContents().getItemStack();
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
        entityData.define(DATA_CONTENTS, CupContents.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        this.setContents(input.read("CupContents", CupContents.CODEC).orElse(CupContents.EMPTY));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
        output.store("CupContents", CupContents.CODEC, this.getContents());
    }
}
