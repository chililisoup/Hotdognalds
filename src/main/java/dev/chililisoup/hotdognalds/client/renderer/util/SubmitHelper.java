package dev.chililisoup.hotdognalds.client.renderer.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

@SuppressWarnings("DataFlowIssue")
public record SubmitHelper<T extends EntityRenderState>(
        T state,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        boolean hasFoil,
        int overlayCoords
) {
    public void submitBaseColoredTextureModel(Model<T> model, Identifier texture, int color) {
        this.submitNodeCollector.order(1).submitModel(
                model,
                this.state,
                this.poseStack,
                RenderTypes.entityTranslucent(texture),
                this.state.lightCoords,
                this.overlayCoords,
                color,
                null,
                this.state.outlineColor,
                null
        );
    }

    public void submitBlendedTextureModelPart(
            Model<T> model,
            Identifier texture1,
            Identifier texture2,
            float amt
    ) {
        this.submitBaseTextureModelPart(model, texture1);
        this.submitColoredTextureModelPart(model, texture2, ARGB.color(
                Math.clamp(Math.round(amt * 255), 0, 255), 0xFFFFFF
        ));
    }

    public void submitBaseTextureModelPart(Model<T> model, Identifier texture) {
        this.submitNodeCollector.submitModelPart(
                model.root(),
                poseStack,
                model.renderType(texture),
                this.state.lightCoords,
                overlayCoords,
                null,
                false,
                false,
                -1,
                null,
                this.state.outlineColor
        );
    }

    public void submitTextureModelPart(Model<T> model, Identifier texture) {
        this.submitModelPart(model, model.renderType(texture));
    }

    public void submitTranslucentTextureModelPart(Model<T> model, Identifier texture) {
        this.submitModelPart(model, RenderTypes.entityTranslucent(texture));
    }

    public void submitColoredTextureModelPart(Model<T> model, Identifier texture, int color) {
        this.submitModelPart(this.submitNodeCollector.order(1), model, RenderTypes.entityTranslucent(texture), color);
    }

    public void submitModelPart(Model<T> model, RenderType renderType) {
        this.submitModelPart(model, renderType, -1);
    }

    public void submitModelPart(Model<T> model, RenderType renderType, int color) {
        this.submitModelPart(this.submitNodeCollector, model, renderType, color);
    }

    private void submitModelPart(OrderedSubmitNodeCollector collector, Model<T> model, RenderType renderType, int color) {
        collector.submitModelPart(
                model.root(),
                poseStack,
                renderType,
                this.state.lightCoords,
                overlayCoords,
                null,
                false,
                this.hasFoil,
                color,
                null,
                this.state.outlineColor
        );
    }
}
