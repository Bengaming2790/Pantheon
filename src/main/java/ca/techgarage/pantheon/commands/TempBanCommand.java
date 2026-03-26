package ca.techgarage.pantheon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import ca.techgarage.pantheon.database.BanManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;


import net.minecraft.server.level.ServerPlayer;

public class TempBanCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("tempban")
                        .requires(Permissions.require("pantheon.tempban"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("days", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("hours", IntegerArgumentType.integer(0))
                                                .then(Commands.argument("minutes", IntegerArgumentType.integer(0))
                                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                                              .then(Commands.argument("reason", StringArgumentType.greedyString())
                                                                .executes(ctx -> {

                                                                    ServerPlayer target =
                                                                            EntityArgument.getPlayer(ctx, "player");

                                                                    int days = IntegerArgumentType.getInteger(ctx, "days");
                                                                    int hours = IntegerArgumentType.getInteger(ctx, "hours");
                                                                    int minutes = IntegerArgumentType.getInteger(ctx, "minutes");
                                                                    int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
                                                                    String reason = StringArgumentType.getString(ctx, "reason");

                                                                    long duration =
                                                                            days * 86400000L +
                                                                                    hours * 3600000L +
                                                                                    minutes * 60000L +
                                                                                    seconds * 1000L;

                                                                    if (duration <= 0) {
                                                                        ctx.getSource().sendFailure(
                                                                                Component.literal("Duration must be greater than 0."));
                                                                        return 0;
                                                                    }

                                                                    BanManager.ban(target.getUUID(), target.getName().toString(), duration, reason);

                                                                    target.connection.disconnect(
                                                                            Component.literal("You have been temporarily banned. \n For: " + reason)
                                                                    );

                                                                    ctx.getSource().sendSuccess(
                                                                            () -> Component.literal("Temporarily banned "
                                                                                    + target.getName().getString()),
                                                                            true
                                                                    );

                                                                    return 1;
                                                                }))))))));
    }
}
