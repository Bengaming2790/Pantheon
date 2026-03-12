package ca.techgarage.pantheon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import ca.techgarage.pantheon.database.BanManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TempBanCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(
                CommandManager.literal("tempban")
                        .requires(Permissions.require("pantheon.tempban"))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("days", IntegerArgumentType.integer(0))
                                        .then(CommandManager.argument("hours", IntegerArgumentType.integer(0))
                                                .then(CommandManager.argument("minutes", IntegerArgumentType.integer(0))
                                                        .then(CommandManager.argument("seconds", IntegerArgumentType.integer(0))
                                                              .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                                                                .executes(ctx -> {

                                                                    ServerPlayerEntity target =
                                                                            EntityArgumentType.getPlayer(ctx, "player");

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
                                                                        ctx.getSource().sendError(
                                                                                Text.literal("Duration must be greater than 0."));
                                                                        return 0;
                                                                    }

                                                                    BanManager.ban(target.getUuid(), target.getName().toString(), duration, reason);

                                                                    target.networkHandler.disconnect(
                                                                            Text.literal("You have been temporarily banned. \n For: " + reason)
                                                                    );

                                                                    ctx.getSource().sendFeedback(
                                                                            () -> Text.literal("Temporarily banned "
                                                                                    + target.getName().getString()),
                                                                            true
                                                                    );

                                                                    return 1;
                                                                }))))))));
    }
}
