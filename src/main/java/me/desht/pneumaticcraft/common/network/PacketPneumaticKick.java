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

import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Comparator;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client when the kick hotkey is pressed
 */
public enum PacketPneumaticKick implements CustomPacketPayload {
    INSTANCE;

    public static final ResourceLocation ID = RL("pneumatic_kick");

    public static PacketPneumaticKick fromNetwork(@SuppressWarnings("unused") FriendlyByteBuf buf) {
        return INSTANCE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketPneumaticKick message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            if (handler.upgradeUsable(CommonUpgradeHandlers.kickHandler, false)) {
                int upgrades = handler.getUpgradeCount(EquipmentSlot.FEET, ModUpgrades.DISPENSER.get());
                if (upgrades > 0) {
                    message.handleKick(player, Math.min(PneumaticValues.PNEUMATIC_KICK_MAX_UPGRADES, upgrades));
                }
            }
        }));
    }

    private void handleKick(Player player, int upgrades) {
        Vec3 lookVec = new Vec3(player.getLookAngle().x, Math.max(0, player.getLookAngle().y), player.getLookAngle().z).normalize();

        double playerFootY = player.getY() - player.getBbHeight() / 2;
        AABB box = new AABB(player.getX(), playerFootY, player.getZ(), player.getX(), playerFootY, player.getZ())
                .inflate(1.5, 1.5, 1.5).move(lookVec);
        List<Entity> entities = player.level().getEntities(player, box);
        if (entities.isEmpty()) return;
        entities.sort(Comparator.comparingDouble(o -> o.distanceToSqr(player)));

        Entity target = entities.get(0);
        if (!target.skipAttackInteraction(player)) {
            if (target instanceof LivingEntity living) {
                target.hurt(target.damageSources().playerAttack(player), 3.0f + upgrades * 0.5f);
                living.setJumping(true);
            }
            target.setOnGround(false);
            target.horizontalCollision = false;
            target.verticalCollision = false;
            target.setDeltaMovement(target.getDeltaMovement().add(lookVec.scale(1.0 + upgrades * 0.5)).add(0, upgrades * 0.1, 0));
        }
        player.level().playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.PUNCH.get(), SoundSource.PLAYERS, 1f, 1f);
        NetworkHandler.sendToAllTracking(PacketSetEntityMotion.create(target, target.getDeltaMovement()), target);
        NetworkHandler.sendToAllTracking(new PacketSpawnParticle(ParticleTypes.EXPLOSION, target.getX(), target.getY(), target.getZ(), 1.0D, 0.0D, 0.0D), target);
        CommonArmorHandler.getHandlerForPlayer(player).addAir(EquipmentSlot.FEET, -PneumaticValues.PNEUMATIC_KICK_AIR_USAGE * upgrades);
    }
}
