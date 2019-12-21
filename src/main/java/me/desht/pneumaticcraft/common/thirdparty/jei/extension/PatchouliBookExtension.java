package me.desht.pneumaticcraft.common.thirdparty.jei.extension;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.special.PatchouliBookCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.SpecialRecipe;

public class PatchouliBookExtension extends AbstractShapelessExtension {
    public PatchouliBookExtension(SpecialRecipe recipe) {
        super(recipe, new ItemStack(PatchouliBookCrafting.GUIDE_BOOK), Items.BOOK, ModItems.INGOT_IRON_COMPRESSED);

        PatchouliBookCrafting.setBookNBT(getOutput());
    }
}