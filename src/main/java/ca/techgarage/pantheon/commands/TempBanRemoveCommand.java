package ca.techgarage.pantheon.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import ca.techgarage.pantheon.database.BanManager;
import net.minecraft.server.players.NameAndId;

import java.util.Collection;
import java.util.UUID;

public class TempBanRemoveCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tempbanremove")
                        .requires(Permissions.require("pantheon.tempban", 2))
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes(TempBanRemoveCommand::execute)
                        )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<NameAndId> profiles = GameProfileArgument.getGameProfiles(context, "player");

        for (NameAndId profile : profiles) {
            UUID uuid = profile.id();
            String name = profile.name();

            BanManager.remove(uuid);

            context.getSource().sendSuccess(
                    () -> Component.literal("Removed temp ban for " + name),
                    true
            );
        }

        return profiles.size();
    }
}