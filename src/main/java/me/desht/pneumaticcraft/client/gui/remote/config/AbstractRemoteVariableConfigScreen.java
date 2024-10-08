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

package me.desht.pneumaticcraft.client.gui.remote.config;

import me.desht.pneumaticcraft.api.remote.IRemoteVariableWidget;
import me.desht.pneumaticcraft.client.gui.remote.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class AbstractRemoteVariableConfigScreen<A extends IRemoteVariableWidget> extends AbstractRemoteConfigScreen<A> {
    private boolean playerGlobal;
    private WidgetButtonExtended varTypeButton;
    private WidgetComboBox variableField;

    public AbstractRemoteVariableConfigScreen(A widget, RemoteEditorScreen guiRemote) {
        super(widget, guiRemote);
    }

    @Override
    public void init() {
        super.init();

        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinate.variableName"), guiLeft + 10, guiTop + 70);

        playerGlobal = remoteWidget.varName().isEmpty() || remoteWidget.varName().startsWith("#");

        varTypeButton = new WidgetButtonExtended(guiLeft + 10, guiTop + 78, 12, 14, GlobalVariableHelper.getInstance().getVarPrefix(playerGlobal),
                b -> togglePlayerGlobal())
                .setTooltipKey("pneumaticcraft.gui.remote.varType.tooltip");
        addRenderableWidget(varTypeButton);

        variableField = new WidgetComboBox(font, guiLeft + 23, guiTop + 79, 147, font.lineHeight + 3);
        variableField.setElements(extractVarnames(playerGlobal));
        variableField.setValue(GlobalVariableHelper.getInstance().stripVarPrefix(remoteWidget.varName()));
        variableField.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.remote.variable.tooltip")));
        addRenderableWidget(variableField);
    }

    protected String makeVarName() {
        return GlobalVariableHelper.getInstance().getPrefixedVar(variableField.getValue(), playerGlobal);
    }

    private void togglePlayerGlobal() {
        playerGlobal = !playerGlobal;
        variableField.setElements(extractVarnames(playerGlobal));
        varTypeButton.setMessage(Component.literal(GlobalVariableHelper.getInstance().getVarPrefix(playerGlobal)));
    }
}
