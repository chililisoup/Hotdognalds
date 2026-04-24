package dev.chililisoup.hotdognalds.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.chililisoup.hotdognalds.block.CrateBlock;
import dev.chililisoup.hotdognalds.block.entity.CrateBlockEntity;
import dev.chililisoup.hotdognalds.item.HotdogContents;
import dev.chililisoup.hotdognalds.reg.ModComponents;
import it.unimi.dsi.fastutil.HashCommon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class CrateRenderer implements BlockEntityRenderer<CrateBlockEntity, CrateRenderState> {
    private final ItemModelResolver itemModelResolver;

    public CrateRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public int getViewDistance() {
        return 24;
    }

    @Override
    public void submit(
            @NotNull CrateRenderState state,
            @NotNull PoseStack poseStack,
            @NotNull SubmitNodeCollector submitNodeCollector,
            @NotNull CameraRenderState camera
    ) {
        if (state.itemStackRenderState == null) return;

        poseStack.pushPose();

        poseStack.translate(0.5F, 0.01F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.translate(-0.5F, state.yOffset, -0.5F);

        poseStack.scale(state.scale, state.scale, state.scale);

        for (int y = 0; y < 4; y++) {
            float yo = y / (state.scale * 4F);
            float zFightOffset = (y % 2) * 0.001F - 0.001F;
            for (int x = 0; x < state.xCount; x += 1) {
                float xo = (x + 0.5F) / (state.scale * state.xCount) + zFightOffset;
                for (int z = 0; z < state.zCount; z++) {
                    float zo = (z + 0.5F) / (state.scale * state.zCount) + zFightOffset;
                    poseStack.pushPose();
                    poseStack.translate(xo, yo, zo);
                    state.itemStackRenderState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                    poseStack.popPose();
                }
            }
        }

        poseStack.popPose();
    }

    @Override
    public void extractRenderState(
            @NotNull CrateBlockEntity blockEntity,
            @NotNull CrateRenderState state,
            float partialTicks,
            @NotNull Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        ItemStack itemStack = blockEntity.getTheItem();
        if (!itemStack.isEmpty()) {
            ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(
                    itemStackRenderState,
                    itemStack,
                    ItemDisplayContext.ON_SHELF,
                    blockEntity.getLevel(),
                    blockEntity,
                    HashCommon.long2int(blockEntity.getBlockPos().asLong())
            );
            state.itemStackRenderState = itemStackRenderState;

            state.yRot = (switch (blockEntity.getBlockState().getValue(CrateBlock.FACING)) {
                case NORTH -> Direction.NORTH;
                case SOUTH -> Direction.SOUTH;
                case WEST -> Direction.EAST;
                default -> Direction.WEST;
            }).toYRot();


            AABB box = state.itemStackRenderState.getModelBoundingBox();
            state.yOffset = (float) -box.minY / 4F;

            if (itemStack.get(ModComponents.HOTDOG_CONTENTS) instanceof HotdogContents contents) {
                state.yRot += 90F;
                if (contents.hasDog()) {
                    state.scale = 0.5F;
                    state.zCount = 6;
                } else {
                    state.scale = 0.67F;
                    state.zCount = 4;
                }
                state.xCount = 2;

                return;
            }

            if (itemStack.has(ModComponents.CUP_CONTENTS)) {
                state.yRot += 90F;
                state.yOffset += 0.375F;
                state.scale = 0.67F;
                state.zCount = 4;
                state.xCount = 4;

                return;
            }

            state.scale = 0.25F;
            state.xCount = Mth.clamp(Mth.floor(3.0 / box.getXsize()), 1, 4);
            state.zCount = Mth.clamp(Mth.floor(3.0 / box.getZsize()), 1, 4);
        }
    }

    @Override
    public @NotNull CrateRenderState createRenderState() {
        return new CrateRenderState();
    }
}
