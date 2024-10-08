package me.desht.pneumaticcraft.client.model.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Items;

public class ModelDrone extends EntityModel<AbstractDroneEntity> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_wing;
    private final ModelPart left_wing;
    private static final float FLYING_ANIMATION_X_ROT = 0.7853982F;
    private static final float MAX_HAND_HOLDING_ITEM_X_ROT_RAD = -1.134464F;
    private static final float MIN_HAND_HOLDING_ITEM_X_ROT_RAD = -1.0471976F;

    public ModelDrone(ModelPart p_233312_) {
        super(RenderType::entityTranslucent);
        this.root = p_233312_.getChild("root");
        this.head = this.root.getChild("head");
        this.body = this.root.getChild("body");
        this.right_arm = this.body.getChild("right_arm");
        this.left_arm = this.body.getChild("left_arm");
        this.right_wing = this.body.getChild("right_wing");
        this.left_wing = this.body.getChild("left_wing");
    }

    public ModelPart root() {
        return this.root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 23.5F, 0.0F));
        partdefinition1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.99F, 0.0F));
        PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, -4.0F, 0.0F));
        partdefinition2.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(23, 0).addBox(-0.75F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offset(-1.75F, 0.5F, 0.0F));
        partdefinition2.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(23, 6).addBox(-0.25F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offset(1.75F, 0.5F, 0.0F));
        partdefinition2.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.0F, 0.6F));
        partdefinition2.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.0F, 0.6F));
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public void setupAnim(AbstractDroneEntity p_233325_, float p_233326_, float p_233327_, float p_233328_, float p_233329_, float p_233330_) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float f = p_233328_ * 20.0F * 0.017453292F + p_233326_;
        float f1 = Mth.cos(f) * 3.1415927F * 0.15F + p_233327_;
        float f2 = p_233328_ - (float)p_233325_.tickCount;
        float f3 = p_233328_ * 9.0F * 0.017453292F;
        float f4 = Math.min(p_233327_ / 0.3F, 1.0F);
        float f5 = 1.0F - f4;
        float f6 = p_233328_ * 0.02F;
        float f12;
        float f13;
        float f14;
        this.head.xRot = p_233330_ * 0.017453292F;
        this.head.yRot = p_233329_ * 0.017453292F;

        this.right_wing.xRot = 0.43633232F * (1.0F - f4);
        this.right_wing.yRot = -0.7853982F + f1;
        this.left_wing.xRot = 0.43633232F * (1.0F - f4);
        this.left_wing.yRot = 0.7853982F - f1;
        this.body.xRot = f4 * 0.7853982F;
        f12 = f6 * Mth.lerp(f4, -1.0471976F, -1.134464F);
        this.root.y += (float)Math.cos((double)f3) * 0.25F * f5;
        this.right_arm.xRot = f12;
        this.left_arm.xRot = f12;
        f13 = f5 * (1.0F - f6);
        f14 = 0.43633232F - Mth.cos(f3 + 4.712389F) * 3.1415927F * 0.075F * f13;
        this.left_arm.zRot = -f14;
        this.right_arm.zRot = f14;
        this.right_arm.yRot = 0.27925268F * f6;
        this.left_arm.yRot = -0.27925268F * f6;
    }

    public void translateToHand(HumanoidArm p_233322_, PoseStack p_233323_) {
        float f = 1.0F;
        float f1 = 3.0F;
        this.root.translateAndRotate(p_233323_);
        this.body.translateAndRotate(p_233323_);
        p_233323_.translate(0.0F, 0.0625F, 0.1875F);
        p_233323_.mulPose(Axis.XP.rotation(this.right_arm.xRot));
        p_233323_.scale(0.7F, 0.7F, 0.7F);
        p_233323_.translate(0.0625F, 0.0F, 0.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int i1, int i2) {
        this.root.render(poseStack, vertexConsumer, i, i1, i2);
    }
}
