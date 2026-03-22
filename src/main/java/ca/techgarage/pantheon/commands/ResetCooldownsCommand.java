package ca.techgarage.pantheon.commands;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.database.BanManager;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ResetCooldownsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("resetcooldown")
                        .requires(Permissions.require("pantheon.debug"))
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    Cooldowns.clearAll(target);
                                    target.sendMessage(Text.literal("Cooldowns reset!"));

                                    return 1;
                                }))
        );
    }
}
