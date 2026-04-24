package dev.chililisoup.hotdognalds.client.reg;

import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.client.model.CupDrinkModel;
import dev.chililisoup.hotdognalds.client.model.HotdogBunModel;
import dev.chililisoup.hotdognalds.client.model.HotdogSauceModel;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

public final class ModModelLayers {
    public static final ModelLayerLocation HOTDOG_BUN = register(
            "hotdog",
            "bun",
            HotdogBunModel::createBodyLayer
    );

    public static final ModelLayerLocation HOTDOG_SAUCE = register(
            "hotdog",
            "sauce",
            HotdogSauceModel::createBodyLayer
    );

    public static final ModelLayerLocation CUP_DRINK = register(
            "cup",
            "drink",
            CupDrinkModel::createBodyLayer
    );

    private static ModelLayerLocation register(
            String name,
            String layer,
            ModelLayerRegistry.TexturedLayerDefinitionProvider provider
    ) {
        ModelLayerLocation model = new ModelLayerLocation(Hotdognalds.id(name), layer);
        ModelLayerRegistry.registerModelLayer(model, provider);
        return model;
    }

    public static void init() {}
}
