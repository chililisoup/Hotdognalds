package dev.chililisoup.hotdognalds.client.renderer;

import dev.chililisoup.hotdognalds.item.CupContents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

@Environment(EnvType.CLIENT)
public class CupRenderState extends EntityRenderState {
    public float yRot;
    public CupContents contents;
}
