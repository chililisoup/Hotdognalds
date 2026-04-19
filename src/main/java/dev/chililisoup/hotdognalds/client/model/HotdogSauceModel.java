package dev.chililisoup.hotdognalds.client.model;

import dev.chililisoup.hotdognalds.client.renderer.HotdogRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

@Environment(EnvType.CLIENT)
public class HotdogSauceModel extends EntityModel<HotdogRenderState> {
    public HotdogSauceModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();

        mesh.getRoot().addOrReplaceChild(
                "sauce",
                CubeListBuilder.create()
                        .texOffs(13, 8)
                        .addBox(-1.0F, -2.0F, -3.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.1F)),
                PartPose.ZERO
        );

        return LayerDefinition.create(mesh, 32, 16);
    }
}
