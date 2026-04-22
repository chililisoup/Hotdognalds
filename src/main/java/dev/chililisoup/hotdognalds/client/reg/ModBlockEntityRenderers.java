package dev.chililisoup.hotdognalds.client.reg;

import dev.chililisoup.hotdognalds.client.renderer.CrateRenderer;
import dev.chililisoup.hotdognalds.reg.ModBlockEntityTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

@Environment(EnvType.CLIENT)
public final class ModBlockEntityRenderers {
    static {
        BlockEntityRenderers.register(ModBlockEntityTypes.CRATE, CrateRenderer::new);
    }

    public static void init() {}
}
