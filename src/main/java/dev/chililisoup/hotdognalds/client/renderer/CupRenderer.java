package dev.chililisoup.hotdognalds.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.chililisoup.hotdognalds.entity.Cup;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class CupRenderer extends EntityRenderer<Cup, CupRenderState> {
    private final BaseCupRenderer baseRenderer;

    public CupRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.baseRenderer = new BaseCupRenderer(context.getModelSet());
    }

    @Override
    public void submit(
            @NotNull CupRenderState state,
            @NotNull PoseStack poseStack,
            @NotNull SubmitNodeCollector submitNodeCollector,
            @NotNull CameraRenderState camera
    ) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - state.yRot));
        poseStack.scale(-1F, -1F, 1F);
        poseStack.translate(0.0F, -0.001F, 0.0F);

        this.baseRenderer.submit(state, poseStack, submitNodeCollector);

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public void extractRenderState(@NotNull Cup cup, @NotNull CupRenderState state, float partialTick) {
        super.extractRenderState(cup, state, partialTick);
        state.yRot = Mth.rotLerp(partialTick, cup.yRotO, cup.getYRot());
        state.contents = cup.getContents().withFillLevel(cup.getFillLevel(partialTick));
    }

    @Override
    public @NotNull CupRenderState createRenderState() {
        return new CupRenderState();
    }
}
