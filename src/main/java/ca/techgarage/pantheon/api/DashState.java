package ca.techgarage.pantheon.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class DashState {

    // UUID -> (ticks left + particle)
    public static final Map<UUID, ParticleCast> DASH_TICKS = new HashMap<>();

    /** Call this from Peitho / items / abilities */
    public static void start(ServerPlayerEntity player, int ticks, ParticleEffect particle) {
        DASH_TICKS.put(player.getUuid(), new ParticleCast(ticks, particle));
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, ParticleCast>> it = DASH_TICKS.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, ParticleCast> entry = it.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());

            if (player == null) {
                it.remove();
                continue;
            }

            ParticleCast data = entry.getValue();

            if (data.ticks <= 0) {
                it.remove();
                continue;
            }

            ServerWorld world = player.getEntityWorld();

            world.spawnParticles(
                    data.particle,
                    player.getX(),
                    player.getY() + 0.5,
                    player.getZ(),
                    30,
                    0.2,
                    0.25,
                    0.2,
                    0.0
            );

            data.ticks--;
        }
    }

    public static class ParticleCast {
        int ticks;
        final ParticleEffect particle;

        ParticleCast(int ticks, ParticleEffect particle) {
            this.ticks = ticks;
            this.particle = particle;
        }
    }
}
