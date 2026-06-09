package ca.techgarage.pantheon.items;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AltarModelItem extends Item implements PolymerItem {

    public AltarModelItem(Properties properties) {
        super(properties);
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.PAPER;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return Identifier.fromNamespaceAndPath("pantheon", "altar");
    }
}