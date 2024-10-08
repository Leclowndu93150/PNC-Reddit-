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

package me.desht.pneumaticcraft.client.event;

import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.IGuiDrone;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.drone.progwidgets.SavedDroneProgram;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.upgrades.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

@EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class TooltipEventHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null) return;

        ItemStack stack = event.getItemStack();

        if (stack.getItem() instanceof BucketItem) {
            handleFluidContainerTooltip(event);
        } else if (PneumaticCraftUtils.getRegistryName(stack.getItem()).orElseThrow().getNamespace().equals(Names.MOD_ID)) {
            addStandardTooltip(stack, event.getToolTip(), event.getFlags());
        }
        if (stack.getItem() instanceof IProgrammable programmable) {
            handleProgrammableTooltip(event, programmable);
        }
    }

    private static void addStandardTooltip(ItemStack stack, List<Component> curInfo, TooltipFlag flagIn) {
        addPressureTooltip(stack, curInfo);

        UpgradableItemUtils.addUpgradeInformation(stack, curInfo, flagIn);

        if (stack.getItem() instanceof IInventoryItem item) {
            List<ItemStack> stacks = new ArrayList<>();
            item.getStacksInItem(stack, stacks);
            if (item.getInventoryHeader() != null && !stacks.isEmpty()) {
                curInfo.add(item.getInventoryHeader());
            }
            PneumaticCraftUtils.summariseItemStacks(curInfo, stacks, Component.literal(item.getTooltipPrefix(stack)));
        }

        String key = ICustomTooltipName.getTranslationKey(stack, true);
        if (I18n.exists(key)) {
            if (ClientUtils.hasShiftDown()) {
                String translatedInfo = ChatFormatting.AQUA + I18n.get(key);
                curInfo.addAll(PneumaticCraftUtils.asStringComponent(PneumaticCraftUtils.splitString(translatedInfo)));
                if (!ThirdPartyManager.instance().getDocsProvider().isInstalled()) {
                    curInfo.add(xlate("pneumaticcraft.gui.tab.info.installDocsProvider"));
                }
            } else {
                curInfo.add(xlate("pneumaticcraft.gui.tooltip.sneakForInfo").withStyle(ChatFormatting.AQUA));
            }
        }
    }

    private static void addPressureTooltip(ItemStack stack, List<Component> textList) {
        PNCCapabilities.getAirHandler(stack).ifPresent(airHandler -> {
            float f = airHandler.getPressure() / airHandler.maxPressure();
            ChatFormatting color;
            if (f < 0.1f) {
                color = ChatFormatting.RED;
            } else if (f < 0.5f) {
                color = ChatFormatting.GOLD;
            } else {
                color = ChatFormatting.DARK_GREEN;
            }
            textList.add(xlate("pneumaticcraft.gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(airHandler.getPressure(), 1)).withStyle(color));
        });
    }

    private static void handleProgrammableTooltip(ItemTooltipEvent event, IProgrammable programmable) {
        if (programmable.canProgram(event.getItemStack()) && programmable.showProgramTooltip()) {
            MutableBoolean hasInvalidPrograms = new MutableBoolean(false);
            List<Component> addedEntries = new ArrayList<>();

            SavedDroneProgram program = SavedDroneProgram.fromItemStack(event.getItemStack());
            program.summarize().forEach((widgetType, count) -> {
                ChatFormatting[] prefix = new ChatFormatting[0];
                if (widgetType != null) {
                    Screen curScreen = Minecraft.getInstance().screen;
                    if (curScreen instanceof IGuiDrone guiDrone) {
                        if (!guiDrone.getDrone().isProgramApplicable(widgetType)) {
                            prefix = new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC};
                            hasInvalidPrograms.setTrue();
                        }
                    }
                    addedEntries.add(Component.literal(Symbols.BULLET + " " + count + " x ")
                            .append(xlate(widgetType.getTranslationKey()))
                            .withStyle(prefix));
                }
            });

            if (hasInvalidPrograms.booleanValue()) {
                event.getToolTip().add(xlate("pneumaticcraft.gui.tooltip.programmable.invalidPieces").withStyle(ChatFormatting.RED));
            }

            addedEntries.sort(Comparator.comparing(Component::getString));
            event.getToolTip().addAll(addedEntries);
            if (ClientUtils.hasShiftDown() && !program.isEmpty()) {
                event.getToolTip().add(
                        xlate("pneumaticcraft.gui.tooltip.programmable.requiredPieces", program.getRequiredPuzzlePieces())
                                .withStyle(ChatFormatting.GREEN));
            }
        }
    }

    private static void handleFluidContainerTooltip(ItemTooltipEvent event) {
        FluidUtil.getFluidContained(event.getItemStack()).ifPresent(fluidStack -> {
            String key = ICustomTooltipName.getTranslationKey(event.getItemStack(), true);
            if (I18n.exists(key)) {
                if (Screen.hasShiftDown()) {
                    String translatedInfo = ChatFormatting.AQUA + I18n.get(key);
                    event.getToolTip().addAll(PneumaticCraftUtils.asStringComponent(PneumaticCraftUtils.splitString(translatedInfo)));
                } else {
                    event.getToolTip().add(xlate("pneumaticcraft.gui.tooltip.sneakForInfo").withStyle(ChatFormatting.AQUA));
                }
            }
        });
    }

    @SubscribeEvent
    public static void gatherComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof MicromissilesItem
                && MicromissilesItem.getFireMode(stack) == MicromissilesItem.FireMode.SMART) {
            event.getTooltipElements().add(Either.right(new MicromissilesItem.Tooltip(stack)));
        }
    }
}
