package ca.techgarage.pantheon.blocks;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.packettweaker.PacketContext;

public class AltarBlockItem extends BlockItem implements PolymerItem {

    public AltarBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        // What the client sees in the player's hand / inventory
        return Items.ENCHANTING_TABLE;
    }

}