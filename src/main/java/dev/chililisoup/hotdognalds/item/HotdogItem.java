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
}
