package ca.techgarage.pantheon.api;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class Dash {

    /**
     * Dashes the player in the direction they are looking. Credit: Fabridash-resurrected by ninem4re for math and general movement logic https://github.com/ninem4re/Fabridash-resurrected
     */
    public static void dashForward(Player player, float power) {
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        // Same math as Fabridash
        float x = -Mth.sin(yaw * 0.017453292F) * Mth.cos(pitch * 0.017453292F);
        float y = -Mth.sin(pitch * 0.017453292F);
        float z =  Mth.cos(yaw * 0.017453292F) * Mth.cos(pitch * 0.017453292F);

        float magnitude = Mth.sqrt(x * x + y * y + z * z);
        float strength = 3.0F * ((1.0F + power) / 4.0F);

        Vec3 velocity = new Vec3(
                x / magnitude * strength,
                y / magnitude * strength,
                z / magnitude * strength
        );

        player.push(velocity.x, velocity.y, velocity.z);

        player.fallDistance = 0;
        player.hurtMarked = true;

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
    }

}