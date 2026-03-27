package ca.techgarage.pantheon.api;

import ca.techgarage.pantheon.items.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.Set;

public class InventoryBlocker {

    private static Set<Item> BLOCKED_ITEMS;

    public static void register() {

        BLOCKED_ITEMS = ModItems.getAllItems();

        ServerTickEvents.END_SERVER_TICK.register(server -> {

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {

                AbstractContainerMenu menu = player.containerMenu;
                if (menu == null) continue;

                // Only block vanilla storage containers
                // Note: Checking against player.inventoryMenu ensures we aren't blocking the player's own inventory screen
                if (!(menu instanceof ChestMenu
                        || menu instanceof ShulkerBoxMenu
                        || menu instanceof HopperMenu
                        || menu instanceof FurnaceMenu
                        || menu instanceof BlastFurnaceMenu
                        || menu instanceof SmokerMenu
                        || menu instanceof BrewingStandMenu
                        || menu instanceof AnvilMenu
                        || menu instanceof CartographyTableMenu
                        || menu instanceof GrindstoneMenu
                        || menu instanceof StonecutterMenu
                        || menu instanceof SmithingMenu
                        || menu instanceof CrafterMenu
                        || menu instanceof HorseInventoryMenu
                )) {
                    continue;
                }

                for (Slot slot : menu.slots) {

                    // Skip player inventory slots
                    if (slot.container == player.getInventory()) continue;

                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;

                    if (BLOCKED_ITEMS.contains(stack.getItem())) {
                        player.sendSystemMessage(Component.translatable("item.anvil.rename").withStyle(style -> style.withBold(true)), true);
                        slot.set(ItemStack.EMPTY);
                        player.getInventory().placeItemBackInInventory(stack);
                    }
                }
            }
        });
    }
}