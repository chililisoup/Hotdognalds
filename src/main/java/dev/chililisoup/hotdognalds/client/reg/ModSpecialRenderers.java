package dev.chililisoup.hotdognalds.client.reg;

import dev.chililisoup.hotdognalds.client.renderer.HotdogSpecialRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.special.SpecialModelRenderers;

@Environment(EnvType.CLIENT)
public final class ModSpecialRenderers {
    static {
        SpecialModelRenderers.ID_MAPPER.put(
                ModEntityRenderers.HOTDOG_MODEL.model(),
                HotdogSpecialRenderer.Unbaked.MAP_CODEC
        );
    }

    public static void init() {}
}
