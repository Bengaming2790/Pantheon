package ca.techgarage.pantheon.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import ca.techgarage.pantheon.database.BanManager;

import java.util.Collection;
import java.util.UUID;

public class TempBanRemoveCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(
                CommandManager.literal("tempbanremove")
                        .requires(Permissions.require("pantheon.tempban"))
                        .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                                .executes(TempBanRemoveCommand::execute)
                        )
        );
    }

    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        Collection<PlayerConfigEntry> profiles =
                GameProfileArgumentType.getProfileArgument(context, "player");

        for (PlayerConfigEntry profile : profiles) {
            UUID uuid = profile.id();
            String name = profile.name();

            BanManager.remove(uuid);

            context.getSource().sendFeedback(
                    () -> Text.literal("Removed temp ban for " + name),
                    true
            );
        }

        return profiles.size();
    }
}