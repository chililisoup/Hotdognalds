package dev.chililisoup.hotdognalds.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.entity.CondimentDispenser;
import dev.chililisoup.hotdognalds.item.HotdogContents;
import dev.chililisoup.hotdognalds.item.HotdogItem;
import dev.chililisoup.hotdognalds.item.SpawnItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public final class ModItems {
    public static Item HOTDOG;
    public static Item CONDIMENT_DISPENSER;

    static {
        HOTDOG = register(
                "hotdog",
                HotdogItem::new,
                new Properties().component(ModComponents.HOTDOG_CONTENTS, HotdogContents.DOG)
        );
        CreativeModeTabEvents.modifyOutputEvent(ModCreativeTabs.MAIN).register(tab -> {
            ItemStack cookedHotdog = new ItemStack(HOTDOG);
            cookedHotdog.set(ModComponents.HOTDOG_CONTENTS, HotdogContents.dog(1F));
            tab.accept(cookedHotdog);

            ItemStack bun = new ItemStack(HOTDOG);
            bun.set(ModComponents.HOTDOG_CONTENTS, HotdogContents.BUN);
            tab.accept(bun);
        });

        CONDIMENT_DISPENSER = register(
                "condiment_dispenser",
                properties -> new SpawnItem<>(properties, ModEntityTypes.CONDIMENT_DISPENSER, CondimentDispenser::create)
        );
    }

    private static Item register(
            String name,
            Function<Properties, Item> itemFactory,
            Properties properties
    ) {
        ResourceKey<Item> itemKey = itemKey(name);
        Item item = itemFactory.apply(properties.setId(itemKey));
        CreativeModeTabEvents.modifyOutputEvent(ModCreativeTabs.MAIN).register(tab -> tab.accept(item));
        return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    }

    private static Item register(
            String name,
            Function<Properties, Item> itemFactory
    ) {
        return register(name, itemFactory, new Properties());
    }

    private static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, Hotdognalds.id(name));
    }

    public static void init() {}
}
