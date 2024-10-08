package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelAllayDrone;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.common.item.minigun.AbstractGunAmmoItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.AllayRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class DroneHeldItemLayer extends RenderLayer<AbstractDroneEntity, ModelAllayDrone> {
    private final ItemInHandRenderer itemInHandRenderer;

    DroneHeldItemLayer(RenderDrone renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractDroneEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack held = entityIn.getDroneHeldItem();
        if (!held.isEmpty() && !(held.getItem() instanceof AbstractGunAmmoItem && entityIn.hasMinigun())) {
            renderHeldItem(held, matrixStackIn, bufferIn, packedLightIn, LivingEntityRenderer.getOverlayCoords(entityIn, 0.0F), entityIn.level(), entityIn);
        }
    }

    private void renderHeldItem(@Nonnull ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, int packedOverlay, Level level, AbstractDroneEntity drone) {
        matrixStack.pushPose();

        // Lower the item by 1 block (was 1.5, now 0.5)
        matrixStack.translate(-0.05D, 1.5D, -1.55D); // Move the item forward (-Z) to place it in front of the drone

        matrixStack.mulPose(Axis.YP.rotationDegrees(-90));

        this.getParentModel().translateToHand(drone.getMainArm(), matrixStack);

        float scaleFactor = stack.getItem() instanceof BlockItem ? 0.7F : 0.5F;
        matrixStack.scale(scaleFactor, scaleFactor, scaleFactor);

        // Render the item
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, matrixStack, buffer, level, 0);

        matrixStack.popPose();
    }
}
