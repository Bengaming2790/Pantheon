package ca.techgarage.pantheon.items;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class DrachmaItem extends Item implements PolymerItem {

    private static final Identifier MODEL =
            Identifier.of("pantheon", "drachma");

    public DrachmaItem(Settings settings) {
        super(settings.fireproof());
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.GOLD_NUGGET;
    }
    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        PlayerEntity player = (PlayerEntity) entity;
        if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            player.sendMessage(Text.translatable("item.anvil.rename").formatted(), true);
            stack.remove(DataComponentTypes.CUSTOM_NAME);
        }
        if (stack.contains(DataComponentTypes.ENCHANTMENTS)) {
            stack.remove(DataComponentTypes.ENCHANTMENTS);
        }
    }

    public static int countDrachma(ServerPlayerEntity player) {
        int count = 0;

        for (ItemStack stack : player.getInventory()) {
            if (stack.isOf(ModItems.DRACHMA)) {
                count += stack.getCount();
            }
        }

        return count;
    }
    public static void removeDrachmaFromInventory(ServerPlayerEntity player, int amount) {
        int remaining = amount;

        for (ItemStack stack : player.getInventory()) {
            if (stack.isOf(ModItems.DRACHMA)) {

                int remove = Math.min(stack.getCount(), remaining);
                stack.decrement(remove);
                remaining -= remove;

                if (remaining <= 0) break;
            }
        }
    }
    public static void dropDrachma(ServerPlayerEntity player, int amount) {
        ItemStack drop = new ItemStack(ModItems.DRACHMA, amount);
        player.dropItem(drop, true);
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.drachma").formatted();
    }
    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL;
    }
}
