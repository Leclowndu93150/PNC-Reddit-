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

package me.desht.pneumaticcraft.client.gui.remote.actionwidget;

import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.variables.TextVariableParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

import java.util.Optional;
import java.util.UUID;

public class WidgetLabelVariable extends WidgetLabel {
    private final MutableComponent displayedMessage;

    public WidgetLabelVariable(int x, int y, Component text) {
        super(x, y, text);

        displayedMessage = Component.empty();
        UUID uuid = ClientUtils.getClientPlayer().getUUID();
        text.visit((style, string) -> {
            TextVariableParser p = new TextVariableParser(string, uuid);
            displayedMessage.append(Component.literal(p.parse()).withStyle(style));
            return Optional.empty();
        }, text.getStyle());
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Component origText = getMessage();
        setMessage(displayedMessage);
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        setMessage(origText);
    }
}
