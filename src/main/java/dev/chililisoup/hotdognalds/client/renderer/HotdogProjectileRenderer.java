package dev.chililisoup.hotdognalds.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.chililisoup.hotdognalds.entity.HotdogProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class HotdogProjectileRenderer extends EntityRenderer<HotdogProjectile, HotdogProjectileRenderState> {
    private final ItemModelResolver itemModelResolver;

    public HotdogProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void submit(
            HotdogProjectileRenderState state,
            PoseStack poseStack,
            @NotNull SubmitNodeCollector submitNodeCollector,
            @NotNull CameraRenderState camera
    ) {
        poseStack.pushPose();

        poseStack.scale(2, 2, 2);
        poseStack.translate(0F, 0.1F, 0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XN.rotationDegrees(state.xRot));

        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public void extractRenderState(@NotNull HotdogProjectile entity, @NotNull HotdogProjectileRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.GROUND, entity);
        state.xRot = entity.getXRot(partialTick);
        state.yRot = entity.getYRot(partialTick);
    }

    @Override
    public @NotNull HotdogProjectileRenderState createRenderState() {
        return new HotdogProjectileRenderState();
    }
}
