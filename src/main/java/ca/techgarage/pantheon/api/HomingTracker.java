package ca.techgarage.pantheon.api;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import java.util.Comparator;

public class HomingTracker {

    private static final int MAX_AGE = 200;
    private static final double SPEED = 1.25;
    private static final double TURN = 0.35;
    private static final double RANGE = 12.0;

    /**
     * Note: In a production mod, it is better to handle this via a Mixin
     * in Snowball#tick or a TickEvent that iterates projectiles rather
     * than registering a new event listener per snowball.
     */
    public static void attach(Snowball projectile, LivingEntity owner) {
        ServerTickEvents.END_WORLD_TICK.register(level -> {
            if (projectile.isRemoved() || projectile.tickCount > MAX_AGE) return;

            LivingEntity target = findTarget((ServerLevel) level, projectile, owner);
            if (target == null) return;

            // target.getEyePosition() is the Mojang equivalent for centering on the head/chest
            Vec3 targetPos = target.getEyePosition(1.0f);
            Vec3 currentPos = projectile.position();
            Vec3 dir = targetPos.subtract(currentPos).normalize();

            // getDeltaMovement() is the Mojang equivalent for getVelocity()
            Vec3 vel = projectile.getDeltaMovement()
                    .scale(1.0 - TURN)
                    .add(dir.scale(TURN))
                    .normalize()
                    .scale(SPEED);

            projectile.setDeltaMovement(vel);
        });
    }

    private static LivingEntity findTarget(ServerLevel level, Entity projectile, LivingEntity owner) {
        return level.getEntitiesOfClass(
                        LivingEntity.class,
                        projectile.getBoundingBox().inflate(RANGE),
                        e -> e.isAlive() && e != owner
                ).stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(projectile)))
                .orElse(null);
    }
}