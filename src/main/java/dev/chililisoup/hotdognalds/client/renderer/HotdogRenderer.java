package dev.chililisoup.hotdognalds.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.chililisoup.hotdognalds.client.model.HotdogBunModel;
import dev.chililisoup.hotdognalds.client.model.HotdogModel;
import dev.chililisoup.hotdognalds.client.reg.ModEntityRenderers;
import dev.chililisoup.hotdognalds.client.reg.ModModelLayers;
import dev.chililisoup.hotdognalds.entity.Hotdog;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class HotdogRenderer extends EntityRenderer<Hotdog, HotdogRenderState> {
    private final BaseHotdogRenderer baseRenderer;

    public HotdogRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.baseRenderer = new BaseHotdogRenderer(
                new HotdogModel(context.bakeLayer(ModEntityRenderers.HOTDOG_MODEL)),
                new HotdogBunModel(context.getModelSet().bakeLayer(ModModelLayers.HOTDOG_BUN))
        );
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
        poseStack.translate(0.0F, state.contents.hasBun() ? -0.03225 : -0.001F, 0.0F);

        this.baseRenderer.submit(state, poseStack, submitNodeCollector);

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public void extractRenderState(@NotNull Hotdog hotdog, @NotNull HotdogRenderState state, float partialTick) {
        super.extractRenderState(hotdog, state, partialTick);
        state.yRot = Mth.rotLerp(partialTick, hotdog.yRotO, hotdog.getYRot());
        state.contents = hotdog.getContents();
    }

    @Override
    public @NotNull HotdogRenderState createRenderState() {
        return new HotdogRenderState();
    }
}
