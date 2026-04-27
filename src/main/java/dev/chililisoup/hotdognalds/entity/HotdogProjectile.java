package dev.chililisoup.hotdognalds.entity;

import dev.chililisoup.hotdognalds.item.HotdogContents;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import dev.chililisoup.hotdognalds.reg.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
        if (id == 3) {
            ParticleOptions particle = this.getParticle();

            for (int i = 0; i < 8; i++) {
                this.level().addParticle(particle, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
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

        ItemStack drop = this.getItem();
        Block.popResource(serverLevel, this.blockPosition(), drop);
        serverLevel.broadcastEntityEvent(this, (byte) 3);

        this.discard();
    }
}
