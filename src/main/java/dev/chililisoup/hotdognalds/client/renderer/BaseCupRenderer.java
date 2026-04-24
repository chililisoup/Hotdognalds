package dev.chililisoup.hotdognalds.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.client.model.*;
import dev.chililisoup.hotdognalds.client.reg.ModEntityRenderers;
import dev.chililisoup.hotdognalds.client.reg.ModModelLayers;
import dev.chililisoup.hotdognalds.client.renderer.util.SubmitHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class BaseCupRenderer {
    public static final Identifier TEXTURE = Hotdognalds.id("textures/entity/cup.png");

    final CupModel model;
    final CupDrinkModel drinkModel;

    public BaseCupRenderer(EntityModelSet modelSet) {
        this.model = new CupModel(modelSet.bakeLayer(ModEntityRenderers.CUP));
        this.drinkModel = new CupDrinkModel(modelSet.bakeLayer(ModModelLayers.CUP_DRINK));
    }

    public void submit(
            CupRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector
    ) {
        this.submit(state, poseStack, submitNodeCollector, OverlayTexture.NO_OVERLAY);
    }

    public void submit(
            CupRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int overlayCoords
    ) {
        SubmitHelper<CupRenderState> helper = new SubmitHelper<>(state, poseStack, submitNodeCollector, overlayCoords);

        helper.submitTranslucentTextureModel(this.model, TEXTURE);
        if (state.contents.hasDrink()) helper.submitColoredTextureModel(
                this.drinkModel, TEXTURE, state.contents.drinkColor()
        );
    }
}
