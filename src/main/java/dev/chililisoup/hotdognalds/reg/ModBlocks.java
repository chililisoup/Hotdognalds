package dev.chililisoup.hotdognalds.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.block.GrillBlock;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import java.util.function.Function;

public final class ModBlocks {
    public static final Block GRILL = register(
            "grill",
            GrillBlock::new,
            Properties.ofFullCopy(Blocks.CAULDRON)
    );

    static {
        FlammableBlockRegistry.getDefaultInstance().add(GRILL, 1, 0);
    }

    private static Block register(
            String name,
            Function<Properties, Block> blockFactory,
            Properties properties
    ) {
        ResourceKey<Block> blockKey = blockKey(name);
        Block block = blockFactory.apply(properties.setId(blockKey));

        ResourceKey<Item> itemKey = itemKey(blockKey.identifier());
        Item.Properties itemProperties = new Item.Properties()
                .useBlockDescriptionPrefix()
                .requiredFeatures(block.requiredFeatures())
                .setId(itemKey);

        BlockItem blockItem = new BlockItem(block, itemProperties);
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        CreativeModeTabEvents.modifyOutputEvent(ModCreativeTabs.MAIN).register(tab -> tab.accept(blockItem));

        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    private static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(Registries.BLOCK, Hotdognalds.id(name));
    }

    private static ResourceKey<Item> itemKey(Identifier identifier) {
        return ResourceKey.create(Registries.ITEM, identifier);
    }

    public static void init() {}
}
