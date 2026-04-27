package dev.chililisoup.hotdognalds.client.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.client.model.CondimentDispenserModel;
import dev.chililisoup.hotdognalds.client.model.CupModel;
import dev.chililisoup.hotdognalds.client.model.HotdogModel;
import dev.chililisoup.hotdognalds.client.renderer.CondimentDispenserRenderer;
import dev.chililisoup.hotdognalds.client.renderer.CupRenderer;
import dev.chililisoup.hotdognalds.client.renderer.HotdogProjectileRenderer;
import dev.chililisoup.hotdognalds.client.renderer.HotdogRenderer;
import dev.chililisoup.hotdognalds.reg.ModEntityTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

@Environment(EnvType.CLIENT)
public final class ModEntityRenderers {
    public static final ModelLayerLocation HOTDOG = register(
            "hotdog",
            ModEntityTypes.HOTDOG,
            HotdogRenderer::new,
            HotdogModel::createBodyLayer
    );

    public static final ModelLayerLocation CONDIMENT_DISPENSER = register(
            "condiment_dispenser",
            ModEntityTypes.CONDIMENT_DISPENSER,
            CondimentDispenserRenderer::new,
            CondimentDispenserModel::createBodyLayer
    );

    public static final ModelLayerLocation CUP = register(
            "cup",
            ModEntityTypes.CUP,
            CupRenderer::new,
            CupModel::createBodyLayer
    );

    static {
        EntityRenderers.register(ModEntityTypes.HOTDOG_PROJECTILE, HotdogProjectileRenderer::new);
    }

    private static <T extends Entity> ModelLayerLocation register(
            String name,
            EntityType<? extends T> entityType,
            EntityRendererProvider<T> entityRendererProvider,
            ModelLayerRegistry.TexturedLayerDefinitionProvider provider
    ) {
        EntityRenderers.register(entityType, entityRendererProvider);
        ModelLayerLocation model = new ModelLayerLocation(Hotdognalds.id(name), "main");
        ModelLayerRegistry.registerModelLayer(model, provider);
        return model;
    }

    public static void init() {}
}
