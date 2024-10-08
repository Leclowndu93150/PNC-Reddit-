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

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIBlockInteraction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class ProgWidgetDigAndPlace extends ProgWidgetAreaItemBase implements IBlockOrdered, IMaxActions {
    protected static <P extends ProgWidgetDigAndPlace> Products.P2<RecordCodecBuilder.Mu<P>, PositionFields, ProgWidgetDigAndPlace.DigPlaceFields> digPlaceParts(RecordCodecBuilder.Instance<P> pInstance) {
        return baseParts(pInstance).and(
                ProgWidgetDigAndPlace.DigPlaceFields.CODEC.fieldOf("dig_place").forGetter(p -> p.digPlaceFields)
        );
    }

    protected DigPlaceFields digPlaceFields;

    public ProgWidgetDigAndPlace(PositionFields pos, DigPlaceFields digPlaceFields) {
        super(pos);

        this.digPlaceFields = digPlaceFields;
    }

    @Override
    public Ordering getOrder() {
        return digPlaceFields.order;
    }

    @Override
    public void setOrder(Ordering order) {
        this.digPlaceFields = digPlaceFields.withOrder(order);
    }

    @Override
    public int getMaxActions() {
        return digPlaceFields.maxActions;
    }

    @Override
    public void setMaxActions(int maxActions) {
        digPlaceFields = digPlaceFields.withMaxActions(maxActions);
    }

    @Override
    public boolean useMaxActions() {
        return digPlaceFields.useMaxActions;
    }

    @Override
    public void setUseMaxActions(boolean useMaxActions) {
        this.digPlaceFields = digPlaceFields.withUseMaxActions(useMaxActions);
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(xlate("pneumaticcraft.message.misc.order", xlate(getOrder().getTranslationKey())));
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return Collections.singletonList(xlate(getOrder().getTranslationKey()));
    }

    DroneAIBlockInteraction<?> setupMaxActions(DroneAIBlockInteraction<?> ai, IMaxActions widget) {
        return widget.useMaxActions() ? ai.setMaxActions(widget.getMaxActions()) : ai;
    }

    @Override
    protected boolean baseEquals(ProgWidget other) {
        return super.baseEquals(other) && other instanceof ProgWidgetDigAndPlace d && digPlaceFields.equals(d.digPlaceFields);
    }

    @Override
    protected int baseHashCode() {
        return Objects.hash(positionFields, digPlaceFields);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProgWidgetDigAndPlace that = (ProgWidgetDigAndPlace) o;
        return baseEquals(that);
    }

    @Override
    public int hashCode() {
        return baseHashCode();
    }

    public record DigPlaceFields(Ordering order, int maxActions, boolean useMaxActions) {
        public static DigPlaceFields makeDefault(Ordering order) {
            return new DigPlaceFields(order, 1, false);
        }

        public static final Codec<DigPlaceFields> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                StringRepresentable.fromEnum(Ordering::values).fieldOf("order").forGetter(DigPlaceFields::order),
                Codec.INT.optionalFieldOf("max_actions", 1).forGetter(DigPlaceFields::maxActions),
                Codec.BOOL.optionalFieldOf("use_max_actions", false).forGetter(DigPlaceFields::useMaxActions)
        ).apply(builder, DigPlaceFields::new));

        public static final StreamCodec<FriendlyByteBuf, DigPlaceFields> STREAM_CODEC = StreamCodec.composite(
                NeoForgeStreamCodecs.enumCodec(Ordering.class), DigPlaceFields::order,
                ByteBufCodecs.VAR_INT, DigPlaceFields::maxActions,
                ByteBufCodecs.BOOL, DigPlaceFields::useMaxActions,
                DigPlaceFields::new
        );

        public DigPlaceFields withOrder(Ordering order) {
            return new DigPlaceFields(order, maxActions, useMaxActions);
        }

        public DigPlaceFields withMaxActions(int maxActions) {
            return new DigPlaceFields(order, maxActions, useMaxActions);
        }

        public DigPlaceFields withUseMaxActions(boolean useMaxActions) {
            return new DigPlaceFields(order, maxActions, useMaxActions);
        }
    }

}
