package dev.chililisoup.hotdognalds.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import dev.chililisoup.hotdognalds.client.model.HotdogModel;
import dev.chililisoup.hotdognalds.client.reg.ModSpecialRenderers;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

import static dev.chililisoup.hotdognalds.client.renderer.HotdogRenderer.*;

@Environment(EnvType.CLIENT)
public class HotdogSpecialRenderer implements SpecialModelRenderer<DataComponentMap> {
    private final HotdogModel model;

    public HotdogSpecialRenderer(HotdogModel model) {
        this.model = model;
    }

    @Override
    public @Nullable DataComponentMap extractArgument(@NotNull ItemStack stack) {
        return stack.immutableComponents();
    }

    @Override
    public void submit(
            @Nullable DataComponentMap components,
            @NotNull PoseStack poseStack,
            @NotNull SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 3F, 0.5F);
        poseStack.scale(-1F, -1F, 1F);

        HotdogRenderState state = new HotdogRenderState();
        state.cookAmt = components != null ? components.getOrDefault(ModComponents.COOK_AMOUNT, 0F) : 0F;
        state.lightCoords = lightCoords;

        if (state.cookAmt < 1F)
            submitBlendedTextureModel(state, poseStack, submitNodeCollector, overlayCoords, RAW_TEXTURE, COOKED_TEXTURE, state.cookAmt);
        else if (state.cookAmt < 2F)
            submitBaseTextureModel(state, poseStack, submitNodeCollector, overlayCoords, COOKED_TEXTURE);
        else if (state.cookAmt < 3F)
            submitBlendedTextureModel(state, poseStack, submitNodeCollector, overlayCoords, COOKED_TEXTURE, BURNT_TEXTURE, state.cookAmt - 2F);
        else
            submitBaseTextureModel(state, poseStack, submitNodeCollector, overlayCoords, BURNT_TEXTURE);

        poseStack.popPose();
    }

    @SuppressWarnings("DataFlowIssue")
    private void submitBlendedTextureModel(
            HotdogRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int overlayCoords,
            Identifier texture1,
            Identifier texture2,
            float amt
    ) {
        this.submitBaseTextureModel(state, poseStack, submitNodeCollector, overlayCoords, texture1);
        submitNodeCollector.order(1).submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityTranslucent(texture2),
                state.lightCoords,
                overlayCoords,
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
            int overlayCoords,
            Identifier texture
    ) {
        submitNodeCollector.submitModel(
                this.model,
                state,
                poseStack,
                texture,
                state.lightCoords,
                overlayCoords,
                state.outlineColor,
                null
        );
    }

    @Override
    public void getExtents(@NotNull Consumer<Vector3fc> output) {
        this.model.root().getExtentsForGui(new PoseStack(), output);
    }

    @Environment(EnvType.CLIENT)
    public record Unbaked() implements SpecialModelRenderer.Unbaked<DataComponentMap> {
        public static final HotdogSpecialRenderer.Unbaked INSTANCE = new HotdogSpecialRenderer.Unbaked();
        public static final MapCodec<HotdogSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public @NotNull MapCodec<HotdogSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        public HotdogSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new HotdogSpecialRenderer(
                    new HotdogModel(context.entityModelSet().bakeLayer(ModSpecialRenderers.HOTDOG))
            );
        }
    }
}
