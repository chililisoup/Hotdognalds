package dev.chililisoup.hotdognalds.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.entity.CondimentDispenser;
import dev.chililisoup.hotdognalds.item.*;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;

import java.util.function.Function;

public final class ModItems {
    public static final Item HOTDOG;
    public static final Item CONDIMENT_DISPENSER;
    public static final Item CUP;

    static {
        HOTDOG = register(
                "hotdog",
                HotdogItem::new,
                new Properties()
                        .component(ModComponents.HOTDOG_CONTENTS, HotdogContents.DOG)
                        .food(HotdogContents.DOG.getFoodProperties())
        );
        CreativeModeTabEvents.modifyOutputEvent(ModCreativeTabs.MAIN).register(tab -> {
            tab.accept(HotdogContents.dog(1F).getRoundedItemStack());
            tab.accept(HotdogContents.BUN.getRoundedItemStack());
        });

        CONDIMENT_DISPENSER = register(
                "condiment_dispenser",
                properties -> new SpawnItem<>(properties, ModEntityTypes.CONDIMENT_DISPENSER, CondimentDispenser::create)
        );
        CreativeModeTabEvents.modifyOutputEvent(ModCreativeTabs.MAIN).register(tab -> {
            ItemStack invulnerableDispenser = CONDIMENT_DISPENSER.getDefaultInstance();
            CompoundTag data = new CompoundTag();
            data.putBoolean("Invulnerable", true);
            invulnerableDispenser.set(DataComponents.ENTITY_DATA, TypedEntityData.of(
                    ModEntityTypes.CONDIMENT_DISPENSER,
                    data
            ));
            tab.accept(invulnerableDispenser);
        });

        CUP = register(
                "cup",
                CupItem::new,
                new Properties()
                        .stacksTo(16)
                        .component(ModComponents.CUP_CONTENTS, CupContents.EMPTY)
        );
    }

    private static Item register(
            String name,
            Function<Properties, Item> itemFactory,
            Properties properties
    ) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Hotdognalds.id(name));
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

    public static void init() {}
}
