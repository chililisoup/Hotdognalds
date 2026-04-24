package dev.chililisoup.hotdognalds.client.model;

import dev.chililisoup.hotdognalds.client.renderer.CupRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

@Environment(EnvType.CLIENT)
public class CupModel extends EntityModel<CupRenderState> {
    public CupModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition cup = root.addOrReplaceChild(
                "cup",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.5F, -5.0F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.ZERO
        );

        cup.addOrReplaceChild(
                "straw",
                CubeListBuilder.create()
                        .texOffs(12, 0)
                        .addBox(-0.5F, -7.25F, -0.5F, 1.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1681F, -0.4279F, 0.2217F)
        );

        return LayerDefinition.create(mesh, 16, 8);
    }
}
