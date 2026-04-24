package dev.chililisoup.hotdognalds.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.entity.CondimentDispenser;
import dev.chililisoup.hotdognalds.entity.Cup;
import dev.chililisoup.hotdognalds.entity.Hotdog;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes {
    public static final EntityType<Hotdog> HOTDOG = register(
            "hotdog",
            EntityType.Builder.of(Hotdog::new, MobCategory.MISC)
                    .noLootTable()
                    .noSummon()
                    .sized(0.2F, 0.2F)
    );

    public static final EntityType<CondimentDispenser> CONDIMENT_DISPENSER = register(
            "condiment_dispenser",
            EntityType.Builder.of(CondimentDispenser::new, MobCategory.MISC)
                    .noLootTable()
                    .noSummon()
                    .sized(0.25F, 0.4375F)
    );

    public static final EntityType<Cup> CUP = register(
            "cup",
            EntityType.Builder.of(Cup::new, MobCategory.MISC)
                    .noLootTable()
                    .noSummon()
                    .sized(0.1875F, 0.3125F)
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> entityKey = entityKey(name);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, entityKey, builder.build(entityKey));
    }

    private static ResourceKey<EntityType<?>> entityKey(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Hotdognalds.id(name));
    }

    public static void init() {}
}
