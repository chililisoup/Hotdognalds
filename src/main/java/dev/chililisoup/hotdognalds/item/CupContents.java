package dev.chililisoup.hotdognalds.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public record CupContents(float fillLevel, int drinkColor) {
    public static final CupContents EMPTY = new CupContents(0F, -1);

    public static final Codec<CupContents> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("fill_level").forGetter(CupContents::fillLevel),
            ExtraCodecs.ARGB_COLOR_CODEC.fieldOf("drink_color").forGetter(CupContents::fixedDrinkColor)
    ).apply(i, CupContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CupContents> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, CupContents::fillLevel,
            ByteBufCodecs.INT, CupContents::fixedDrinkColor,
            CupContents::new
    );

    private int fixedDrinkColor() {
        return this.hasDrink() ? this.drinkColor : -1;
    }

    public static void updateItem(ItemStack stack) {
        CupContents contents = stack.get(ModComponents.CUP_CONTENTS);
        if (contents != null) contents.applyToItem(stack);
    }

    public void applyToItem(ItemStack stack) {
        CupContents contents = new CupContents(this.fillLevel, this.fixedDrinkColor());
        stack.set(ModComponents.CUP_CONTENTS, contents);
        stack.set(DataComponents.MAX_STACK_SIZE, contents.isEmpty() ? 16 : 1);
    }

    public CupContents withFillLevel(float fillLevel) {
        return new CupContents(
                Math.clamp(fillLevel, 0F, 1F),
                fillLevel > 0 ? this.drinkColor : -1
        );
    }

    public boolean hasDrink() {
        return this.fillLevel > 0F;
    }

    public boolean isEmpty() {
        return !this.hasDrink();
    }

    public boolean isFull() {
        return this.fillLevel >= 1F;
    }
}
