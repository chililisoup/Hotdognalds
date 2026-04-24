package dev.chililisoup.hotdognalds.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CrateRenderState extends BlockEntityRenderState {
    public @Nullable ItemStackRenderState itemStackRenderState;
    float yRot;
    float yOffset;
    float scale;
    int zCount;
    int xCount;
}
