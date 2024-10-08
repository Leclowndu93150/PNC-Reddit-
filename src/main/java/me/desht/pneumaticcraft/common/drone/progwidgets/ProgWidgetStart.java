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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetStart extends ProgWidget {
    public static final MapCodec<ProgWidgetStart> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).apply(builder, ProgWidgetStart::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetStart> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ProgWidgetStart::new
    );

    public ProgWidgetStart(PositionFields pos) {
        super(pos);
    }

    public ProgWidgetStart() {
        super(PositionFields.DEFAULT);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetStart(getPosition());
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public boolean hasStepOutput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_START;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.LIME;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.START.get();
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        for (IProgWidget widget : widgets) {
            if (widget != this && widget instanceof ProgWidgetStart) {
                curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.error.multipleStartPieces"));
                break;
            }
        }
    }
}
