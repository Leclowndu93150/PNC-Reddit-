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

package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange.TemperatureScale;
import me.desht.pneumaticcraft.api.crafting.recipe.RefineryRecipe;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIRefineryCategory extends AbstractPNCCategory<RefineryRecipe> {
    private final ITickTimer tickTimer;
    private final Map<RefineryRecipe, WidgetTemperature> tempWidgets = new IdentityHashMap<>();

    JEIRefineryCategory() {
        super(RecipeTypes.REFINERY,
                xlate(ModBlocks.REFINERY.get().getDescriptionId()),
                guiHelper().createDrawable(Textures.GUI_REFINERY, 6, 15, 166, 79),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.REFINERY.get()))
        );
        tickTimer = JEIPlugin.jeiHelpers.getGuiHelper().createTickTimer(60, 60, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RefineryRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 2, 10)
                .addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.asList(recipe.getInput().getFluids()))
                .setFluidRenderer(recipe.getInput().amount(), true, 16, 64)
                .setOverlay(Helpers.makeTankOverlay(64), 0, 0);

        int n = 1;
        for (FluidStack out : recipe.getOutputs()) {
            int h = out.getAmount() * 64 / recipe.getInput().amount();
            int yOff = 64 - h;
            builder.addSlot(RecipeIngredientRole.OUTPUT, 69 + n * 20, 18 - n * 4 + yOff)
                    .addIngredient(NeoForgeTypes.FLUID_STACK, out)
                    .setFluidRenderer(out.getAmount(), true, 16, h)
                    .setOverlay(Helpers.makeTankOverlay(h), 0, 0);
            n++;
        }
    }

    @Override
    public void draw(RefineryRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        WidgetTemperature w = tempWidgets.computeIfAbsent(recipe,
                id -> WidgetTemperature.fromOperatingRange(26, 18, recipe.getOperatingTemp()));
        w.setTemperature(w.getTotalRange().getMin() + (w.getTotalRange().getMax() - w.getTotalRange().getMin()) * tickTimer.getValue() / tickTimer.getMaxValue());
        w.renderWidget(graphics, (int)mouseX, (int)mouseY, 0f);
    }

    @Override
    public List<Component> getTooltipStrings(RefineryRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        WidgetTemperature w = tempWidgets.get(recipe);
        if (w != null && w.isMouseOver(mouseX, mouseY)) {
            return ImmutableList.of(HeatUtil.formatHeatString(recipe.getOperatingTemp().asString(TemperatureScale.CELSIUS)));
        }
        return Collections.emptyList();
    }
}
