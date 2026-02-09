// java
package ca.techgarage.pantheon.items;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import xyz.nucleoid.packettweaker.PacketContext;

public class DrachmaItem extends Item implements PolymerItem {

    public DrachmaItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.GOLD_NUGGET;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack stack, TooltipType tooltipType, PacketContext context) {
        return PolymerItem.super.getPolymerItemStack(stack, tooltipType, context);
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.drachma");
    }
}