package dev.chililisoup.hotdognalds.item;

import dev.chililisoup.hotdognalds.entity.Cup;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CupItem extends SpawnItem<Cup> {
    private static final float CONSUME_SECONDS = 3F;
    private static final int CONSUME_TICKS = Math.round(CONSUME_SECONDS * 20F) + 2;
    private static final float CONSUMED_PER_TICK = 1F / (CONSUME_SECONDS * 20F);
    private static final int CONSUME_BUFFER_TICKS = 10;

    public CupItem(Properties properties) {
        super(properties, ModEntityTypes.CUP, Cup::create);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (getContents(player.getItemInHand(hand)).isEmpty())
            return super.use(level, player, hand);

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int ticksRemaining) {
        CupContents contents = getContents(stack);
        if (contents.isEmpty()) {
            super.onUseTick(level, livingEntity, stack, ticksRemaining);
            return;
        }

        if (ticksRemaining > CONSUME_TICKS) return;

        contents.withFillLevel(contents.fillLevel() - CONSUMED_PER_TICK).updateItemStack(stack);
        if (ticksRemaining % 4 == 0) livingEntity.playSound(
                SoundEvents.GENERIC_DRINK.value(),
                0.5F,
                Mth.randomBetween(livingEntity.getRandom(), 0.9F, 1.0F)
        );
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack stack) {
        return getContents(stack).hasDrink() ?
                ItemUseAnimation.DRINK :
                super.getUseAnimation(stack);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity user) {
        CupContents contents = getContents(stack);
        return contents.hasDrink() ?
                CONSUME_TICKS + CONSUME_BUFFER_TICKS :
                super.getUseDuration(stack, user);
    }

    @Override
    public boolean allowComponentsUpdateAnimation(
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull ItemStack oldStack,
            @NotNull ItemStack newStack
    ) {
        CupContents oldContents = oldStack.get(ModComponents.CUP_CONTENTS);
        if (oldContents == null) return true;
        CupContents newContents = newStack.get(ModComponents.CUP_CONTENTS);
        if (newContents == null) return true;

        if (oldContents.drinkColor() != newContents.drinkColor()) return true;

        ItemStack oldNoContents = oldStack.copy();
        ItemStack newNoContents = newStack.copy();
        oldNoContents.remove(ModComponents.CUP_CONTENTS);
        newNoContents.remove(ModComponents.CUP_CONTENTS);

        return !ItemStack.isSameItemSameComponents(oldNoContents, newNoContents);
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return getContents(stack).hasDrink();
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return Math.min(Math.round(getContents(stack).fillLevel() * 13), 13);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return ARGB.setBrightness(ARGB.opaque(getContents(stack).drinkColor()), 1F);
    }

    private static CupContents getContents(ItemStack stack) {
        return stack.getOrDefault(ModComponents.CUP_CONTENTS, CupContents.EMPTY);
    }
}
