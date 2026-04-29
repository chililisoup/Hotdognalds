package dev.chililisoup.hotdognalds.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModItems;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public record HotdogContents(Optional<Float> cookAmt, Optional<Float> bunCookAmt, Optional<Integer> sauce) implements TooltipProvider {
    public static final HotdogContents DOG = dog();
    public static final HotdogContents BUN = bun();

    public static final Codec<HotdogContents> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExtraCodecs.floatRange(0.0F, 3.0F).optionalFieldOf("cook_amount").forGetter(HotdogContents::cookAmt),
            ExtraCodecs.floatRange(0.0F, 3.0F).optionalFieldOf("bun_cook_amount").forGetter(HotdogContents::bunCookAmt),
            ExtraCodecs.ARGB_COLOR_CODEC.optionalFieldOf("sauce").forGetter(HotdogContents::sauce)
    ).apply(i, HotdogContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HotdogContents> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional), HotdogContents::cookAmt,
            ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional), HotdogContents::bunCookAmt,
            ByteBufCodecs.INT.apply(ByteBufCodecs::optional), HotdogContents::sauce,
            HotdogContents::new
    );

    public static HotdogContents dog(float cookAmt) {
        return new HotdogContents(Optional.of(cookAmt), Optional.empty(), Optional.empty());
    }

    public static HotdogContents dog() {
        return dog(0F);
    }

    public static HotdogContents bun(float bunCookAmt) {
        return new HotdogContents(Optional.empty(), Optional.of(bunCookAmt), Optional.empty());
    }

    public static HotdogContents bun() {
        return bun(0F);
    }

    public boolean hasDog() {
        return this.cookAmt.isPresent();
    }

    public boolean hasBun() {
        return this.bunCookAmt.isPresent();
    }

    public int sauceAmount() {
        return this.sauce.map(ARGB::alpha).orElse(0);
    }

    public int sauceColor() {
        return ARGB.opaque(this.sauce.orElse(0));
    }

    public ItemStack getRoundedItemStack() {
        Mutable mutable = this.toMutable();
        if (this.hasDog()) mutable.cookAmt(roundCookAmt(mutable.cookAmt));
        if (this.hasBun()) mutable.bunCookAmt(roundCookAmt(mutable.bunCookAmt));
        return mutable.toImmutable().getItemStack();
    }

    private ItemStack getItemStack() {
        ItemStack result = new ItemStack(ModItems.HOTDOG);
        result.set(ModComponents.HOTDOG_CONTENTS, this);
        result.set(DataComponents.FOOD, this.getFoodProperties());
        return result;
    }

    public FoodProperties getFoodProperties() {
        int nutrition = 0;
        float saturation = 1F;

        if (this.cookAmt.isPresent()) {
            float cA = this.cookAmt.get();
            float cASq = cA * cA;
            // (0,2) (1,6) (2,6) (3,1)
            float value = 2F - (cASq * cA / 6F) - (1.5F * cASq) + (17F * cA / 3F);
            nutrition += Math.round(value);
            saturation *= value / 8F + 0.05F;
        }

        if (this.bunCookAmt.isPresent()) {
            float value = 1F - this.bunCookAmt.get() / 4F;
            nutrition += Math.round(value * 4F);
            saturation *= (value + 0.5F) / 1.5F;
        }

        return new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .build();
    }

    private static float roundCookAmt(float cookAmt) {
        return cookAmt >= 1F && cookAmt <= 2F ?
                1F : Mth.floor(cookAmt * 4F) / 4F;
    }

    public static Component getDogName(float cookAmt) {
        return Component.translatable(
                "item.hotdognalds.hotdog",
                getDogPrefix(cookAmt)
        );
    }

    public static Component getBunName(float bunCookAmt) {
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
    public void addToTooltip(
            @NotNull Item.TooltipContext context,
            @NotNull Consumer<Component> consumer,
            @NotNull TooltipFlag flag,
            @NotNull DataComponentGetter components
    ) {
        if (this.cookAmt.isPresent() && this.bunCookAmt.isPresent())
            consumer.accept(getBunName(this.bunCookAmt.get()));
    }

    public Mutable toMutable() {
        return new Mutable(
                this.cookAmt.orElse(-1F),
                this.bunCookAmt.orElse(-1F),
                this.sauce.orElse(0)
        );
    }

    public static class Mutable {
        private float cookAmt;
        private float bunCookAmt;
        private int sauce;

        private Mutable(float cookAmt, float bunCookAmt, int sauce) {
            this.cookAmt = cookAmt;
            this.bunCookAmt = bunCookAmt;
            this.sauce = sauce;
        }

        public Mutable cookAmt(float cookAmt) {
            this.cookAmt = cookAmt;
            return this;
        }

        public Mutable bunCookAmt(float bunCookAmt) {
            this.bunCookAmt = bunCookAmt;
            return this;
        }

        public Mutable giveDog() {
            this.cookAmt = Math.max(this.cookAmt, 0F);
            return this;
        }

        public Mutable giveBun() {
            this.bunCookAmt = Math.max(this.bunCookAmt, 0F);
            return this;
        }

        public Mutable takeDog() {
            this.cookAmt = -1F;
            return this.takeSauce();
        }

        public Mutable takeBun() {
            this.bunCookAmt = -1F;
            return this;
        }

        public Mutable sauce(int sauceAmount, int sauceColor) {
            this.sauce = ARGB.color(sauceAmount, sauceColor);
            return this;
        }

        public Mutable takeSauce() {
            this.sauce = 0;
            return this;
        }

        public HotdogContents toImmutable() {
            return new HotdogContents(
                    this.cookAmt < 0 ?
                            Optional.empty() :
                            Optional.of(Math.clamp(this.cookAmt, 0F, 3F)),
                    this.bunCookAmt < 0 ?
                            Optional.empty() :
                            Optional.of(Math.clamp(this.bunCookAmt, 0F, 3F)),
                    ARGB.alpha(this.sauce) == 0 ?
                            Optional.empty() :
                            Optional.of(this.sauce)
            );
        }
    }
}
