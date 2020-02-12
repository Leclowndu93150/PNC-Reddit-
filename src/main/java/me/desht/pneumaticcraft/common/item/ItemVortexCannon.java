package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemVortexCannon extends ItemPressurizable {

    public ItemVortexCannon() {
        super(PneumaticValues.VORTEX_CANNON_MAX_AIR, PneumaticValues.VORTEX_CANNON_VOLUME);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity playerIn, Hand handIn) {
        ItemStack iStack = playerIn.getHeldItem(handIn);

        IAirHandlerItem airHandler = iStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                .orElseThrow(RuntimeException::new);
        float factor = 0.2F * airHandler.getPressure();

        if (world.isRemote) {
            if (airHandler.getPressure() > 0.1f) {
                world.playSound(playerIn.posX, playerIn.posY, playerIn.posZ, ModSounds.AIR_CANNON.get(), SoundCategory.PLAYERS, 1.0F, 0.7F + factor * 0.2F, false);
            } else {
                playerIn.playSound(SoundEvents.BLOCK_COMPARATOR_CLICK, 1.0f, 2f);
                return ActionResult.newResult(ActionResultType.FAIL, iStack);
            }
        } else {
            if (airHandler.getPressure() > 0.1f) {
                world.playSound(playerIn, playerIn.posX, playerIn.posY, playerIn.posZ, ModSounds.AIR_CANNON.get(), SoundCategory.PLAYERS, 1.0F, 0.7F + factor * 0.2F);
                EntityVortex vortex = ModEntities.VORTEX.get().create(world);
                Vec3d directionVec = playerIn.getLookVec().normalize().scale(-0.25);
                Vec3d vortexPos = playerIn.getPositionVector().add(0, playerIn.getEyeHeight() / 2, 0).add(directionVec);
                vortex.posX = vortexPos.x;
                vortex.posY = vortexPos.y;
                vortex.posZ = vortexPos.z;
                vortex.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F * factor, 0.0F);
                world.addEntity(vortex);
                airHandler.addAir(-PneumaticValues.USAGE_VORTEX_CANNON);
            }
        }
        return ActionResult.newResult(ActionResultType.SUCCESS, iStack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
}
