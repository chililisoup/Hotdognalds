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
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class HotdogRenderer extends EntityRenderer<Hotdog, HotdogRenderState> {
    private static final Identifier TEXTURE = Hotdognalds.id("textures/entity/hotdog.png");
    protected final HotdogModel model;

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
        poseStack.translate(0.0F, -1.501F, 0.0F);

        //noinspection DataFlowIssue
        submitNodeCollector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityCutout(TEXTURE),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null,
                state.outlineColor,
                null
        );

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public void extractRenderState(@NotNull Hotdog hotdog, @NotNull HotdogRenderState state, float partialTick) {
        super.extractRenderState(hotdog, state, partialTick);
        state.yRot = Mth.rotLerp(partialTick, hotdog.yRotO, hotdog.getYRot());
    }

    @Override
    public @NotNull HotdogRenderState createRenderState() {
        return new HotdogRenderState();
    }
}
