package ca.techgarage.pantheon.blocks;

import eu.pb4.polymer.core.api.item.PolymerItem;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class AltarBlockItem extends BlockItem implements PolymerItem {

    public AltarBlockItem(Block block, Item.Properties settings) {
        super(block, settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        // What the client sees in the player's hand / inventory
        return Items.ENCHANTING_TABLE;
    }

}