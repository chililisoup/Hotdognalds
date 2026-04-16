package dev.chililisoup.hotdognalds.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import dev.chililisoup.hotdognalds.reg.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record HotdogContents(Optional<Float> cookAmt, Optional<Float> bunCookAmt) {
    public static final HotdogContents DOG = dog();
    public static final HotdogContents BUN = bun();

    public static final Codec<HotdogContents> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExtraCodecs.floatRange(0.0F, 3.0F).optionalFieldOf("cook_amount").forGetter(HotdogContents::cookAmt),
            ExtraCodecs.floatRange(0.0F, 3.0F).optionalFieldOf("bun_cook_amount").forGetter(HotdogContents::bunCookAmt)
    ).apply(i, HotdogContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HotdogContents> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional), HotdogContents::cookAmt,
            ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional), HotdogContents::bunCookAmt,
            HotdogContents::new
    );

    public HotdogContents(float cookAmt, float bunCookAmt) {
        this(Optional.of(cookAmt), Optional.of(bunCookAmt));
    }

    public static HotdogContents dog(float cookAmt) {
        return new HotdogContents(Optional.of(cookAmt), Optional.empty());
    }

    public static HotdogContents dog() {
        return dog(0F);
    }

    public static HotdogContents bun(float bunCookAmt) {
        return new HotdogContents(Optional.empty(), Optional.of(bunCookAmt));
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

    public ItemStack getRoundedItemStack() {
        ItemStack result = new ItemStack(ModItems.HOTDOG);

        Mutable mutable = this.toMutable();
        if (this.hasDog()) mutable.cookAmt(roundCookAmt(mutable.cookAmt));
        if (this.hasBun()) mutable.bunCookAmt(roundCookAmt(mutable.bunCookAmt));

        result.set(ModComponents.HOTDOG_CONTENTS, mutable.toImmutable());
        return result;
    }

    private static float roundCookAmt(float cookAmt) {
        return cookAmt >= 1F && cookAmt <= 2F ?
                1F : Mth.floor(cookAmt * 4F) / 4F;
    }

    public Mutable toMutable() {
        return new Mutable(
                this.cookAmt.orElse(-1F),
                this.bunCookAmt.orElse(-1F)
        );
    }

    public static class Mutable {
        private float cookAmt;
        private float bunCookAmt;

        private Mutable(float cookAmt, float bunCookAmt) {
            this.cookAmt = cookAmt;
            this.bunCookAmt = bunCookAmt;
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
            return this;
        }

        public Mutable takeBun() {
            this.bunCookAmt = -1F;
            return this;
        }

        public HotdogContents toImmutable() {
            return new HotdogContents(
                    this.cookAmt < 0 ?
                            Optional.empty() :
                            Optional.of(Math.clamp(this.cookAmt, 0F, 3F)),
                    this.bunCookAmt < 0 ?
                            Optional.empty() :
                            Optional.of(Math.clamp(this.bunCookAmt, 0F, 3F))
            );
        }
    }
}
