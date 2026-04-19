package dev.chililisoup.hotdognalds.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.client.model.HotdogBunModel;
import dev.chililisoup.hotdognalds.client.model.HotdogModel;
import dev.chililisoup.hotdognalds.client.model.HotdogSauceModel;
import dev.chililisoup.hotdognalds.client.reg.ModEntityRenderers;
import dev.chililisoup.hotdognalds.client.reg.ModModelLayers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

@Environment(EnvType.CLIENT)
public final class BaseHotdogRenderer {
    public static final Identifier RAW_TEXTURE = Hotdognalds.id("textures/entity/hotdog_raw.png");
    public static final Identifier COOKED_TEXTURE = Hotdognalds.id("textures/entity/hotdog_cooked.png");
    public static final Identifier BURNT_TEXTURE = Hotdognalds.id("textures/entity/hotdog_burnt.png");

    final HotdogModel model;
    final HotdogBunModel bunModel;
    final HotdogSauceModel sauceModel;

    public BaseHotdogRenderer(EntityModelSet modelSet) {
        this.model = new HotdogModel(modelSet.bakeLayer(ModEntityRenderers.HOTDOG_MODEL));
        this.bunModel = new HotdogBunModel(modelSet.bakeLayer(ModModelLayers.HOTDOG_BUN));
        this.sauceModel = new HotdogSauceModel(modelSet.bakeLayer(ModModelLayers.HOTDOG_SAUCE));
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

        boolean hasDog = state.contents.cookAmt().isPresent();
        if (hasDog) this.submitBlendedCookModel(helper, this.model, state.contents.cookAmt().get());

        boolean hasBun = state.contents.bunCookAmt().isPresent();
        if (hasBun) this.submitBlendedCookModel(helper, this.bunModel, state.contents.bunCookAmt().get());

        // Submit red bun for nonsense hotdog contents
        if (!hasDog && !hasBun) helper.submitColoredTextureModel(this.bunModel, RAW_TEXTURE, 0xFFFF0000);

        int sauceAmount = state.contents.sauceAmount();
        if (sauceAmount > 0) {
            Identifier sauceTexture;
            if (sauceAmount > 2) sauceTexture = BURNT_TEXTURE;
            else if (sauceAmount > 1) sauceTexture = COOKED_TEXTURE;
            else sauceTexture = RAW_TEXTURE;
            helper.submitColoredTextureModel(this.sauceModel, sauceTexture, state.contents.sauceColor());
        }
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
        private void submitBlendedTextureModel(
                Model<HotdogRenderState> model,
                Identifier texture1,
                Identifier texture2,
                float amt
        ) {
            this.submitBaseTextureModel(model, texture1);
            this.submitColoredTextureModel(model, texture2, ARGB.color(
                    Math.clamp(Math.round(amt * 255), 0, 255), 0xFFFFFF
            ));
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

        @SuppressWarnings("DataFlowIssue")
        private void submitColoredTextureModel(Model<HotdogRenderState> model, Identifier texture, int color) {
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
    }
}
