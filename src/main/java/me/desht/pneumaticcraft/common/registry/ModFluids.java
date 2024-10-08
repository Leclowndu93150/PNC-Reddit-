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

package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.fluid.*;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Names.MOD_ID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, Names.MOD_ID);

    public static final Supplier<FluidType> OIL_FLUID_TYPE = registerFluidType("oil",
            standardProps(800, 20000));
    public static final Supplier<FluidType> BIODIESEL_FLUID_TYPE = registerFluidType("biodiesel",
            standardProps(880, 4000));
    public static final Supplier<FluidType> DIESEL_FLUID_TYPE = registerFluidType("diesel",
            standardProps(880, 4000));
    public static final Supplier<FluidType> ETCHING_ACID_FLUID_TYPE = registerFluidType("etching_acid",
            standardProps(1500, 2000));
    public static final Supplier<FluidType> ETHANOL_FLUID_TYPE = registerFluidType("ethanol",
            standardProps(789, 1200));
    public static final Supplier<FluidType> GASOLINE_FLUID_TYPE = registerFluidType("gasoline",
            standardProps(750, 500));
    public static final Supplier<FluidType> KEROSENE_FLUID_TYPE = registerFluidType("kerosene",
            standardProps(790, 2700));
    public static final Supplier<FluidType> LPG_FLUID_TYPE = registerFluidType("lpg",
            standardProps(550, 200));
    public static final Supplier<FluidType> LUBRICANT_FLUID_TYPE = registerFluidType("lubricant",
            standardProps(800, 900));
    public static final Supplier<FluidType> MEMORY_ESSENCE_FLUID_TYPE = registerFluidType("memory_essence",
            standardProps(250, 250));
    public static final Supplier<FluidType> PLASTIC_FLUID_TYPE = registerFluidType("plastic",
            standardProps(2000, 500).temperature(PneumaticValues.MOLTEN_PLASTIC_TEMPERATURE));
    public static final Supplier<FluidType> VEGETABLE_OIL_FLUID_TYPE = registerFluidType("vegetable_oil",
            standardProps(900, 1500));
    public static final Supplier<FluidType> YEAST_CULTURE_FLUID_TYPE = registerFluidType("yeast_culture",
            standardProps(800, 5000));

    public static final Supplier<FlowingFluid> OIL = register("oil", FluidOil.Source::new);
    public static final Supplier<FlowingFluid> OIL_FLOWING = register("oil_flowing", FluidOil.Flowing::new);

    public static final Supplier<FlowingFluid> ETCHING_ACID = register("etching_acid", FluidEtchingAcid.Source::new);
    public static final Supplier<FlowingFluid> ETCHING_ACID_FLOWING = register("etching_acid_flowing", FluidEtchingAcid.Flowing::new);

    public static final Supplier<FlowingFluid> PLASTIC = register("plastic", FluidPlastic.Source::new);
    public static final Supplier<FlowingFluid> PLASTIC_FLOWING = register("plastic_flowing", FluidPlastic.Flowing::new);

    public static final Supplier<FlowingFluid> DIESEL = register("diesel", FluidDiesel.Source::new);
    public static final Supplier<FlowingFluid> DIESEL_FLOWING = register("diesel_flowing", FluidDiesel.Flowing::new);

    public static final Supplier<FlowingFluid> KEROSENE = register("kerosene", FluidKerosene.Source::new);
    public static final Supplier<FlowingFluid> KEROSENE_FLOWING = register("kerosene_flowing", FluidKerosene.Flowing::new);

    public static final Supplier<FlowingFluid> GASOLINE = register("gasoline", FluidGasoline.Source::new);
    public static final Supplier<FlowingFluid> GASOLINE_FLOWING = register("gasoline_flowing", FluidGasoline.Flowing::new);

    public static final Supplier<FlowingFluid> LPG = register("lpg", FluidLPG.Source::new);
    public static final Supplier<FlowingFluid> LPG_FLOWING = register("lpg_flowing", FluidLPG.Flowing::new);

    public static final Supplier<FlowingFluid> LUBRICANT = register("lubricant", FluidLubricant.Source::new);
    public static final Supplier<FlowingFluid> LUBRICANT_FLOWING = register("lubricant_flowing", FluidLubricant.Flowing::new);

    public static final Supplier<FlowingFluid> MEMORY_ESSENCE = register("memory_essence", FluidMemoryEssence.Source::new);
    public static final Supplier<FlowingFluid> MEMORY_ESSENCE_FLOWING = register("memory_essence_flowing", FluidMemoryEssence.Flowing::new);

    public static final Supplier<FlowingFluid> YEAST_CULTURE = register("yeast_culture", FluidYeastCulture.Source::new);
    public static final Supplier<FlowingFluid> YEAST_CULTURE_FLOWING = register("yeast_culture_flowing", FluidYeastCulture.Flowing::new);

    public static final Supplier<FlowingFluid> ETHANOL = register("ethanol", FluidEthanol.Source::new);
    public static final Supplier<FlowingFluid> ETHANOL_FLOWING = register("ethanol_flowing", FluidEthanol.Flowing::new);

    public static final Supplier<FlowingFluid> VEGETABLE_OIL = register("vegetable_oil", FluidVegetableOil.Source::new);
    public static final Supplier<FlowingFluid> VEGETABLE_OIL_FLOWING = register("vegetable_oil_flowing", FluidVegetableOil.Flowing::new);

    public static final Supplier<FlowingFluid> BIODIESEL = register("biodiesel", FluidBiodiesel.Source::new);
    public static final Supplier<FlowingFluid> BIODIESEL_FLOWING = register("biodiesel_flowing", FluidBiodiesel.Flowing::new);

    private static <T extends Fluid> Supplier<T> register(String name, final Supplier<T> sup) {
        return FLUIDS.register(name, sup);
    }

    private static Supplier<FluidType> registerFluidType(String name, FluidType.Properties props) {
        return FLUID_TYPES.register(name, () -> new FluidType(props));
    }

    private static FluidType.Properties standardProps(int density, int viscosity) {
        return FluidType.Properties.create()
                .density(density)
                .viscosity(viscosity)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH);
    }
}
