package ca.techgarage.pantheon.commands;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.database.LegendaryDatabase;
import ca.techgarage.pantheon.items.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class PantheonCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("pantheon")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal("\n§a--Pantheon Legenday Weapons--"), false);
                            for (Item item : ModItems.getAllItems()) {
                                if (item == ModItems.DRACHMA
                                        || item == ModItems.ALTAR_MODEL)
                                    continue;

                                String holder =
                                        LegendaryDatabase.getHolder(item);

                                ctx.getSource().sendSuccess(
                                        () -> Component.literal(
                                                item.getName(
                                                        new ItemStack(item)
                                                ).getString()
                                                        + " §r- "
                                                        + holder
                                        ),
                                        false
                                );
                            }

                            return 1;
                        })
        );
    }
}
