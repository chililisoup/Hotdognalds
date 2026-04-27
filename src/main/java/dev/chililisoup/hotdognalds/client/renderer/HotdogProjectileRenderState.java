package dev.chililisoup.hotdognalds.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;

@Environment(EnvType.CLIENT)
public class HotdogProjectileRenderState extends ThrownItemRenderState {
    public float xRot;
    public float yRot;
}
