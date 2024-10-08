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

package me.desht.pneumaticcraft.common.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class VortexEntity extends ThrowableProjectile {
    private int hitCounter = 0;

    // clientside: rendering X offset of vortex, depends on which hand the vortex was fired from
    private float renderOffsetX = -Float.MAX_VALUE;

    private static int lastPlayerBoost;  // client-side

    public VortexEntity(EntityType<? extends VortexEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();

        // onImpact() is no longer called for blocks with no collision box, like shrubs & crops, as of MC 1.16.2
        if (!level().isClientSide) {
            BlockPos.betweenClosedStream(getBoundingBox())
                    .filter(pos -> vortexBreakable(level(), pos))
                    .forEach(this::handleVortexCollision);
        }

        setDeltaMovement(getDeltaMovement().scale(0.99));
        if (getDeltaMovement().lengthSqr() < 0.1D) {
            discard();
        }
    }


    public boolean hasRenderOffsetX() {
        return renderOffsetX > -Float.MAX_VALUE;
    }

    public float getRenderOffsetX() {
        return renderOffsetX;
    }

    public void setRenderOffsetX(float renderOffsetX) {
        this.renderOffsetX = renderOffsetX;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (hitResult instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof Player player && (!player.level().isClientSide() || boostedRecently(player))) {
                return;
            }
            // for players, doing this client-side only does work as expected
            entity.setDeltaMovement(entity.getDeltaMovement().add(this.getDeltaMovement().add(0, 0.4, 0)));
            if (entity instanceof LivingEntity livingEntity && getOwner() instanceof Player shooter) {
                ItemStack shears = new ItemStack(Items.SHEARS);
                shears.getItem().interactLivingEntity(shears, shooter, livingEntity, InteractionHand.MAIN_HAND);
            }
        } else if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos pos = blockHitResult.getBlockPos();
            if (vortexBreakable(level(), pos)) {
                if (!level().isClientSide) {
                    handleVortexCollision(pos);
                }
            } else {
                discard();
            }
        }
        if (++hitCounter > 20) {
            discard();
        }
    }

    private boolean boostedRecently(Player player) {
        if (player.tickCount - lastPlayerBoost < 20) {
            return true;
        }
        lastPlayerBoost = player.tickCount;
        return false;
    }

    private void handleVortexCollision(BlockPos pos) {
        level().destroyBlock(pos, true);
        setDeltaMovement(getDeltaMovement().scale(0.85D));
    }

    private boolean vortexBreakable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && state.getDestroySpeed(level, pos) == 0f
                || state.getBlock() instanceof LeavesBlock
                || state.getBlock() instanceof WebBlock
                || state.getBlock() instanceof SnowLayerBlock;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // none
    }
}
