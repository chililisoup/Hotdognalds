package dev.chililisoup.hotdognalds.client.model;

import dev.chililisoup.hotdognalds.client.renderer.CondimentDispenserRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class CondimentDispenserModel extends EntityModel<CondimentDispenserRenderState> {
    private final ModelPart pump;

    public CondimentDispenserModel(ModelPart root) {
        super(root);
        this.pump = root.getChild("pump");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "bottle",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0F, -7.0F, -2.0F, 4.0F, 7.0F, 4.0F)
                        .texOffs(0, 11)
                        .addBox(-1.0F, -8.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F)
        );

        PartDefinition pump = root.addOrReplaceChild(
                "pump",
                CubeListBuilder.create()
                        .texOffs(0, 14)
                        .addBox(-4.0F, -5.0F, -0.5F, 5.0F, 1.0F, 1.0F)
                        .texOffs(5, 11)
                        .addBox(-1.0F, -4.5F, -1.5F, 2.0F, 0.0F, 3.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F)
        );
        pump.addOrReplaceChild(
                "pump_neck_1",
                CubeListBuilder.create()
                        .texOffs(12, 11)
                        .addBox(-0.5F, -4.0F, 0.0F, 1.0F, 4.0F, 0.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F)
        );
        pump.addOrReplaceChild(
                "pump_neck_2",
                CubeListBuilder.create()
                        .texOffs(14, 11)
                        .addBox(-0.5F, -4.0F, 0.0F, 1.0F, 4.0F, 0.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F)
        );

        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void setupAnim(@NotNull CondimentDispenserRenderState state) {
        super.setupAnim(state);
        this.pump.y = 16F + state.pumpAmt * 4F;
    }
}
