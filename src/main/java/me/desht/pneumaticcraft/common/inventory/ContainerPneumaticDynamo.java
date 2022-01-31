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

import me.desht.pneumaticcraft.common.core.ModMenuTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDynamo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ContainerPneumaticDynamo extends ContainerEnergy<TileEntityPneumaticDynamo> {
    public ContainerPneumaticDynamo(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        super(ModMenuTypes.PNEUMATIC_DYNAMO.get(), i, playerInventory, getTilePos(buffer));
    }

    public ContainerPneumaticDynamo(int i, Inventory playerInventory, BlockPos tilePos) {
        super(ModMenuTypes.PNEUMATIC_DYNAMO.get(), i, playerInventory, tilePos);
    }
}
