package ca.techgarage.pantheon.api;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import java.util.Comparator;

public class HomingTracker {

    private static final double SPEED = 1.5;
    private static final double TURN = 0.25;       // lower = smoother arc, higher = snappier
    private static final double RANGE = 24.0;
    private static final double LOCK_RANGE = 2.5;  // teleport-correct if this close and about to miss

    public static void attach(Snowball projectile, LivingEntity owner) {
        // Lock onto the nearest target at launch, don't re-acquire
        final LivingEntity[] lockedTarget = {null};

        ServerTickEvents.END_WORLD_TICK.register(level -> {
            if (projectile.isRemoved()) return;

            ServerLevel serverLevel = (ServerLevel) level;

            // Acquire lock on first tick
            if (lockedTarget[0] == null || !lockedTarget[0].isAlive()) {
                lockedTarget[0] = findTarget(serverLevel, projectile, owner);
            }

            LivingEntity target = lockedTarget[0];
            if (target == null || !target.isAlive()) return;

            Vec3 targetPos = target.getEyePosition(1.0f);
            Vec3 currentPos = projectile.position();
            Vec3 toTarget = targetPos.subtract(currentPos);
            double dist = toTarget.length();

            if (dist < LOCK_RANGE) {
                projectile.setDeltaMovement(toTarget.normalize().scale(SPEED));
                return;
            }

            // Predict where target will be next tick based on their velocity
            Vec3 targetVelocity = target.getDeltaMovement();
            Vec3 predictedPos = targetPos.add(targetVelocity);
            Vec3 dir = predictedPos.subtract(currentPos).normalize();

            // Blend current velocity toward target direction
            Vec3 vel = projectile.getDeltaMovement()
                    .normalize()
                    .scale(1.0 - TURN)
                    .add(dir.scale(TURN))
                    .normalize()
                    .scale(SPEED);

            projectile.setDeltaMovement(vel);
            projectile.setNoGravity(true); // prevent gravity from pulling it off course
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