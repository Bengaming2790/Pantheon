package ca.techgarage.pantheon.blocks;

import ca.techgarage.pantheon.altar.AltarRecipe;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ModAltarBlocks {
    //Altar Block definition
    public static Block FIRE_ALTAR;


    public static void register() {

        FIRE_ALTAR = registerAltar(
                "fire_altar",
                new AltarRecipe(
                        Map.of(
                                Items.BLAZE_POWDER, 4,
                                Items.MAGMA_CREAM, 2
                        ),
                        10,
                        new ItemStack(Items.NETHER_STAR)
                )
        );

        // Example of adding more altars — just uncomment and fill in:
        //
        // WATER_ALTAR = registerAltar(
        //         "water_altar",
        //         new AltarRecipe(
        //                 Map.of(
        //                         Items.PRISMARINE_SHARD, 4,
        //                         Items.HEART_OF_THE_SEA, 1
        //                 ),
        //                 20,
        //                 new ItemStack(Items.CONDUIT)
        //         )
        // );
    }

    private static Block registerAltar(String name, AltarRecipe recipe) {

        Identifier id = Identifier.of("pantheon", name);

        RegistryKey<Block> blockKey = RegistryKey.of(Registries.BLOCK.getKey(), id);

        AbstractBlock.Settings blockSettings = AbstractBlock.Settings
                .create()
                .strength(3.5f)
                .registryKey(blockKey);

        Block block = new AltarBlock(blockSettings, recipe);
        Registry.register(Registries.BLOCK, id, block);

        RegistryKey<Item> itemKey = RegistryKey.of(Registries.ITEM.getKey(), id);

        Item.Settings itemSettings = new Item.Settings()
                .registryKey(itemKey);

        AltarBlockItem blockItem = new AltarBlockItem(block, itemSettings);
        Registry.register(Registries.ITEM, id, blockItem);

        return block;
    }
}