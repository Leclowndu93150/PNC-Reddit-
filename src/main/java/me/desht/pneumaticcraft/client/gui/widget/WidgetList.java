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

package me.desht.pneumaticcraft.client.gui.widget;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WidgetList<T> extends AbstractWidget {
    @Nonnull
    private final Consumer<WidgetList<T>> pressable;
    private final List<T> items = new ArrayList<>();
    private int selected = -1;  // < 0 indicates nothing selected
    private long lastClick = 0;
    private boolean doubleClicked;
    private int fgColor = 0x404040;
    private int selectedFg = 0xFFFFFF;
    private int selectedBg = 0x000000;
    private boolean inverseSelected = true;
    private ToolTipType toolTipType = ToolTipType.AUTO;

    public WidgetList(int xIn, int yIn, int width, int height) {
        this(xIn, yIn, width, height, c -> {});
    }

    public WidgetList(int xIn, int yIn, int width, int height, @Nonnull Consumer<WidgetList<T>> pressable) {
        super(xIn, yIn, width, height, Component.empty());

        this.pressable = pressable;
    }

    public WidgetList<T> setColor(int color) {
        this.fgColor = color;
        return this;
    }

    public WidgetList<T> setToolTipType(ToolTipType toolTipType) {
        this.toolTipType = toolTipType;
        return this;
    }

    public WidgetList<T> setSelectedColors(int selectedFg, int selectedBg) {
        this.selectedFg = selectedFg;
        this.selectedBg = selectedBg;
        return this;
    }

    public WidgetList<T> inverseSelected(boolean inverse) {
        this.inverseSelected = inverse;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            drawList(graphics);

            setTooltip(null);
            if (toolTipType != ToolTipType.NONE) {
                int h = Minecraft.getInstance().font.lineHeight;
                int idx = Math.max(0, (mouseY - this.getY()) / h);
                if (idx < items.size()) {
                    String s = items.get(idx).toString();
                    if (toolTipType == ToolTipType.ALWAYS || Minecraft.getInstance().font.width(s) * 3 / 4 > width) {
                        setTooltip(Tooltip.create(Component.literal(s)));
                    }
                }
            }
        }
    }

    public boolean isDoubleClicked() {
        return doubleClicked;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public void unselectAll() {
        this.selected = -1;
    }

    public List<T> getLines() {
        return ImmutableList.copyOf(items);
    }

    public T getSelectedLine() {
        return selected >= 0 && selected < items.size() ? items.get(selected) : null;
    }

    public boolean contains(T s) {
        return items.contains(s);
    }

    public void add(T s) {
        items.add(s);
    }

    public void removeSelected() {
        if (selected >= 0 && selected < items.size()) {
            items.remove(selected);
            updateSelection();
        }
    }

    public void clear() {
        items.clear();
        setSelected(-1);
    }

    private void updateSelection() {
        if (items.isEmpty()) {
            selected = -1;
        } else if (selected >= items.size()) {
            selected = items.size() - 1;
        }
    }

    public int size() {
        return items.size();
    }

    private void drawList(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        int lineHeight = mc.font.lineHeight;
        int lines = height / lineHeight;

        int x = getX(), y = getY();

        graphics.pose().pushPose();
        graphics.enableScissor(x, y, x + width, y + height);
        if (inverseSelected && selected >= 0) {
            graphics.fill(x, y + lineHeight * selected, x + width, y + lineHeight * (selected + 1), 0xFF000000 | selectedBg);
        }
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(0.75f, 1f, 1f);
        for (int i = 0; i < items.size() && i < lines; i++) {
            graphics.drawString(mc.font, items.get(i).toString(), 0, i * lineHeight, i == selected ? selectedFg : fgColor, false);
        }
        graphics.disableScissor();
        graphics.pose().popPose();
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (active) {
            long now = System.currentTimeMillis();
            int h = Minecraft.getInstance().font.lineHeight;
            int newSel = Mth.clamp((int) (mouseY - this.getY()) / h, 0, items.size() - 1);
            doubleClicked = now - lastClick < 250 && newSel == selected;
            setSelected(newSel);
            lastClick = now;
            pressable.accept(this);
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }

    public enum ToolTipType {
        NONE,
        AUTO,
        ALWAYS
    }
}
