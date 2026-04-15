package dev.chililisoup.hotdognalds.item;

import dev.chililisoup.hotdognalds.entity.Hotdog;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class HotdogItem extends SpawnItem<Hotdog> {
    public HotdogItem(Properties properties) {
        super(properties, ModEntityTypes.HOTDOG, Hotdog::create);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        float cookAmt = itemStack.getOrDefault(ModComponents.COOK_AMOUNT, 0F);
        Component prefix;
        if (cookAmt <= 0F) prefix = Component.translatable("item.hotdognalds.hotdog.raw");
        else if (cookAmt < 1F) prefix = Component.translatable("item.hotdognalds.hotdog.rare");
        else if (cookAmt <= 2F) prefix = Component.translatable("item.hotdognalds.hotdog.cooked");
        else if (cookAmt < 3F) prefix = Component.translatable("item.hotdognalds.hotdog.burnt");
        else prefix = Component.translatable("item.hotdognalds.hotdog.congratulation");

        return Component.translatable("item.hotdognalds.hotdog", prefix);
    }
}
