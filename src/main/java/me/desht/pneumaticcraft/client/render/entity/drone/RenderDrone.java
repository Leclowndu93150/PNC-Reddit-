package me.desht.pneumaticcraft.client.render.entity.drone;

import me.desht.pneumaticcraft.client.model.entity.drone.ModelAllayDrone;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.allay.Allay;

public class RenderDrone extends MobRenderer<AbstractDroneEntity, ModelAllayDrone> {
    private static final ResourceLocation ALLAY_TEXTURE = new ResourceLocation("textures/entity/allay/allay.png");

    public RenderDrone(EntityRendererProvider.Context p_234551_) {
        super(p_234551_, new ModelAllayDrone(p_234551_.bakeLayer(ModelLayers.ALLAY)), 0.4F);
        this.addLayer(new ItemInHandLayer<>(this, p_234551_.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractDroneEntity abstractDroneEntity) {
        return ALLAY_TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(AbstractDroneEntity pEntity, BlockPos pPos) {
        return 15;
    }
}
