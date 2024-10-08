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

package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.block.entity.utility.SecurityStationBlockEntity;
import me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation;
import me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation.ConnectionEntry;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController.HackingSide;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 *
 * Sent by the server periodically while a Security Station hack is in progress, and when either side has just won,
 * to keep the client up-to-date.  Client also runs the simulation (dead reckoning), but needs to be updated by the
 * server every so often to keep the display accurate.
 */
public record PacketSyncHackSimulationUpdate(BlockPos pos,
                                             List<ConnectionEntry> playerConns,
                                             List<ConnectionEntry> aiConns,
                                             List<Pair<Integer, Integer>> fortification,
                                             boolean aiAwake, boolean aiStopWormed,
                                             boolean aiWon, boolean playerWon)
        implements CustomPacketPayload {
    public static final Type<PacketSyncHackSimulationUpdate> TYPE = new Type<>(RL("sync_hack_simulation"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncHackSimulationUpdate> STREAM_CODEC = StreamCodec.of(
            PacketSyncHackSimulationUpdate::toNetwork,
            PacketSyncHackSimulationUpdate::fromNetwork
    );

    public static PacketSyncHackSimulationUpdate forSecurityStation(SecurityStationBlockEntity te) {
        HackSimulation aiSim = te.getSimulationController().getSimulation(HackingSide.AI);
        HackSimulation playerSim = te.getSimulationController().getSimulation(HackingSide.PLAYER);

        var playerConns = te.getSimulationController().getSimulation(HackingSide.PLAYER).allConnections;
        var aiConns = aiSim.allConnections;
        List<Pair<Integer,Integer>> fortification = new ArrayList<>();
        for (int i = 0; i < HackSimulation.GRID_SIZE; i++) {
            if (aiSim.getNodeAt(i) != null && aiSim.getNodeAt(i).getFortification() > 0) {
                fortification.add(Pair.of(i, aiSim.getNodeAt(i).getFortification()));
            }
        }

        return new PacketSyncHackSimulationUpdate(te.getBlockPos(), playerConns, aiConns, fortification,
                aiSim.isAwake(), aiSim.isStopWormed(), aiSim.isHackComplete(), playerSim.isHackComplete());
    }

    public static PacketSyncHackSimulationUpdate fromNetwork(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        List<ConnectionEntry> playerConns = buffer.readList(buf -> ConnectionEntry.STREAM_CODEC.decode(buf));
        List<ConnectionEntry> aiConns = buffer.readList(buf -> ConnectionEntry.STREAM_CODEC.decode(buf));
        List<Pair<Integer,Integer>> fortification = buffer.readList(buf -> Pair.of(buf.readVarInt(), buf.readVarInt()));
        boolean aiAwake = buffer.readBoolean();
        boolean aiStopWormed = buffer.readBoolean();
        boolean aiWon = buffer.readBoolean();
        boolean playerWon = buffer.readBoolean();

        return new PacketSyncHackSimulationUpdate(pos, playerConns, aiConns, fortification, aiAwake, aiStopWormed, aiWon, playerWon);
    }

    public static void toNetwork(FriendlyByteBuf buffer, PacketSyncHackSimulationUpdate message) {
        buffer.writeBlockPos(message.pos);
        buffer.writeCollection(message.playerConns, (buf, c) -> ConnectionEntry.STREAM_CODEC.encode(buf, c));
        buffer.writeCollection(message.aiConns, (buf, c) -> ConnectionEntry.STREAM_CODEC.encode(buf, c));
        buffer.writeCollection(message.fortification, (buf, pair) -> {
            buf.writeVarInt(pair.getLeft());
            buf.writeVarInt(pair.getRight());
        });
        buffer.writeBoolean(message.aiAwake);
        buffer.writeBoolean(message.aiStopWormed);
        buffer.writeBoolean(message.aiWon);
        buffer.writeBoolean(message.playerWon);
    }

    @Override
    public Type<PacketSyncHackSimulationUpdate> type() {
        return TYPE;
    }

    public static void handle(PacketSyncHackSimulationUpdate message, IPayloadContext ctx) {
        if (ctx.player().level().getBlockEntity(message.pos()) instanceof SecurityStationBlockEntity secStation) {
            ISimulationController controller = secStation.getSimulationController();
            if (controller != null) {
                HackSimulation aiSim = controller.getSimulation(HackingSide.AI);
                HackSimulation playerSim = controller.getSimulation(HackingSide.PLAYER);
                playerSim.syncFromServer(message.playerConns());
                aiSim.syncFromServer(message.aiConns());
                aiSim.updateFortification(message.fortification());
                if (message.aiAwake()) aiSim.wakeUp();
                aiSim.applyStopWorm(message.aiStopWormed() ? 100 : 0);
                if (message.aiWon()) aiSim.setHackComplete();
                if (message.playerWon()) playerSim.setHackComplete();
            }
        }
    }
}
