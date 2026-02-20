package ca.techgarage.pantheon.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Dash {

    /**
     * Dashes the player in the direction they are looking. Credit: Fabridash-resurrected by ninem4re for math and general movement logic https://github.com/ninem4re/Fabridash-resurrected
     */
    public static void dashForward(PlayerEntity player, float power) {
        float yaw = player.getYaw();
        float pitch = player.getPitch();

        // Same math as Fabridash
        float x = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        float y = -MathHelper.sin(pitch * 0.017453292F);
        float z =  MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);

        float magnitude = MathHelper.sqrt(x * x + y * y + z * z);
        float strength = 3.0F * ((1.0F + power) / 4.0F);

        Vec3d velocity = new Vec3d(
                x / magnitude * strength,
                y / magnitude * strength,
                z / magnitude * strength
        );

        player.addVelocity(velocity);

        player.fallDistance = 0;
        player.velocityDirty = true;

        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
        }
    }

}
