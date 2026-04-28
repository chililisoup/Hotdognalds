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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class HotdogItem extends SpawnItem<Hotdog> implements ProjectileItem {
    public HotdogItem(Properties properties) {
        super(properties, ModEntityTypes.HOTDOG, Hotdog::create);
    }

    @Override
    protected boolean requireSneakToPlace(ItemStack itemStack) {
        return true;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        HotdogContents contents = itemStack.getOrDefault(ModComponents.HOTDOG_CONTENTS, HotdogContents.DOG);
        if (contents.cookAmt().isPresent()) return Component.translatable(
                "item.hotdognalds.hotdog",
                getDogPrefix(contents.cookAmt().get())
        );

        float bunCookAmt = contents.bunCookAmt().orElse(0F);
        return bunCookAmt > 0 ?
                Component.translatable("item.hotdognalds.hotdog.bun.tip", getBunPrefix(bunCookAmt)) :
                Component.translatable("item.hotdognalds.hotdog.bun");
    }

    private static Component getDogPrefix(float cookAmt) {
        if (cookAmt <= 0F) return Component.translatable("item.hotdognalds.hotdog.raw");
        if (cookAmt < 1F) return Component.translatable("item.hotdognalds.hotdog.uncooked");
        if (cookAmt <= 2F) return Component.translatable("item.hotdognalds.hotdog.cooked");
        if (cookAmt < 3F) return Component.translatable("item.hotdognalds.hotdog.well_done");
        return Component.translatable("item.hotdognalds.hotdog.congratulation");
    }

    private static Component getBunPrefix(float bunCookAmt) {
        if (bunCookAmt < 1F) return Component.translatable("item.hotdognalds.hotdog.bun.tip.dry");
        if (bunCookAmt <= 2F) return Component.translatable("item.hotdognalds.hotdog.bun.tip.toasted");
        if (bunCookAmt < 3F) return Component.translatable("item.hotdognalds.hotdog.bun.tip.burnt");
        return Component.translatable("item.hotdognalds.hotdog.bun.tip.blackened");
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
            Projectile.spawnProjectileFromRotation(HotdogProjectile::create, serverLevel, itemStack, player, 0.0F, 1.5F, 1.0F);
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
