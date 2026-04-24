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
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class CupDrinkModel extends EntityModel<CupRenderState> {
    private final ModelPart drink;

    public CupDrinkModel(ModelPart root) {
        super(root);
        this.drink = root.getChild("drink");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();

        mesh.getRoot().addOrReplaceChild(
                "drink",
                CubeListBuilder.create()
                        .texOffs(-3, 0)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 0.0F, 3.0F),
                PartPose.ZERO
        );

        return LayerDefinition.create(mesh, 16, 8);
    }

    @Override
    public void setupAnim(@NotNull CupRenderState state) {
        super.setupAnim(state);
        this.drink.y = state.contents.fillLevel() * -4.5F;
    }
}
