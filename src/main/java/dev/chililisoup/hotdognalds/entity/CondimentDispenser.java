package dev.chililisoup.hotdognalds.entity;

import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import dev.chililisoup.hotdognalds.reg.ModItems;
import dev.chililisoup.hotdognalds.reg.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
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
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CondimentDispenser extends Entity {
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(CondimentDispenser.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_PUMPING = SynchedEntityData.defineId(CondimentDispenser.class, EntityDataSerializers.BOOLEAN);
    private static final int DEFAULT_COLOR = 0xFFFCBA03;

    protected final InterpolationHandler interpolation = new InterpolationHandler(this);

    private float lastPumpAmt = 0F;
    private float pumpAmt = 0F;
    private int pumpingTicks = 0;

    public CondimentDispenser(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Nullable
    public static CondimentDispenser create(
            ServerLevel serverLevel,
            Vec3 position,
            float rotation,
            EntitySpawnReason entitySpawnReason,
            ItemStack itemStack,
            @Nullable Player player
    ) {
        Consumer<CondimentDispenser> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack, player);
        CondimentDispenser dispenser = ModEntityTypes.CONDIMENT_DISPENSER.create(
                serverLevel,
                consumer,
                BlockPos.containing(position),
                entitySpawnReason,
                true,
                true
        );
        if (dispenser == null) return null;

        dispenser.setColor(itemStack.get(DataComponents.DYED_COLOR));
        dispenser.snapTo(position, rotation, 0);
        dispenser.playSound(SoundEvents.PAINTING_PLACE, 0.75F, 1F);
        dispenser.gameEvent(GameEvent.ENTITY_PLACE, player);
        return dispenser;
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

        if (this.level().isClientSide()) {
            this.lastPumpAmt = this.pumpAmt;
            this.pumpAmt = Mth.lerp(0.25F, this.pumpAmt, this.isPumping() ? 1F : 0F);

            if (this.isPumping()) {
                Vec3 nozzlePos = this.nozzleFloorPos()
                        .add(0, 0.625 - this.pumpAmt * 0.25, 0);
                int color = this.getColor();
                this.level().addParticle(
                        ModParticles.COLORED_FALL,
                        nozzlePos.x,
                        nozzlePos.y,
                        nozzlePos.z,
                        ARGB.redFloat(color),
                        ARGB.greenFloat(color),
                        ARGB.blueFloat(color)
                );
            }
        } else if (this.pumpingTicks > 0 && --this.pumpingTicks == 0) {
            this.level().getEntitiesOfClass(Hotdog.class, AABB.ofSize(
                    this.nozzleFloorPos().add(0, 0.125, 0),
                    0.25,
                    0.25,
                    0.25
            )).forEach(hotdog -> hotdog.addSauce(this.getColor()));
            this.setPumping(false);
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.08;
    }

    private Vec3 nozzleFloorPos() {
        return this.position().add(this.getLookAngle().scale(0.25));
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 location) {
        if (player.isSpectator()) return InteractionResult.SUCCESS;

        if (!this.isRemoved() && !player.level().isClientSide() && this.pumpingTicks <= 0) {
            this.setPumping(true);
            this.pumpingTicks = 10;
            this.playSound(SoundEvents.HONEY_BLOCK_HIT, 0.75F, 1F);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    public int getColor() {
        return this.getEntityData().get(DATA_COLOR);
    }

    private void setColor(@Nullable DyedItemColor color) {
        if (color == null) return;
        this.getEntityData().set(DATA_COLOR, color.rgb());
    }

    public boolean isPumping() {
        return this.getEntityData().get(DATA_PUMPING);
    }

    private void setPumping(boolean pumping) {
        this.getEntityData().set(DATA_PUMPING, pumping);
    }

    public float getPumpAmt(float partialTick) {
        return Mth.lerp(partialTick, this.lastPumpAmt, this.pumpAmt);
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float damage) {
        if (this.isInvulnerableToBase(source)) return false;
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING) && source.getEntity() instanceof Mob)
            return false;
        if (this.isRemoved()) return true;

        this.kill(level);
        this.markHurt();

        ItemStack drop = this.getItemStack();
        Block.popResource(this.level(), this.blockPosition(), drop);
        this.playSound(SoundEvents.PAINTING_BREAK, 0.75F, 1F);

        return true;
    }

    private ItemStack getItemStack() {
        ItemStack stack = new ItemStack(ModItems.CONDIMENT_DISPENSER);

        int color = this.getColor();
        if (color == DEFAULT_COLOR) return stack;

        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        return stack;
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
        entityData.define(DATA_COLOR, DEFAULT_COLOR);
        entityData.define(DATA_PUMPING, false);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        this.getEntityData().set(DATA_COLOR, input.getIntOr("Color", DEFAULT_COLOR));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
        output.putInt("Color", this.getColor());
    }
}
