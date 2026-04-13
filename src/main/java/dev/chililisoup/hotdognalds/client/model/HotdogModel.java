package dev.chililisoup.hotdognalds.client.model;

import dev.chililisoup.hotdognalds.client.renderer.HotdogRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

@Environment(EnvType.CLIENT)
public class HotdogModel extends EntityModel<HotdogRenderState> {
    public HotdogModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition bone = partDefinition.addOrReplaceChild(
                "bone",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -2.0F, -4.0F, 2.0F, 2.0F, 8.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        bone.addOrReplaceChild(
                "tail_south",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 5.0F, 0.7854F, -0.6109F, -0.5236F)
        );
        bone.addOrReplaceChild(
                "tail_north",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, -5.0F, -2.3562F, 0.6109F, -0.5236F)
        );

        return LayerDefinition.create(meshDefinition, 32, 16);
    }
}
