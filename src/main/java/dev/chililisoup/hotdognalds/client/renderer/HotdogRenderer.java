package dev.chililisoup.hotdognalds.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.client.model.HotdogModel;
import dev.chililisoup.hotdognalds.client.reg.ModEntityRenderers;
import dev.chililisoup.hotdognalds.entity.Hotdog;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class HotdogRenderer extends EntityRenderer<Hotdog, HotdogRenderState> {
    public static final Identifier RAW_TEXTURE = Hotdognalds.id("textures/entity/hotdog_raw.png");
    public static final Identifier COOKED_TEXTURE = Hotdognalds.id("textures/entity/hotdog_cooked.png");
    public static final Identifier BURNT_TEXTURE = Hotdognalds.id("textures/entity/hotdog_burnt.png");
    private final HotdogModel model;

    public HotdogRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new HotdogModel(context.bakeLayer(ModEntityRenderers.HOTDOG_MODEL));
    }

    @Override
    public void submit(
            @NotNull HotdogRenderState state,
            @NotNull PoseStack poseStack,
            @NotNull SubmitNodeCollector submitNodeCollector,
            @NotNull CameraRenderState camera
    ) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - state.yRot));
        poseStack.scale(-1F, -1F, 1F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        if (state.cookAmt < 1F)
            this.submitBlendedTextureModel(state, poseStack, submitNodeCollector, RAW_TEXTURE, COOKED_TEXTURE, state.cookAmt);
        else if (state.cookAmt <= 2F)
            this.submitBaseTextureModel(state, poseStack, submitNodeCollector, COOKED_TEXTURE);
        else if (state.cookAmt < 3F)
            this.submitBlendedTextureModel(state, poseStack, submitNodeCollector, COOKED_TEXTURE, BURNT_TEXTURE, state.cookAmt - 2F);
        else
            this.submitBaseTextureModel(state, poseStack, submitNodeCollector, BURNT_TEXTURE);

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @SuppressWarnings("DataFlowIssue")
    private void submitBlendedTextureModel(
            HotdogRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            Identifier texture1,
            Identifier texture2,
            float amt
    ) {
        this.submitBaseTextureModel(state, poseStack, submitNodeCollector, texture1);
        submitNodeCollector.order(1).submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityTranslucent(texture2),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                ARGB.color(Math.clamp(Math.round(amt * 255), 0, 255), 0xFFFFFF),
                null,
                state.outlineColor,
                null
        );
    }

    @SuppressWarnings("DataFlowIssue")
    private void submitBaseTextureModel(
            HotdogRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            Identifier texture
    ) {
        submitNodeCollector.submitModel(
                this.model,
                state,
                poseStack,
                texture,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null
        );
    }

    @Override
    public void extractRenderState(@NotNull Hotdog hotdog, @NotNull HotdogRenderState state, float partialTick) {
        super.extractRenderState(hotdog, state, partialTick);
        state.yRot = Mth.rotLerp(partialTick, hotdog.yRotO, hotdog.getYRot());
        state.cookAmt = hotdog.getCookAmt();
    }

    @Override
    public @NotNull HotdogRenderState createRenderState() {
        return new HotdogRenderState();
    }
}
