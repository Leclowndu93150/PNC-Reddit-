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

package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.BuiltinArmorUpgrades;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableBlock;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.hacking.HackTickTracker;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketHackingBlockFinish;
import me.desht.pneumaticcraft.common.network.PacketHackingEntityFinish;
import me.desht.pneumaticcraft.common.registry.ModCriterionTriggers;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class HackHandler extends BaseArmorUpgradeHandler<HackHandler.HackData> {

    @Override
    public ResourceLocation getID() {
        return BuiltinArmorUpgrades.HACKING;
    }

    @Override
    public PNCUpgrade[] getRequiredUpgrades() {
        return new PNCUpgrade[] { ModUpgrades.SECURITY.get() };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public Supplier<HackData> extensionData() {
        return HackData::new;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        Player player = commonArmorHandler.getPlayer();
        if (player instanceof ServerPlayer sp) {
            commonArmorHandler.getExtensionData(this).tickServerSide(sp);
        }
    }

    public static class HackData implements IArmorExtensionData {
        private int hackTime;
        private GlobalPos hackedBlockPos;
        private Entity hackedEntity;

        private void tickServerSide(ServerPlayer player) {
            if (hackedBlockPos != null) {
                tickBlockHack(player);
            } else if (hackedEntity != null) {
                tickEntityHack(player);
            }
        }

        private void tickBlockHack(ServerPlayer player) {
            Level level = GlobalPosHelper.getWorldForGlobalPos(player.getServer(), hackedBlockPos);
            BlockPos pos = hackedBlockPos.pos();

            IHackableBlock hackableBlock = HackManager.getHackableForBlock(level, pos, player);
            if (hackableBlock != null) {
                int requiredTime = hackableBlock.getHackTime(level, pos, player);
                if (requiredTime <= 0) {
                    setHackedBlockPos(null);
                    return;
                }
                if (++this.hackTime >= requiredTime) {
                    hackableBlock.onHackComplete(player.level(), pos, player);
                    HackTickTracker.getInstance(player.level()).trackBlock(pos, hackableBlock);
                    NetworkHandler.sendToAllTracking(PacketHackingBlockFinish.create(pos), player.level(), player.blockPosition());
                    setHackedBlockPos(null);
                    ModCriterionTriggers.BLOCK_HACK.get().trigger(player);
                }
            } else {
                setHackedBlockPos(null);
            }
        }

        private void tickEntityHack(ServerPlayer player) {
            IHackableEntity<?> hackableEntity = HackManager.getHackableForEntity(hackedEntity, player);
            if (hackableEntity != null) {
                int requiredTime = hackableEntity._getHackTime(hackedEntity, player);
                if (requiredTime <= 0) {
                    setHackedEntity(null);
                    return;
                }
                if (++hackTime >= requiredTime) {
                    if (hackedEntity.isAlive()) {
                        hackableEntity._onHackFinished(hackedEntity, player);
                        HackTickTracker.getInstance(player.level()).trackEntity(hackedEntity, hackableEntity);
                        NetworkHandler.sendToAllTracking(PacketHackingEntityFinish.forEntity(hackedEntity), hackedEntity);
                        ModCriterionTriggers.ENTITY_HACK.get().trigger(player);
                    }
                    setHackedEntity(null);
                }
            } else {
                setHackedEntity(null);
            }
        }

        public void setHackedBlockPos(GlobalPos pos) {
            this.hackedBlockPos = pos;
            this.hackedEntity = null;
            this.hackTime = 0;
        }

        public void setHackedEntity(Entity entity) {
            hackedEntity = entity;
            hackedBlockPos = null;
            hackTime = 0;
        }
    }
}
