package dev.chililisoup.hotdognalds.item;

import dev.chililisoup.hotdognalds.entity.Hotdog;
import dev.chililisoup.hotdognalds.entity.HotdogProjectile;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class HotdogItem extends FoodEntityItem<Hotdog> implements ProjectileItem {
    public HotdogItem(Properties properties) {
        super(properties, ModEntityTypes.HOTDOG);
    }

    @Override
    protected boolean requireSneakToPlace(ItemStack itemStack) {
        return true;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        HotdogContents contents = itemStack.getOrDefault(ModComponents.HOTDOG_CONTENTS, HotdogContents.DOG);
        return contents.cookAmt().isPresent() ?
                HotdogContents.getDogName(contents.cookAmt().get()) :
                HotdogContents.getBunName(contents.bunCookAmt().orElse(0F));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (!player.isShiftKeyDown()) return super.use(level, player, hand);

        ItemStack itemStack = player.getItemInHand(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        if (level instanceof ServerLevel serverLevel) {
            int numProjectiles = EnchantmentHelper.processProjectileCount(serverLevel, itemStack, player, 1);
            float maxAngle = EnchantmentHelper.processProjectileSpread(serverLevel, itemStack, player, 0.0F);
            float angleStep = numProjectiles == 1 ? 0.0F : 2.0F * maxAngle / (numProjectiles - 1);
            float angleOffset = (numProjectiles - 1) % 2 * angleStep / 2.0F;
            float direction = 1.0F;
            boolean forceNoPickup = player.hasInfiniteMaterials();

            for (int i = 0; i < numProjectiles; i++) {
                int idk = (i + 1) / 2;
                float angle = angleOffset + direction * idk * angleStep;
                direction = -direction;

                HotdogProjectile projectile = Projectile.spawnProjectile(
                        HotdogProjectile.create(serverLevel, player, itemStack),
                        serverLevel,
                        itemStack,
                        projectileEntity -> projectileEntity.shootFromRotation(
                                player, player.getXRot(), player.getYRot() + angle, 0.0F, 1.5F, 1.0F
                        )
                );

                if (forceNoPickup || i > 0) projectile.setPickup(false);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        itemStack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull Projectile asProjectile(
            @NotNull Level level,
            @NotNull Position pos,
            @NotNull ItemStack itemStack,
            @NotNull Direction direction
    ) {
        return HotdogProjectile.create(level, pos.x(), pos.y(), pos.z(), itemStack);
    }
}
