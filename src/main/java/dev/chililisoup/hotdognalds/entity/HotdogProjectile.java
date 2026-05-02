package dev.chililisoup.hotdognalds.entity;

import com.mojang.datafixers.util.Pair;
import dev.chililisoup.hotdognalds.item.HotdogContents;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import dev.chililisoup.hotdognalds.reg.ModItems;
import dev.chililisoup.hotdognalds.reg.ModParticles;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HotdogProjectile extends ThrowableItemProjectile {
    private boolean pickup = true;

    public HotdogProjectile(EntityType<HotdogProjectile> type, Level level) {
        super(type, level);
    }

    public static HotdogProjectile create(Level level, ItemStack itemStack) {
        HotdogProjectile projectile = new HotdogProjectile(ModEntityTypes.HOTDOG_PROJECTILE, level);
        projectile.setItem(itemStack);
        return projectile;
    }

    public static HotdogProjectile create(Level level, double x, double y, double z, ItemStack itemStack) {
        HotdogProjectile projectile = create(level, itemStack);
        projectile.setPos(x, y, z);
        return projectile;
    }

    public static HotdogProjectile create(Level level, LivingEntity owner, ItemStack itemStack) {
        HotdogProjectile projectile = create(level, owner.getX(), owner.getEyeY() - 0.1F, owner.getZ(), itemStack);
        projectile.setOwner(owner);
        return projectile;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved() || !this.level().isClientSide()) return;

        Pair<ParticleOptions, Integer> sauce = this.getSauce();
        if (sauce == null) return;

        if (sauce.getSecond() > this.random.nextInt(6)) {
            Vec3 dir = this.getDeltaMovement().scale(0.5);
            this.level().addParticle(
                    sauce.getFirst(),
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    dir.x + 0.1F * (this.random.nextFloat() - 0.5),
                    dir.y + 0.1F * (this.random.nextFloat() - 0.5),
                    dir.z + 0.1F * (this.random.nextFloat() - 0.5)
            );
        }
    }

    public void setPickup(boolean pickup) {
        this.pickup = pickup;
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ModItems.HOTDOG;
    }

    private ParticleOptions getParticle() {
        ItemStack item = this.getItem();
        return item.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(item));
    }

    private @Nullable Pair<ParticleOptions, Integer> getSauce() {
        HotdogContents contents = this.getItem().get(ModComponents.HOTDOG_CONTENTS);
        if (contents == null) return null;

        int sauceAmount = contents.sauceAmount();
        return sauceAmount > 0 ? Pair.of(
                ColorParticleOption.create(ModParticles.COLORED_FALL, contents.sauceColor()),
                sauceAmount
        ) : null;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id != EntityEvent.DEATH) return;

        ParticleOptions foodParticle = this.getParticle();
        for (int i = 0; i < 8; i++) this.level().addParticle(
                foodParticle, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0
        );

        Pair<ParticleOptions, Integer> sauce = this.getSauce();
        if (sauce == null) return;

        ParticleOptions sauceParticle = sauce.getFirst();
        int sauceAmount = sauce.getSecond();
        Vec3 dir = this.getDeltaMovement().scale(-0.1);
        for (int i = 0; i < 5 * sauceAmount; i++) this.level().addParticle(
                sauceParticle,
                this.getX(),
                this.getY(),
                this.getZ(),
                dir.x + 0.1F * (this.random.nextFloat() - 0.5),
                dir.y + 0.1F * (this.random.nextFloat() - 0.5),
                dir.z + 0.1F * (this.random.nextFloat() - 0.5)
        );
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        HotdogContents contents = this.getItem().get(ModComponents.HOTDOG_CONTENTS);
        float damage = contents != null ?
                contents.cookAmt().orElse(0F) + contents.bunCookAmt().orElse(0F) :
                0F;

        hitResult.getEntity().hurtServer(serverLevel, this.damageSources().thrown(this, this.getOwner()), damage);
    }

    @Override
    protected void onHit(@NotNull HitResult hitResult) {
        super.onHit(hitResult);
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        serverLevel.broadcastEntityEvent(this, EntityEvent.DEATH);
        this.discard();
        if (!this.pickup) return;

        Direction face = Direction.getApproximateNearest(this.getDeltaMovement().reverse());
        ItemStack item = this.getItem();
        HotdogContents contents = item.get(ModComponents.HOTDOG_CONTENTS);
        if (contents != null) {
            HotdogContents.Mutable mutable = contents.toMutable().takeSauce();

            if (contents.hasDog() && contents.bunCookAmt().isPresent()) {
                mutable.takeBun();

                ItemStack bunStack = item.copy();
                bunStack.set(ModComponents.HOTDOG_CONTENTS, contents.toMutable().takeDog().toImmutable());
                Block.popResourceFromFace(serverLevel, this.blockPosition(), face, bunStack);
            }

            item.set(ModComponents.HOTDOG_CONTENTS, mutable.toImmutable());
        }

        Block.popResourceFromFace(serverLevel, this.blockPosition(), face, item);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("pickup", this.pickup);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.pickup = input.getBooleanOr("pickup", false);
    }
}
