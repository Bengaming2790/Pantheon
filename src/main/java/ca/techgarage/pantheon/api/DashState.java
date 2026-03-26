package ca.techgarage.pantheon.api;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class DashState {

    public static final Map<UUID, ParticleCast> DASH_TICKS = new HashMap<>();

    public static void start(ServerPlayer player, int ticks, ParticleOptions particle) {
        DASH_TICKS.put(player.getUUID(), new ParticleCast(ticks, particle));
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, ParticleCast>> it = DASH_TICKS.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, ParticleCast> entry = it.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

            if (player == null) {
                it.remove();
                continue;
            }

            ParticleCast data = entry.getValue();

            if (data.ticks <= 0) {
                it.remove();
                continue;
            }

            ServerLevel world = (ServerLevel) player.level();

            world.sendParticles(
                    data.particle,
                    player.getX(),
                    player.getY() + 0.5,
                    player.getZ(),
                    80,
                    1,
                    1,
                    1,
                    0.0
            );

            data.ticks--;
        }
    }

    public static class ParticleCast {
        int ticks;
        final ParticleOptions particle;

        ParticleCast(int ticks, ParticleOptions particle) {
            this.ticks = ticks;
            this.particle = particle;
        }
    }
}