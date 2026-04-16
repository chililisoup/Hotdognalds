package dev.chililisoup.hotdognalds.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.client.model.HotdogBunModel;
import dev.chililisoup.hotdognalds.client.model.HotdogModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public final class BaseHotdogRenderer {
    public static final Identifier RAW_TEXTURE = Hotdognalds.id("textures/entity/hotdog_raw.png");
    public static final Identifier COOKED_TEXTURE = Hotdognalds.id("textures/entity/hotdog_cooked.png");
    public static final Identifier BURNT_TEXTURE = Hotdognalds.id("textures/entity/hotdog_burnt.png");

    final HotdogModel model;
    final HotdogBunModel bunModel;

    public BaseHotdogRenderer(HotdogModel model, HotdogBunModel bunModel) {
        this.model = model;
        this.bunModel = bunModel;
    }

    public void submit(
            HotdogRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector
    ) {
        this.submit(state, poseStack, submitNodeCollector, OverlayTexture.NO_OVERLAY);
    }

    public void submit(
            HotdogRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int overlayCoords
    ) {
        SubmitHelper helper = new SubmitHelper(state, poseStack, submitNodeCollector, overlayCoords);

        if (state.contents.cookAmt().isPresent())
            this.submitBlendedCookModel(helper, this.model, state.contents.cookAmt().get());

        if (state.contents.bunCookAmt().isPresent())
            this.submitBlendedCookModel(helper, this.bunModel, state.contents.bunCookAmt().get());
    }

    private void submitBlendedCookModel(SubmitHelper helper, Model<HotdogRenderState> model, float cookAmt) {
        if (cookAmt < 1F)
            helper.submitBlendedTextureModel(model, RAW_TEXTURE, COOKED_TEXTURE, cookAmt);
        else if (cookAmt <= 2F)
            helper.submitBaseTextureModel(model, COOKED_TEXTURE);
        else if (cookAmt < 3F)
            helper.submitBlendedTextureModel(model, COOKED_TEXTURE, BURNT_TEXTURE, cookAmt - 2F);
        else
            helper.submitBaseTextureModel(model, BURNT_TEXTURE);
    }

    private record SubmitHelper(
            HotdogRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int overlayCoords
    ) {
        @SuppressWarnings("DataFlowIssue")
        private void submitBlendedTextureModel(
                Model<HotdogRenderState> model,
                Identifier texture1,
                Identifier texture2,
                float amt
        ) {
            submitBaseTextureModel(model, texture1);
            this.submitNodeCollector.order(1).submitModel(
                    model,
                    this.state,
                    this.poseStack,
                    RenderTypes.entityTranslucent(texture2),
                    this.state.lightCoords,
                    this.overlayCoords,
                    ARGB.color(Math.clamp(Math.round(amt * 255), 0, 255), 0xFFFFFF),
                    null,
                    this.state.outlineColor,
                    null
            );
        }

        @SuppressWarnings("DataFlowIssue")
        private void submitBaseTextureModel(Model<HotdogRenderState> model, Identifier texture) {
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
    }
}
