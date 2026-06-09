package ca.techgarage.pantheon.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class HomingTracker {

    private static final double SPEED = 0.8;
    private static final double TURN = 0.25;
    private static final double RANGE = 24.0;
    private static final double LOCK_RANGE = 2.5;

    private record HomingEntry(Snowball projectile, LivingEntity owner, LivingEntity[] lockedTarget) {}
    private static final Set<HomingEntry> active = new HashSet<>();
    private static boolean registered = false;

    public static void attach(Snowball projectile, LivingEntity owner) {
        projectile.setNoGravity(true); // disable gravity immediately on spawn

        active.add(new HomingEntry(projectile, owner, new LivingEntity[]{null}));

        if (!registered) {
            registered = true;
            ServerTickEvents.END_SERVER_TICK.register(server -> {
                active.removeIf(entry -> {
                    Snowball ball = entry.projectile();
                    if (ball.isRemoved()) return true;

                    // Use the level the projectile is actually in
                    if (!(ball.level() instanceof ServerLevel level)) return true;

                    LivingEntity[] lockedTarget = entry.lockedTarget();
                    if (lockedTarget[0] == null || !lockedTarget[0].isAlive()) {
                        lockedTarget[0] = findTarget(level, ball, entry.owner());
                    }

                    LivingEntity target = lockedTarget[0];
                    if (target == null || !target.isAlive()) return false;

                    Vec3 targetPos = target.getEyePosition(1.0f);
                    Vec3 currentPos = ball.position();
                    Vec3 toTarget = targetPos.subtract(currentPos);
                    double dist = toTarget.length();

                    if (dist < LOCK_RANGE) {
                        ball.setDeltaMovement(toTarget.normalize().scale(SPEED));
                        ball.hurtMarked = true;
                    } else {
                        Vec3 predicted = targetPos.add(target.getDeltaMovement());
                        Vec3 dir = predicted.subtract(currentPos).normalize();
                        Vec3 vel = ball.getDeltaMovement()
                                .normalize()
                                .scale(1.0 - TURN)
                                .add(dir.scale(TURN))
                                .normalize()
                                .scale(SPEED);
                        ball.setDeltaMovement(vel);
                        ball.hurtMarked = true;
                    }

                    return false;
                });
            });
        }
    }

    private static LivingEntity findTarget(ServerLevel level, Snowball projectile, LivingEntity owner) {
        return level.getEntitiesOfClass(
                        LivingEntity.class,
                        projectile.getBoundingBox().inflate(RANGE),
                        e -> e.isAlive() && e != owner
                ).stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(projectile)))
                .orElse(null);
    }
}