/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.item.ICustomDurabilityBar;
import me.desht.pneumaticcraft.common.block.entity.processing.UVLightBoxBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.BlockEntityConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.List;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EmptyPCBItem extends NonDespawningItem implements ICustomDurabilityBar, CreativeTabStackProvider {
    public EmptyPCBItem() {
        super(ModItems.defaultProps()
                .component(ModDataComponents.ETCH_PROGRESS, 0)
                .component(ModDataComponents.UV_EXPOSURE, 0)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> infoList, TooltipFlag flag) {
        super.appendHoverText(stack, context, infoList, flag);

        int uvProgress = UVLightBoxBlockEntity.getExposureProgress(stack);
        int etchProgress = getEtchProgress(stack);

        if (etchProgress > 0) {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.item.uvLightBox.etchProgress", etchProgress));
        }
        infoList.add(xlate("pneumaticcraft.gui.tooltip.item.uvLightBox.successChance", uvProgress));
        if (uvProgress < 100 && etchProgress == 0) {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.item.uvLightBox.putInLightBox").withStyle(ChatFormatting.GRAY));
        }
        if (uvProgress > 0) {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.item.uvLightBox.putInAcid").withStyle(ChatFormatting.GRAY));
        }
    }

    public static int getEtchProgress(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.ETCH_PROGRESS, 0);
    }

    public static void setEtchProgress(ItemStack stack, int progress) {
        stack.set(ModDataComponents.ETCH_PROGRESS, Mth.clamp(progress, 0, 100));
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        int progress = UVLightBoxBlockEntity.getExposureProgress(pStack);
        return Math.round(progress / 100F * 13F);
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        int progress = UVLightBoxBlockEntity.getExposureProgress(pStack);
        return progress * 2 << 16 | 0xFF;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
        super.onEntityItemUpdate(stack, entityItem);

        if (entityItem.level().getFluidState(entityItem.blockPosition()).getType().is(PneumaticCraftTags.Fluids.ETCHING_ACID)) {
            int etchProgress = getEtchProgress(stack);
            if (etchProgress < 100) {
                if (entityItem.tickCount % (BlockEntityConstants.PCB_ETCH_TIME / 5) == 0) {
                    setEtchProgress(stack, etchProgress + 1);
                }
                Level level = entityItem.getCommandSenderWorld();
                if (level.random.nextInt(15) == 0) {
                    double x = entityItem.getX() + level.random.nextDouble() * 0.3 - 0.15;
                    double y = entityItem.getY() - 0.15;
                    double z = entityItem.getZ() + level.random.nextDouble() * 0.3 - 0.15;
                    level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0, 0.05, 0.0);
                }
            } else if (!entityItem.level().isClientSide) {
                int successCount = 0;
                int failedCount = 0;
                int uvProgress = UVLightBoxBlockEntity.getExposureProgress(stack);
                for (int i = 0; i < stack.getCount(); i++) {
                    if (entityItem.level().random.nextInt(100) <= uvProgress) {
                        successCount++;
                    } else {
                        failedCount++;
                    }
                }

                ItemStack successStack = new ItemStack(successCount == 0 ? ModItems.FAILED_PCB.get() : ModItems.UNASSEMBLED_PCB.get(),
                        successCount == 0 ? failedCount : successCount);
                entityItem.setItem(successStack);

                // Only when we have failed items and the existing item entity wasn't reused already for the failed items.
                if (successCount > 0 && failedCount > 0) {
                    ItemStack failedStack = new ItemStack(ModItems.FAILED_PCB.get(), failedCount);
                    entityItem.level().addFreshEntity(new ItemEntity(entityItem.level(), entityItem.getX(), entityItem.getY(), entityItem.getZ(), failedStack));
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldShowCustomDurabilityBar(ItemStack stack) {
        return EmptyPCBItem.getEtchProgress(stack) > 0;
    }

    @Override
    public int getCustomDurabilityColour(ItemStack stack) {
        return MapColor.EMERALD.col;
    }

    @Override
    public float getCustomDurability(ItemStack stack) {
        return EmptyPCBItem.getEtchProgress(stack) / 100f;
    }

    @Override
    public boolean isShowingOtherBar(ItemStack stack) {
        return true;
    }

    @Override
    public Stream<ItemStack> getStacksForItem() {
        ItemStack stack = new ItemStack(this);
        ItemStack stack2 = UVLightBoxBlockEntity.setExposureProgress(stack.copy(), 100);
        return Stream.of(stack, stack2);
    }

    /**
     * {@return an itemstack for the item produced on successful etching in the Etching Tank}
     */
    public ItemStack getSuccessItem() {
        return new ItemStack(ModItems.UNASSEMBLED_PCB.get());
    }

    /**
     * {@return an itemstack for the item produced on failed etching in the Etching Tank}
     */
    public ItemStack getFailedItem() {
        return new ItemStack(ModItems.FAILED_PCB.get());
    }

    public static FluidStack getEtchingFluid() {
        return new FluidStack(ModFluids.ETCHING_ACID.get(), FluidType.BUCKET_VOLUME);
    }
}
