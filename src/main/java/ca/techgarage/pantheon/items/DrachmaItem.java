package ca.techgarage.pantheon.items;

import ca.techgarage.pantheon.database.BankDatabase;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import xyz.nucleoid.packettweaker.PacketContext;

public class DrachmaItem extends Item implements PolymerItem {

    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath("pantheon", "drachma");

    public DrachmaItem(Properties properties) {
        super(properties.fireResistant());
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.GOLD_NUGGET;
    }


    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        ItemStack stackInHand = user.getItemInHand(hand);
        int drachmaInHand = stackInHand.getCount();

        if (user instanceof ServerPlayer serverPlayer) {
            BankDatabase.add(user.getUUID(), drachmaInHand);
            removeDrachmaFromInventory(serverPlayer, drachmaInHand);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static int countDrachma(ServerPlayer player) {
        int count = 0;
        // In MojMap, inventory.items is the main list
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(ModItems.DRACHMA)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static void removeDrachmaFromInventory(ServerPlayer player, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(ModItems.DRACHMA)) {
                int remove = Math.min(stack.getCount(), remaining);
                stack.shrink(remove); // shrink is the MojMap version of decrement
                remaining -= remove;
                if (remaining <= 0) break;
            }
        }
    }

    public static void dropDrachma(ServerPlayer player, int amount) {
        ItemStack drop = new ItemStack(ModItems.DRACHMA, amount);
        player.drop(drop, true);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.pantheon.drachma");
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL;
    }
}