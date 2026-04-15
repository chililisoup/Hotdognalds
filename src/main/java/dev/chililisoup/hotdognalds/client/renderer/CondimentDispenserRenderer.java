package dev.chililisoup.hotdognalds.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.chililisoup.hotdognalds.Hotdognalds;
import dev.chililisoup.hotdognalds.client.model.CondimentDispenserModel;
import dev.chililisoup.hotdognalds.client.reg.ModEntityRenderers;
import dev.chililisoup.hotdognalds.entity.CondimentDispenser;
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
public class CondimentDispenserRenderer extends EntityRenderer<CondimentDispenser, CondimentDispenserRenderState> {
    private static final Identifier TEXTURE_LOCATION = Hotdognalds.id("textures/entity/condiment_dispenser.png");
    private static final Identifier OVERLAY_LOCATION = Hotdognalds.id("textures/entity/condiment_dispenser_overlay.png");

    private final CondimentDispenserModel model;

    public CondimentDispenserRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CondimentDispenserModel(context.bakeLayer(ModEntityRenderers.CONDIMENT_DISPENSER_MODEL));
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void submit(
            @NotNull CondimentDispenserRenderState state,
            @NotNull PoseStack poseStack,
            @NotNull SubmitNodeCollector submitNodeCollector,
            @NotNull CameraRenderState camera
    ) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(270 - state.yRot));
        poseStack.scale(-1F, -1F, 1F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        submitNodeCollector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityCutoutCull(TEXTURE_LOCATION),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.color,
                null,
                state.outlineColor,
                null
        );
        submitNodeCollector.order(1).submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityCutoutCull(OVERLAY_LOCATION),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null
        );
        
        poseStack.popPose();
    }

    @Override
    public void extractRenderState(@NotNull CondimentDispenser dispenser, @NotNull CondimentDispenserRenderState state, float partialTick) {
        super.extractRenderState(dispenser, state, partialTick);
        state.yRot = Mth.rotLerp(partialTick, dispenser.yRotO, dispenser.getYRot());
        state.pumpAmt = dispenser.getPumpAmt(partialTick);
        state.color = dispenser.getColor();
    }

    @Override
    public @NotNull CondimentDispenserRenderState createRenderState() {
        return new CondimentDispenserRenderState();
    }
}
