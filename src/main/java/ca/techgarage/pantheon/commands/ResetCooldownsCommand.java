package ca.techgarage.pantheon.commands;

import ca.techgarage.pantheon.api.Cooldowns;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ResetCooldownsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("resetcooldown")
                        // Permissions API usually uses the CommandSourceStack
                        .requires(source -> Permissions.check(source, "pantheon.debug", 2))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                                    // Assuming your Cooldowns API is updated to use ServerPlayer
                                    Cooldowns.clearAll(target);

                                    // Sending a system message is the modern way to notify players of command results
                                    target.sendSystemMessage(Component.literal("Cooldowns Reset!"));

                                    // Optional: Notify the sender as well
                                    ctx.getSource().sendSuccess(() -> Component.literal("Reset cooldowns for " + target.getScoreboardName()), true);

                                    return 1;
                                }))
        );
    }
}