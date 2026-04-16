package dev.chililisoup.hotdognalds.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.item.HotdogContents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class ModEntityDataSerializers {
    public static final EntityDataSerializer<HotdogContents> HOTDOG_CONTENTS = register(
            "hotdog_contents",
            EntityDataSerializer.forValueType(HotdogContents.STREAM_CODEC)
    );

    private static <T> EntityDataSerializer<T> register(String name, EntityDataSerializer<T> serializer) {
        FabricEntityDataRegistry.register(Hotdognalds.id(name), serializer);
        return serializer;
    }

    public static void init() {}
}
