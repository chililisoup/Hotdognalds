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
        HotdogContents contents = itemStack.getOrDefault(ModComponents.HOTDOG_CONTENTS, HotdogContents.DOG);
        if (contents.cookAmt().isPresent()) return Component.translatable(
                "item.hotdognalds.hotdog",
                getPrefix(contents.cookAmt().get())
        );
        return Component.translatable("item.hotdognalds.hotdog.bun");
    }

    private static Component getPrefix(float cookAmt) {
        if (cookAmt <= 0F) return Component.translatable("item.hotdognalds.hotdog.raw");
        if (cookAmt < 1F) return Component.translatable("item.hotdognalds.hotdog.rare");
        if (cookAmt <= 2F) return Component.translatable("item.hotdognalds.hotdog.cooked");
        if (cookAmt < 3F) return Component.translatable("item.hotdognalds.hotdog.burnt");
        return Component.translatable("item.hotdognalds.hotdog.congratulation");
    }
}
