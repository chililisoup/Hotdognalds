package dev.chililisoup.hotdognalds.client.model;

import dev.chililisoup.hotdognalds.client.renderer.HotdogRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

public class HotdogBunModel extends EntityModel<HotdogRenderState> {
    public HotdogBunModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();

        mesh.getRoot().addOrReplaceChild(
                "bun",
                CubeListBuilder.create()
                        .texOffs(12, 0)
                        .addBox(-1.5F, -1.5F, -3.0F, 3.0F, 2.0F, 6.0F),
                PartPose.ZERO
        );

        return LayerDefinition.create(mesh, 32, 16);
    }
}
