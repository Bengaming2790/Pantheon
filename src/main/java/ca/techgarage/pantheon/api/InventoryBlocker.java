package ca.techgarage.pantheon.api;

import ca.techgarage.pantheon.items.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Set;

public class InventoryBlocker {

    private static Set<Item> BLOCKED_ITEMS;

    public static void register() {

        BLOCKED_ITEMS = ModItems.getAllItems();

        ServerTickEvents.END_SERVER_TICK.register(server -> {

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

                ScreenHandler handler = player.currentScreenHandler;
                if (handler == null) continue;

                // Only block vanilla storage containers
                if (!(handler instanceof GenericContainerScreenHandler
                        || handler instanceof ShulkerBoxScreenHandler
                        || handler instanceof HopperScreenHandler
                        || handler instanceof FurnaceScreenHandler
                        || handler instanceof BlastFurnaceScreenHandler
                        || handler instanceof SmokerScreenHandler
                        || handler instanceof BrewingStandScreenHandler
                        || handler instanceof AnvilScreenHandler
                        || handler instanceof CartographyTableScreenHandler
                        || handler instanceof GrindstoneScreenHandler
                        || handler instanceof StonecutterScreenHandler
                        || handler instanceof SmithingScreenHandler
                        || handler instanceof CrafterScreenHandler
                        )) {
                    continue;
                }

                for (Slot slot : handler.slots) {

                    // Skip player inventory slots
                    if (slot.inventory == player.getInventory()) continue;

                    ItemStack stack = slot.getStack();
                    if (stack.isEmpty()) continue;

                    if (BLOCKED_ITEMS.contains(stack.getItem())) {
                        player.sendMessage(Text.translatable("item.anvil.rename").formatted(), true);
                        slot.setStack(ItemStack.EMPTY);
                        player.getInventory().offerOrDrop(stack);
                    }
                }
            }
        });
    }
}