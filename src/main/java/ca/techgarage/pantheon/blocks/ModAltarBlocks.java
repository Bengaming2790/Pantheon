package ca.techgarage.pantheon.blocks;

import ca.techgarage.pantheon.blocks.altar.AltarRecipe;
import ca.techgarage.pantheon.items.ModItems;
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
    public static Block KHALKEUS_ALTAR;
    public static Block AEGIS_ALTAR;

    public static void register() {

        FIRE_ALTAR = registerAltar(
                "fire_altar",
                new AltarRecipe(
                        Map.of(
                                Items.BLAZE_POWDER, 4,
                                Items.MAGMA_CREAM, 2
                        ),
                        10,
                        new ItemStack(Items.NETHER_STAR),
                        AltarRecipe.DEFAULT_ITEM_HEIGHT,
                        AltarRecipe.DEFAULT_TEXT_Y_START,
                        AltarRecipe.DEFAULT_TEXT_Y_STEP
                )
        );

        KHALKEUS_ALTAR = registerAltar(
                "khalkeus_altar",
                new AltarRecipe(
                        Map.of(
                                ModItems.DRACHMA, 75,
                                Items.NETHERITE_INGOT, 3,
                                Items.BLAZE_ROD, 32,
                                Items.HEAVY_CORE, 1,
                                Items.ANVIL, 16
                        ),

                        10,
                        new ItemStack(ModItems.KHALKEOUS),
                        AltarRecipe.DEFAULT_ITEM_HEIGHT,
                        4,
                        AltarRecipe.DEFAULT_TEXT_Y_STEP
                )
        );

        AEGIS_ALTAR = registerAltar(
                "aegis_altar",
                new AltarRecipe(
                        Map.of(
                                ModItems.DRACHMA, 75,
                                Items.GOLD_BLOCK, 32,
                                Items.IRON_BLOCK, 64,
                                Items.FERMENTED_SPIDER_EYE, 16,
                                Items.ENCHANTED_GOLDEN_APPLE, 1
                        ),
                        15,
                        new ItemStack(ModItems.AEGIS),
                        AltarRecipe.DEFAULT_ITEM_HEIGHT,
                        AltarRecipe.DEFAULT_TEXT_Y_START,
                        AltarRecipe.DEFAULT_TEXT_Y_STEP
                )
        );

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