package dev.chililisoup.hotdognalds.client.renderer.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

@SuppressWarnings("DataFlowIssue")
public record SubmitHelper<T extends EntityRenderState>(
        T state,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int overlayCoords
) {
    public void submitBlendedTextureModel(
            Model<T> model,
            Identifier texture1,
            Identifier texture2,
            float amt
    ) {
        this.submitBaseTextureModel(model, texture1);
        this.submitColoredTextureModel(model, texture2, ARGB.color(
                Math.clamp(Math.round(amt * 255), 0, 255), 0xFFFFFF
        ));
    }

    public void submitBaseTextureModel(Model<T> model, Identifier texture) {
        this.submitNodeCollector.submitModel(
                model,
                this.state,
                this.poseStack,
                texture,
                this.state.lightCoords,
                this.overlayCoords,
                this.state.outlineColor,
                null
        );
    }

    public void submitColoredTextureModel(Model<T> model, Identifier texture, int color) {
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

    public void submitTranslucentTextureModel(Model<T> model, Identifier texture) {
        this.submitNodeCollector.submitModel(
                model,
                this.state,
                this.poseStack,
                RenderTypes.entityTranslucent(texture),
                this.state.lightCoords,
                this.overlayCoords,
                this.state.outlineColor,
                null
        );
    }
}
