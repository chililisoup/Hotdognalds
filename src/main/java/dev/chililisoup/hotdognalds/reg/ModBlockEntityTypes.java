package dev.chililisoup.hotdognalds.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.block.entity.CrateBlockEntity;
import dev.chililisoup.hotdognalds.block.entity.SodaFountainBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntityTypes {
    public static final BlockEntityType<CrateBlockEntity> CRATE = register(
            "crate",
            CrateBlockEntity::new,
            ModBlocks.CRATE,
            ModBlocks.CREATIVE_CRATE
    );

    public static final BlockEntityType<SodaFountainBlockEntity> SODA_FOUNTAIN = register(
            "soda_fountain",
            SodaFountainBlockEntity::new,
            ModBlocks.SODA_FOUNTAIN
    );

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<T> factory,
            Block... blocks
    ) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Hotdognalds.id(name),
                FabricBlockEntityTypeBuilder.create(factory, blocks).build()
        );
    }

    public static void init() {}
}
