package ca.techgarage.pantheon.blocks;

import ca.techgarage.pantheon.blocks.altar.AltarRecipe;
import ca.techgarage.pantheon.items.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;


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
        Identifier id = Identifier.fromNamespaceAndPath("pantheon", name);

        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        BlockBehaviour.Properties blockSettings = BlockBehaviour.Properties.of()
                .strength(3.5f)
                .setId(blockKey);

        Block block = new AltarBlock(blockSettings, recipe);

        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        Item.Properties itemSettings = new Item.Properties()
                .setId(itemKey);

        BlockItem blockItem = new BlockItem(block, itemSettings);

        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

        return block;
    }
}