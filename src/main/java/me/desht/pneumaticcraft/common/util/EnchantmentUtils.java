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

package me.desht.pneumaticcraft.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;

import java.util.List;

public class EnchantmentUtils {

    /**
     * Be warned, minecraft doesn't update experienceTotal properly, so we have
     * to do this.
     *
     * @param player the player
     * @return player's current XP amount
     */
    public static int getPlayerXP(Player player) {
        return (int)(EnchantmentUtils.getExperienceForLevel(player.experienceLevel) + (player.experienceProgress * player.getXpNeededForNextLevel()));
    }

    public static void addPlayerXP(Player player, int amount) {
        int experience = getPlayerXP(player) + amount;
        player.totalExperience = experience;
        player.experienceLevel = EnchantmentUtils.getLevelForExperience(experience);
        int expForLevel = EnchantmentUtils.getExperienceForLevel(player.experienceLevel);
        player.experienceProgress = (float)(experience - expForLevel) / (float)player.getXpNeededForNextLevel();
    }

    public static int xpBarCap(int level) {
        if (level >= 30)
            return 112 + (level - 30) * 9;

        if (level >= 15)
            return 37 + (level - 15) * 5;

        return 7 + level * 2;
    }

    private static int sum(int n, int a0, int d) {
        return n * (2 * a0 + (n - 1) * d) / 2;
    }

    public static int getExperienceForLevel(int level) {
        if (level == 0) return 0;
        if (level <= 15) return sum(level, 7, 2);
        if (level <= 30) return 315 + sum(level - 15, 37, 5);
        return 1395 + sum(level - 30, 112, 9);
    }

    public static int getXpToNextLevel(int level) {
        int levelXP = EnchantmentUtils.getLevelForExperience(level);
        int nextXP = EnchantmentUtils.getExperienceForLevel(level + 1);
        return nextXP - levelXP;
    }

    public static int getLevelForExperience(int targetXp) {
        int level = 0;
        while (true) {
            final int xpToNextLevel = xpBarCap(level);
            if (targetXp < xpToNextLevel) return level;
            level++;
            targetXp -= xpToNextLevel;
        }
    }

    public static float getPower(Level world, BlockPos position) {
        float power = 0;

        for (int deltaZ = -1; deltaZ <= 1; ++deltaZ) {
            for (int deltaX = -1; deltaX <= 1; ++deltaX) {
                if ((deltaZ != 0 || deltaX != 0)
                        && world.isEmptyBlock(position.offset(deltaX, 0, deltaZ))
                        && world.isEmptyBlock(position.offset(deltaX, 1, deltaZ))) {
                    power += getEnchantPower(world, position.offset(deltaX * 2, 0, deltaZ * 2));
                    power += getEnchantPower(world, position.offset(deltaX * 2, 1, deltaZ * 2));
                    if (deltaX != 0 && deltaZ != 0) {
                        power += getEnchantPower(world, position.offset(deltaX * 2, 0, deltaZ));
                        power += getEnchantPower(world, position.offset(deltaX * 2, 1, deltaZ));
                        power += getEnchantPower(world, position.offset(deltaX, 0, deltaZ * 2));
                        power += getEnchantPower(world, position.offset(deltaX, 1, deltaZ * 2));
                    }
                }
            }
        }
        return power;
    }

    public static float getEnchantPower(Level world, BlockPos pos) {
        return world.getBlockState(pos).getEnchantPowerBonus(world, pos);
    }

//    public static void addAllBooks(Enchantment enchantment, List<ItemStack> items) {
//        for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); i++)
//            items.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, i)));
//    }

    public static Holder<Enchantment> getEnchantment(HolderLookup.Provider provider, ResourceKey<Enchantment> key) {
        return provider.lookup(Registries.ENCHANTMENT).orElseThrow().getOrThrow(key);
    }
}
