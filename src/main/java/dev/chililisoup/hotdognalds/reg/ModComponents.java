package dev.chililisoup.hotdognalds.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;

import java.util.function.UnaryOperator;

public final class ModComponents {
    public static final DataComponentType<Float> COOK_AMOUNT = register(
            "cook_amount", b -> b.persistent(ExtraCodecs.floatRange(0.0F, 3.0F)).networkSynchronized(ByteBufCodecs.FLOAT)
    );

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Hotdognalds.id(name),
                builder.apply(DataComponentType.builder()).build()
        );
    }

    public static void init() {}
}
