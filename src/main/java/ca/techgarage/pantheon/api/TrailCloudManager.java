package ca.techgarage.pantheon.api;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class TrailCloudManager {

    public static final List<TrailCloud> CLOUDS = new ArrayList<>();

    public static void spawn(ServerLevel level, Vec3 pos, LivingEntity owner) {
        CLOUDS.add(new TrailCloud(level, pos, owner));
    }

    public static void tick(MinecraftServer server) {
        Iterator<TrailCloud> it = CLOUDS.iterator();

        while (it.hasNext()) {
            TrailCloud cloud = it.next();

            if (!cloud.tick()) {
                it.remove();
            }
        }
    }

    public static class TrailCloud {
        private final ServerLevel level;
        private final Vec3 pos;
        private final LivingEntity owner;

        private int ticksLeft = 60;
        private final double radius = 1.5;
        private final float damage = 3.0f;

        private final Map<UUID, Integer> hitCooldowns = new HashMap<>();

        public TrailCloud(ServerLevel level, Vec3 pos, LivingEntity owner) {
            this.level = level;
            this.pos = pos;
            this.owner = owner;
        }

        public boolean tick() {
            if (ticksLeft-- <= 0) return false;

            level.sendParticles(
                    ParticleTypes.RAID_OMEN,
                    pos.x,
                    pos.y + 0.1,
                    pos.z,
                    6,
                    0.3,
                    0.1,
                    0.3,
                    0.01
            );

            hitCooldowns.replaceAll((uuid, time) -> time - 1);
            hitCooldowns.values().removeIf(v -> v <= 0);

            List<LivingEntity> entities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(
                            pos.x - radius, pos.y - 1, pos.z - radius,
                            pos.x + radius, pos.y + 2, pos.z + radius
                    )
            );

            for (LivingEntity entity : entities) {
                if (entity == owner) continue;

                UUID id = entity.getUUID();

                if (hitCooldowns.containsKey(id)) continue;

                entity.hurt(level.damageSources().magic(), damage);

                entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.1, 0));

                hitCooldowns.put(id, 10);
            }

            return true;
        }
    }
}