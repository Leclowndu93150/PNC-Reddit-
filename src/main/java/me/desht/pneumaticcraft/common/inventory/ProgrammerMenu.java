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

package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;
import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammerBlockEntity;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSendNBTPacket;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.List;

public class ProgrammerMenu extends AbstractPneumaticCraftMenu<ProgrammerBlockEntity> {
    public ProgrammerMenu(int i, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.PROGRAMMER.get(), i, playerInventory, pos);

        // server side doesn't care about slot positioning, so doesn't care about screen res either
        boolean isClient = playerInventory.player.level().isClientSide;
        AreaGeometry geometry = isClient ? ProgrammerScreen.calculateAreaBounds() : AreaGeometry.DUMMY;

        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, geometry.x + geometry.width + 24, geometry.y - 8) {
            @Override
            public boolean mayPlace(@Nonnull ItemStack stack) {
                return IProgrammable.isProgrammable(stack);
            }
        });

        // Add the player's inventory slots to the container
        addPlayerSlots(playerInventory, geometry.x + geometry.width / 2 - 81, geometry.y + geometry.height + 6);
    }

    public ProgrammerMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, getTilePos(buffer));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        // update the client about contents of adjacent inventories so the programmer GUI knows what
        // puzzle pieces are available
        if (blockEntity.nonNullLevel().getGameTime() % 20 == 0) {
            for (Direction d : DirectionUtil.VALUES) {
                BlockEntity neighbor = blockEntity.getCachedNeighbor(d);
                if (neighbor != null && IOHelper.getInventoryForBlock(neighbor, d.getOpposite()).isPresent()) {
                    final AbstractPneumaticCraftMenu<?> self = this;
                    List<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().stream()
                            .filter(p -> p.containerMenu == self)
                            .toList();
                    if (!players.isEmpty()) {
                        players.forEach(p -> NetworkHandler.sendToPlayer(PacketSendNBTPacket.forBlockEntity(neighbor), p));
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slotIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot srcSlot = slots.get(slotIndex);

        if (srcSlot != null && srcSlot.hasItem()) {
            ItemStack stackInSlot = srcSlot.getItem();
            stack = stackInSlot.copy();

            if (slotIndex == 0) {
                if (!moveItemStackTo(stackInSlot, 1, 36, false)) return ItemStack.EMPTY;
                srcSlot.onQuickCraft(stackInSlot, stack);
            } else if (IProgrammable.isProgrammable(stack)) {
                if (!moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
                srcSlot.onQuickCraft(stackInSlot, stack);
            }
            if (stackInSlot.isEmpty()) {
                srcSlot.set(ItemStack.EMPTY);
            } else {
                srcSlot.setChanged();
            }

            if (stackInSlot.getCount() == stack.getCount()) return ItemStack.EMPTY;

            srcSlot.onTake(par1EntityPlayer, stackInSlot);
        }

        return stack;
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);

        if (playerIn.level().isClientSide) ProgrammerScreen.onCloseFromContainer();
    }

    public record AreaGeometry(int x, int y, int width, int height) {
        // dummy value used on server-side for slot position calculation where it doesn't matter
        // see ProgrammerScreen.calculateAreaBounds() for client-side calculation
        public static final AreaGeometry DUMMY = new AreaGeometry(5, 17, 100, 100);
    }
}
