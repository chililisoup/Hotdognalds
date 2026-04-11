package dev.chililisoup.hotdognalds.client.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.client.model.HotdogModel;
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
    public static final ModelLayerLocation HOTDOG_MODEL = register(
            "hotdog",
            ModEntityTypes.HOTDOG,
            HotdogRenderer::new,
            HotdogModel::createBodyLayer
    );

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
