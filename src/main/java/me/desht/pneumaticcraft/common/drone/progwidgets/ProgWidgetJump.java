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

package me.desht.pneumaticcraft.common.drone.progwidgets;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetJump extends ProgWidget implements IJump {
    public static final MapCodec<ProgWidgetJump> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).apply(builder, ProgWidgetJump::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetJump> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ProgWidgetJump::new
    );

    public ProgWidgetJump(PositionFields pos) {
        super(pos);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetJump(getPosition());
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.JUMP.get();
    }

    public ProgWidgetJump() {
        super(PositionFields.DEFAULT);
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null) curInfo.add(xlate("pneumaticcraft.gui.progWidget.label.error.noJumpLocation"));
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public boolean hasStepOutput() {
        return false;
    }

    @Override
    public IProgWidget getOutputWidget(IDrone drone, List<IProgWidget> allWidgets) {
        ProgWidgetText jumpedLabel = (ProgWidgetText) getConnectedParameters()[0];
        if (jumpedLabel != null) {
            IDroneBase.asDroneBase(drone).getAIManager().setLabel(jumpedLabel.string);
            IProgWidget widget = jumpToLabel(drone, allWidgets, jumpedLabel.string);
            if (widget != null) return widget;
        }
        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.jump.nowhereToJump");
        return null;
    }

    static IProgWidget jumpToLabel(IDrone drone, List<IProgWidget> allWidgets, IProgWidget conditionWidget, boolean conditionValue) {
        ProgWidgetText textWidget = (ProgWidgetText) (conditionValue ?
                conditionWidget.getConnectedParameters()[conditionWidget.getParameters().size() - 1] :
                conditionWidget.getConnectedParameters()[conditionWidget.getParameters().size() * 2 - 1]);
        if (textWidget != null) {
            return jumpToLabel(drone, allWidgets, textWidget.string);
        } else {
            IProgWidget widget = conditionWidget.getOutputWidget();
            if (widget == null) drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.jump.nowhereToJump");
            return widget;
        }
    }

    static IProgWidget jumpToLabel(IDrone drone, List<IProgWidget> allWidgets, String label) {
        IDroneBase.asDroneBase(drone).getAIManager().setLabel(label);
        List<IProgWidget> possibleJumpLocations = new ArrayList<>();
        for (IProgWidget widget : allWidgets) {
            if (widget instanceof ILabel) {
                String labelLabel = ((ILabel) widget).getLabel();
                if (labelLabel != null && labelLabel.equals(label)) {
                    possibleJumpLocations.add(widget);
                }
            }
        }
        if (possibleJumpLocations.isEmpty()) {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.jump.nowhereToJump");
            return null;
        } else {
            return possibleJumpLocations.get(ThreadLocalRandom.current().nextInt(possibleJumpLocations.size()));
        }

    }

    @Override
    public IProgWidget getOutputWidget() {
        return null;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.TEXT.get());
    }

    @Override
    protected boolean hasBlacklist() {
        return false;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_JUMP;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.MEDIUM;
    }

    @Override
    public List<String> getPossibleJumpLocations() {
        ProgWidgetText jumpedLabel = (ProgWidgetText) getConnectedParameters()[0];
        if (jumpedLabel != null) {
            return Collections.singletonList(jumpedLabel.string);
        }
        return Collections.emptyList();
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }
}
