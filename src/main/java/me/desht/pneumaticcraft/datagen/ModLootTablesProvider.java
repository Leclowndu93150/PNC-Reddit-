package me.desht.pneumaticcraft.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.datagen.loot.TileEntitySerializerFunction;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.functions.CopyName;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModLootTablesProvider extends LootTableProvider {

    public ModLootTablesProvider(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return ImmutableList.of(Pair.of(BlockLootTablePNC::new, LootParameterSets.BLOCK));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationResults validationresults) {
        // ...
    }

    private static class BlockLootTablePNC extends BlockLootTables {
        @Override
        protected void addTables() {
            for (Block b : ModBlocks.Registration.ALL_BLOCKS) {
                if (b instanceof BlockPneumaticCraft && b.hasTileEntity(b.getDefaultState()) && ForgeRegistries.ITEMS.containsKey(b.getRegistryName())) {
                    addStandardSerializedDrop(b);
                }
            }

//            registerDropping(ModBlocks.FAKE_ICE, Blocks.AIR);
//            registerDropping(ModBlocks.KEROSENE_LAMP_LIGHT, Blocks.AIR);
//            registerDropping(ModBlocks.DRONE_REDSTONE_EMITTER, Blocks.AIR);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            List<Block> l = new ArrayList<>();
            for (Block b : ModBlocks.Registration.ALL_BLOCKS) {
                if (ForgeRegistries.ITEMS.containsKey(b.getRegistryName())) {
                    l.add(b);
                }
            }
            return l;
        }

        private void addStandardSerializedDrop(Block block) {
            LootPool.Builder builder = LootPool.builder()
                    .name(block.getRegistryName().getPath())
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(block)
                            .acceptFunction(CopyName.builder(CopyName.Source.BLOCK_ENTITY))
                            .acceptFunction(TileEntitySerializerFunction.builder()));
            registerLootTable(block, LootTable.builder().addLootPool(builder));
        }

    }
}