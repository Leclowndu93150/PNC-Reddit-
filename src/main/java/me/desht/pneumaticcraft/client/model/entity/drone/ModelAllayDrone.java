package me.desht.pneumaticcraft.client.model.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelAllayDrone extends HierarchicalModel<AbstractDroneEntity> implements ArmedModel {
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

    public ModelAllayDrone(ModelPart pRoot) {
        super(RenderType::entityTranslucent);
        this.root = pRoot.getChild("root");
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
        MeshDefinition $$0 = new MeshDefinition();
        PartDefinition $$1 = $$0.getRoot();
        PartDefinition $$2 = $$1.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 23.5F, 0.0F));
        $$2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.99F, 0.0F));
        PartDefinition $$3 = $$2.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, -4.0F, 0.0F));
        $$3.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(23, 0).addBox(-0.75F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offset(-1.75F, 0.5F, 0.0F));
        $$3.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(23, 6).addBox(-0.25F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offset(1.75F, 0.5F, 0.0F));
        $$3.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.0F, 0.6F));
        $$3.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.0F, 0.6F));
        return LayerDefinition.create($$0, 32, 32);
    }

    @Override
    public void setupAnim(AbstractDroneEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float $$6 = pAgeInTicks * 20.0F * 0.017453292F + pLimbSwing;
        float $$7 = Mth.cos($$6) * 3.1415927F * 0.15F + pLimbSwingAmount;
        float $$8 = pAgeInTicks - (float)pEntity.tickCount;
        float $$9 = pAgeInTicks * 9.0F * 0.017453292F;
        float $$10 = Math.min(pLimbSwingAmount / 0.3F, 1.0F);
        float $$11 = 1.0F - $$10;
        float $$12 = Mth.cos($$6) * 3.1415927F * 0.075F * $$11;
        float $$18;
        float $$19;
        float $$20;
        this.head.xRot = pHeadPitch * 0.017453292F;
        this.head.yRot = pNetHeadYaw * 0.017453292F;

        this.right_wing.xRot = 0.43633232F * (1.0F - $$10);
        this.right_wing.yRot = -0.7853982F + $$7;
        this.left_wing.xRot = 0.43633232F * (1.0F - $$10);
        this.left_wing.yRot = 0.7853982F - $$7;
        this.body.xRot = $$10 * 0.7853982F;
        $$18 = $$12 * Mth.lerp($$10, -1.0471976F, -1.134464F);
        ModelPart var10000 = this.root;
        var10000.y += (float)Math.cos((double)$$9) * 0.25F * $$11;
        this.right_arm.xRot = $$18;
        this.left_arm.xRot = $$18;
        $$19 = $$11 * (1.0F - $$12);
        $$20 = 0.43633232F - Mth.cos($$9 + 4.712389F) * 3.1415927F * 0.075F * $$19;
        this.left_arm.zRot = -$$20;
        this.right_arm.zRot = $$20;
        this.right_arm.yRot = 0.27925268F * $$12;
        this.left_arm.yRot = -0.27925268F * $$12;
    }

    public void translateToHand(HumanoidArm pSide, PoseStack pPoseStack) {
        float $$2 = 1.0F;
        float $$3 = 3.0F;
        this.root.translateAndRotate(pPoseStack);
        this.body.translateAndRotate(pPoseStack);
        pPoseStack.translate(0.0F, 0.0625F, 0.1875F);
        pPoseStack.mulPose(Axis.XP.rotation(this.right_arm.xRot));
        pPoseStack.scale(0.7F, 0.7F, 0.7F);
        pPoseStack.translate(0.0625F, 0.0F, 0.0F);
    }
}

