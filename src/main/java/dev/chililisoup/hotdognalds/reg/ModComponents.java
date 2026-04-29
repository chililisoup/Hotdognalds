package dev.chililisoup.hotdognalds.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.item.CupContents;
import dev.chililisoup.hotdognalds.item.HotdogContents;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.UnaryOperator;

public final class ModComponents {
    public static final DataComponentType<HotdogContents> HOTDOG_CONTENTS = register(
            "hotdog_contents", b -> b.persistent(HotdogContents.CODEC).networkSynchronized(HotdogContents.STREAM_CODEC).cacheEncoding()
    );

    public static final DataComponentType<CupContents> CUP_CONTENTS = register(
            "cup_contents", b -> b.persistent(CupContents.CODEC).networkSynchronized(CupContents.STREAM_CODEC).cacheEncoding()
    );

    static {
        ItemComponentTooltipProviderRegistry.addFirst(HOTDOG_CONTENTS);
    }

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Hotdognalds.id(name),
                builder.apply(DataComponentType.builder()).build()
        );
    }

    public static void init() {}
}
