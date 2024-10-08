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

package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetFluidStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetVerticalScrollbar;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetLiquidFilter;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProgWidgetLiquidFilterScreen extends AbstractProgWidgetScreen<ProgWidgetLiquidFilter> {
    private static final int GRID_WIDTH = 8;
    private static final int GRID_HEIGHT = 6;
    private WidgetFluidStack mainFilter;
    private WidgetVerticalScrollbar scrollbar;
    private WidgetTextField searchField;
    private int lastScroll;
    private final List<WidgetFluidStack> visibleFluidWidgets = new ArrayList<>();
    private int textTimer = 0;
    private long lastClickTime = 0L;

    public ProgWidgetLiquidFilterScreen(ProgWidgetLiquidFilter widget, ProgrammerScreen guiProgrammer) {
        super(widget, guiProgrammer);

        xSize = 176;
        ySize = 166;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_ITEM_SEARCHER;
    }

    @Override
    public void init() {
        super.init();

        mainFilter = new WidgetFluidStack(guiLeft + 148, guiTop + 25, progWidget.getFluidStack().getFluid(), b -> {
            b.setFluid(Fluids.EMPTY);
            progWidget.setFluidStack(FluidStack.EMPTY);
        });
        addRenderableWidget(mainFilter);

        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                WidgetFluidStack f = new WidgetFluidStack(guiLeft + 8 + x * 18, guiTop + 52 + y * 18, Fluids.EMPTY, this::fluidClicked);
                addRenderableWidget(f);
                visibleFluidWidgets.add(f);
            }
        }

        searchField = new WidgetTextField(font, guiLeft + 8, guiTop + 34, 90);
        addRenderableWidget(searchField);
        setFocused(searchField);
        searchField.setResponder(s -> textTimer = 5);

        scrollbar = new WidgetVerticalScrollbar(guiLeft + 155, guiTop + 51, 108);
        scrollbar.setListening(true);
        addRenderableWidget(scrollbar);

        addValidFluids();
    }

    private void addValidFluids() {
        List<Fluid> fluids = BuiltInRegistries.FLUID.stream()
                .filter(fluid -> matchSearch(searchField.getValue(), fluid))
                .sorted(Comparator.comparing(f -> new FluidStack(f, 1).getHoverName().getString()))
                .toList();

        scrollbar.setStates(Math.max(0, (fluids.size() - GRID_WIDTH * GRID_HEIGHT + GRID_WIDTH - 1) / GRID_WIDTH));

        int offset = scrollbar.getState() * GRID_WIDTH;
        for (int i = 0; i < visibleFluidWidgets.size(); i++) {
            if (i + offset < fluids.size()) {
                visibleFluidWidgets.get(i).setFluid(fluids.get(i + offset));
            } else {
                visibleFluidWidgets.get(i).setFluid(Fluids.EMPTY);
            }
        }
    }

    private void fluidClicked(WidgetFluidStack widget) {
        long now = Util.getMillis();
        if (now - lastClickTime < 250L && FluidStack.isSameFluidSameComponents(progWidget.getFluidStack(), widget.getFluidStack())) {
            onClose();
        } else {
            mainFilter.setFluid(widget.getFluid());
            progWidget.setFluidStack(widget.getFluidStack().copy());
        }
        lastClickTime = now;
    }

    private boolean matchSearch(String srch, Fluid fluid) {
        if (fluid == Fluids.EMPTY || !fluid.isSource(fluid.defaultFluidState())) {
            return false;
        }
        if (srch.isEmpty()) {
            return true;
        }
        String srchL = srch.toLowerCase();
        if (srchL.startsWith("@")) {
            ResourceLocation id = PneumaticCraftUtils.getRegistryName(fluid).orElse(ResourceLocation.withDefaultNamespace("none"));
            return id.getNamespace().startsWith(srchL.substring(1));
        } else if (srchL.startsWith("#")) {
            ResourceLocation id = ResourceLocation.tryParse(srchL.substring(1));
            return id != null && fluid.builtInRegistryHolder().is(TagKey.create(Registries.FLUID, id));
        } else {
            return new FluidStack(fluid, 1).getHoverName().getString().toLowerCase().contains(srchL);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (lastScroll != scrollbar.getState()) {
            lastScroll = scrollbar.getState();
            addValidFluids();
        } else if (textTimer > 0 && --textTimer == 0) {
            addValidFluids();
        }
    }
}
