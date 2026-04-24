package dev.chililisoup.hotdognalds.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import dev.chililisoup.hotdognalds.item.CupContents;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class CupSpecialRenderer implements SpecialModelRenderer<DataComponentMap> {
    private final BaseCupRenderer baseRenderer;

    public CupSpecialRenderer(BakingContext context) {
        this.baseRenderer = new BaseCupRenderer(context.entityModelSet());
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
        poseStack.translate(0.5F, 0F, 0.5F);
        poseStack.scale(-1F, -1F, 1F);

        CupRenderState state = new CupRenderState();
        state.lightCoords = lightCoords;
        state.outlineColor = outlineColor;
        state.contents = components != null ?
                components.getOrDefault(ModComponents.CUP_CONTENTS, CupContents.EMPTY) :
                CupContents.EMPTY;

        this.baseRenderer.submit(state, poseStack, submitNodeCollector, overlayCoords);

        poseStack.popPose();
    }

    @Override
    public void getExtents(@NotNull Consumer<Vector3fc> output) {
        this.baseRenderer.model.root().getExtentsForGui(new PoseStack(), output);
    }

    @Environment(EnvType.CLIENT)
    public record Unbaked() implements SpecialModelRenderer.Unbaked<DataComponentMap> {
        public static final CupSpecialRenderer.Unbaked INSTANCE = new CupSpecialRenderer.Unbaked();
        public static final MapCodec<CupSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public @NotNull MapCodec<CupSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        public CupSpecialRenderer bake(@NotNull SpecialModelRenderer.BakingContext context) {
            return new CupSpecialRenderer(context);
        }
    }
}
