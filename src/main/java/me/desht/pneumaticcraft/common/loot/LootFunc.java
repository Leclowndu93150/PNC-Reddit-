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

package me.desht.pneumaticcraft.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.block.entity.*;
import me.desht.pneumaticcraft.common.registry.ModLootFunctions;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.api.lib.NBTKeys.NBT_AIR_AMOUNT;
import static me.desht.pneumaticcraft.api.lib.NBTKeys.NBT_SIDE_CONFIG;

public class LootFunc {
    /**
     * Handle the standard serialization of PNC block entity data to the dropped itemstack.
     * Saved to the "BlockEntityTag" NBT tag, so will be copied directly back to the BE's NBT
     * by {@link net.minecraft.world.item.BlockItem#updateCustomBlockEntityTag(Level, Player, BlockPos, ItemStack)}
     */
    public static class BlockEntitySerializerFunction extends LootItemConditionalFunction {
        public static final Codec<BlockEntitySerializerFunction> CODEC
                = RecordCodecBuilder.create(instance -> commonFields(instance).apply(instance, BlockEntitySerializerFunction::new));

        private BlockEntitySerializerFunction(List<LootItemCondition> conditionsIn) {
            super(conditionsIn);
        }

        @Override
        protected ItemStack run(ItemStack stack, LootContext context) {
            return applyBEdata(stack, context.getParamOrNull(LootContextParams.BLOCK_ENTITY));
        }

        public static net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Builder<?> builder() {
            return simpleBuilder(BlockEntitySerializerFunction::new);
        }

        private ItemStack applyBEdata(ItemStack teStack, BlockEntity te) {
            // augment existing BlockEntityTag if present, otherwise create a new one
            CompoundTag nbt = teStack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
            final CompoundTag subTag = nbt == null ? new CompoundTag() : nbt;

            // fluid tanks
            if (te instanceof ISerializableTanks s) {
                CompoundTag tankTag = s.serializeTanks();
                if (!tankTag.isEmpty()) {
                    subTag.put(NBTKeys.NBT_SAVED_TANKS, tankTag);
                }
            }

            // side configuration
            if (te instanceof ISideConfigurable sc) {
                CompoundTag tag = SideConfigurator.writeToNBT(sc);
                if (!tag.isEmpty()) {
                    subTag.put(NBT_SIDE_CONFIG, tag);
                }
            }

            // redstone mode
            if (te instanceof IRedstoneControl<?> rc) {
                rc.getRedstoneController().serialize(subTag);
            }

            if (te instanceof AbstractPneumaticCraftBlockEntity teB) {
                if (teB.shouldPreserveStateOnBreak()) {
                    // upgrades (only when wrenched)
                    AbstractPneumaticCraftBlockEntity.UpgradeHandler upgradeHandler = teB.getUpgradeHandler();
                    for (int i = 0; i < upgradeHandler.getSlots(); i++) {
                        if (!upgradeHandler.getStackInSlot(i).isEmpty()) {
                            // store creative status directly since it's queried for item model rendering (performance)
                            if (teB.getUpgrades(ModUpgrades.CREATIVE.get()) > 0) {
                                NBTUtils.setBoolean(teStack, UpgradableItemUtils.NBT_CREATIVE, true);
                            } else {
                                NBTUtils.removeTag(teStack, UpgradableItemUtils.NBT_CREATIVE);
                            }
                            subTag.put(UpgradableItemUtils.NBT_UPGRADE_TAG, upgradeHandler.serializeNBT());
                            break;
                        }
                    }

                    // saved air (only when wrenched)
                    IAirHandlerMachine handler = te.getLevel().getCapability(PNCCapabilities.AIR_HANDLER_MACHINE, te.getBlockPos(), te.getBlockState(), te, null);
                    if (handler != null && handler.getPressure() != 0f) {
                        subTag.putInt(NBT_AIR_AMOUNT, handler.getAir());
                    }
                }

                teB.serializeExtraItemData(subTag, teB.shouldPreserveStateOnBreak());
            }

            if (!subTag.isEmpty()) {
                CompoundTag tag = teStack.getOrCreateTag();
                tag.put(NBTKeys.BLOCK_ENTITY_TAG, subTag);
            } else {
                if (teStack.hasTag() && Objects.requireNonNull(teStack.getTag()).contains(NBTKeys.BLOCK_ENTITY_TAG)) {
                    teStack.getTag().remove(NBTKeys.BLOCK_ENTITY_TAG);
                }
            }
            return teStack;
        }

        @Override
        public LootItemFunctionType getType() {
            return ModLootFunctions.TE_SERIALIZER.get();
        }
    }
}
