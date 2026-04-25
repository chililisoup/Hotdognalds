package dev.chililisoup.hotdognalds.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.block.entity.CrateBlockEntity;
import dev.chililisoup.hotdognalds.block.entity.SodaFountainBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public final class ModBlockEntityTypes {
    public static final BlockEntityType<CrateBlockEntity> CRATE = register(
            "crate",
            CrateBlockEntity::new,
            ModBlocks.CRATE
    );

    public static final BlockEntityType<SodaFountainBlockEntity> SODA_FOUNTAIN = register(
            "soda_fountain",
            SodaFountainBlockEntity::new,
            ModBlocks.SODA_FOUNTAIN
    );

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            BlockEntityType.BlockEntitySupplier<T> factory,
            Block... validBlocks
    ) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Hotdognalds.id(name),
                new BlockEntityType<>(factory, Set.of(validBlocks))
        );
    }

    public static void init() {}
}
