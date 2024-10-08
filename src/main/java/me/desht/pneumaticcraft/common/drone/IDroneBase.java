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

package me.desht.pneumaticcraft.common.drone;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.network.DronePacket;
import me.desht.pneumaticcraft.common.util.fakeplayer.DroneItemHandler;

/**
 * Non-API extension to public IDrone interface
 */
public interface IDroneBase extends IDrone {
    static IDroneBase asDroneBase(IDrone drone) {
        if (!(drone instanceof IDroneBase droneBase)) {
            throw new IllegalStateException("impossible: IDrone isn't an IDroneBase!");
        }
        return droneBase;
    }

    static DroneEntity asDrone(IDrone drone) {
        if (!(drone instanceof DroneEntity droneEntity)) {
            throw new IllegalStateException("impossible: IDrone isn't a DroneEntity!");
        }
        return droneEntity;
    }

    DroneAIManager getAIManager();

    /**
     * Get the currently-active AI manager. Normally the drone's own aiManager object, but if currently executing
     * an External Program widget, then return the sub-AI which is in effect.
     * @return the active AI manager
     */
    default DroneAIManager getActiveAIManager() {
        return getAIManager().getActiveManager();
    }

    LogisticsManager getLogisticsManager();

    void setLogisticsManager(LogisticsManager logisticsManager);

    DroneItemHandler getDroneItemHandler();

    DronePacket.DroneTarget getPacketTarget();

}
