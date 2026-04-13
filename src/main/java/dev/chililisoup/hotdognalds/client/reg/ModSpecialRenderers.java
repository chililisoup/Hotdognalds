package dev.chililisoup.hotdognalds.client.reg;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.client.renderer.HotdogSpecialRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;

@Environment(EnvType.CLIENT)
public final class ModSpecialRenderers {
    public static final ModelLayerLocation HOTDOG = register("hotdog", HotdogSpecialRenderer.Unbaked.MAP_CODEC);

    private static ModelLayerLocation register(String name, MapCodec<? extends SpecialModelRenderer.Unbaked<?>> mapCodec) {
        ModelLayerLocation result = new ModelLayerLocation(Hotdognalds.id(name), "main");
        SpecialModelRenderers.ID_MAPPER.put(result.model(), mapCodec);
        return result;
    }

    public static void init() {}
}
