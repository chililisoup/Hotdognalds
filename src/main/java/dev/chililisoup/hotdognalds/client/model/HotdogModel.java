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
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(
                "dawg",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-10.5F, -1.0F, 3.0F, 5.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 10)
                        .addBox(-10.5F, -2.0F, 6.0F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(8.0F, 24.0F, -8.0F)
        );

        return LayerDefinition.create(meshdefinition, 16, 16);
    }
}
