package dev.chililisoup.hotdognalds.entity;

import dev.chililisoup.hotdognalds.item.HotdogContents;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import dev.chililisoup.hotdognalds.reg.ModItems;
import dev.chililisoup.hotdognalds.reg.ModParticles;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class HotdogProjectile extends ThrowableItemProjectile {
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
    protected @NotNull Item getDefaultItem() {
        return ModItems.HOTDOG;
    }

    private ParticleOptions getParticle() {
        ItemStack item = this.getItem();
        return item.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(item));
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id != EntityEvent.DEATH) return;

        ParticleOptions foodParticle = this.getParticle();
        for (int i = 0; i < 8; i++) this.level().addParticle(
                foodParticle, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0
        );

        HotdogContents contents = this.getItem().get(ModComponents.HOTDOG_CONTENTS);
        if (contents == null) return;

        int sauceAmount = contents.sauceAmount();
        if (sauceAmount <= 0) return;

        int sauceColor = contents.sauceColor();
        Vec3 dir = this.getDeltaMovement().scale(-0.1);
        ParticleOptions sauceParticle = ColorParticleOption.create(ModParticles.COLORED_FALL, sauceColor);
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

        ItemStack item = this.getItem();
        HotdogContents contents = item.get(ModComponents.HOTDOG_CONTENTS);
        if (contents != null) {
            HotdogContents.Mutable mutable = contents.toMutable().takeSauce();

            if (contents.hasDog() && contents.bunCookAmt().isPresent()) {
                mutable.takeBun();
                HotdogContents bun = HotdogContents.bun(contents.bunCookAmt().get());
                Block.popResource(serverLevel, this.blockPosition(), bun.getRoundedItemStack());
            }

            item.set(ModComponents.HOTDOG_CONTENTS, mutable.toImmutable());
        }

        Block.popResource(serverLevel, this.blockPosition(), item);
        serverLevel.broadcastEntityEvent(this, EntityEvent.DEATH);
        this.discard();
    }
}
