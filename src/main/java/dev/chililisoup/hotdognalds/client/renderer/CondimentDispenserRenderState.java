package dev.chililisoup.hotdognalds.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

@Environment(EnvType.CLIENT)
public class CondimentDispenserRenderState extends EntityRenderState {
    public float yRot;
    public float pumpAmt;
    public int color;
}
