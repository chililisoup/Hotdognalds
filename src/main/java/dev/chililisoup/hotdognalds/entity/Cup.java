package dev.chililisoup.hotdognalds.entity;

import dev.chililisoup.hotdognalds.item.CupContents;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModEntityDataSerializers;
import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Cup extends FoodEntity implements CondimentCollector {
    private static final EntityDataAccessor<CupContents> DATA_CONTENTS = SynchedEntityData.defineId(
            Cup.class, ModEntityDataSerializers.CUP_CONTENTS
    );

    private float lastFillLevel = 0F;
    private float fillLevel = 0F;

    public Cup(EntityType<Cup> type, Level level) {
        super(type, level);
    }

    public static @Nullable Cup create(
            ServerLevel serverLevel,
            Vec3 position,
            float rotation,
            EntitySpawnReason entitySpawnReason,
            ItemStack itemStack,
            @Nullable Player player
    ) {
        Cup cup = create(ModEntityTypes.CUP, serverLevel, position, rotation, entitySpawnReason, itemStack, player);
        if (cup == null) return null;

        cup.setContents(itemStack.getOrDefault(ModComponents.CUP_CONTENTS, CupContents.EMPTY));
        return cup;
    }

    @Override
    protected void doClientCookEffect() {
        CupContents contents = this.getContents();
        this.level().addParticle(new DustParticleOptions(
                contents.hasDrink() ? contents.drinkColor() : 0, 0.5F
        ), this.getX(), this.getEyeY(), this.getZ(), 0.0, 1.0, 0.0);
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

        if (source.is(DamageTypes.HOT_FLOOR)) {
            CupContents contents = this.getContents();
            float fillLevel = contents.fillLevel();
            if (fillLevel > 0) this.setContents(contents.withFillLevel(fillLevel - 0.005F * damage));
            else if (this.random.nextFloat() > 0.98F) this.placeFire(level);
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

    public void mixDrink(float amount, int color) {
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

    @Override
    protected ItemStack getItemStack() {
        return this.updateItemStack(this.getContents().getItemStack());
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
