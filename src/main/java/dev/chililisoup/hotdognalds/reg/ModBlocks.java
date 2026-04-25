package dev.chililisoup.hotdognalds.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.block.CounterBlock;
import dev.chililisoup.hotdognalds.block.CrateBlock;
import dev.chililisoup.hotdognalds.block.GrillBlock;
import dev.chililisoup.hotdognalds.block.SodaFountainBlock;
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
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

public final class ModBlocks {
    public static final Block GRILL = register(
            "grill",
            GrillBlock::new,
            Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)
    );

    public static final Block CRATE = register(
            "crate",
            CrateBlock::new,
            Properties.of()
                    .mapColor(MapColor.STONE)
                    .noOcclusion()
                    .strength(0.3F)
                    .isValidSpawn(Blocks::never)
                    .isRedstoneConductor(Blocks::never)
                    .isSuffocating(Blocks::never)
                    .isViewBlocking(Blocks::never)
    );

    public static final Block SODA_FOUNTAIN = register(
            "soda_fountain",
            SodaFountainBlock::new,
            Properties.ofFullCopy(GRILL)
    );

    public static final Block COUNTER = register(
            "counter",
            CounterBlock::new,
            Properties.ofFullCopy(GRILL)
    );

    static {
        FlammableBlockRegistry.getDefaultInstance().add(GRILL, 1, 0);
    }

    private static Block register(
            String name,
            Function<Properties, Block> blockFactory,
            Properties properties
    ) {
        Identifier id = Hotdognalds.id(name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        Block block = blockFactory.apply(properties.setId(blockKey));

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Item.Properties itemProperties = new Item.Properties()
                .useBlockDescriptionPrefix()
                .requiredFeatures(block.requiredFeatures())
                .setId(itemKey);

        BlockItem blockItem = new BlockItem(block, itemProperties);
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        CreativeModeTabEvents.modifyOutputEvent(ModCreativeTabs.MAIN).register(tab -> tab.accept(blockItem));

        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    public static void init() {}
}
