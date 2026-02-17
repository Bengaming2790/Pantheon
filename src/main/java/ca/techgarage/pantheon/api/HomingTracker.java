package ca.techgarage.pantheon.api;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;

public class HomingTracker {

    private static final int MAX_AGE = 200;
    private static final double SPEED = 1.25;
    private static final double TURN = 0.25;
    private static final double RANGE = 12.0;

    public static void attach(SnowballEntity projectile, LivingEntity owner) {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (projectile.isRemoved() || projectile.age > MAX_AGE) return;

            LivingEntity target = findTarget(world, projectile, owner);
            if (target == null) return;

            Vec3d targetPos = target.getEntityPos().add(0, target.getHeight() * 0.5, 0);
            Vec3d dir = targetPos.subtract(projectile.getEntityPos()).normalize();

            Vec3d vel = projectile.getVelocity()
                    .multiply(1.0 - TURN)
                    .add(dir.multiply(TURN))
                    .normalize()
                    .multiply(SPEED);

            projectile.setVelocity(vel);
            projectile.velocityDirty = true;
        });
    }

    private static LivingEntity findTarget(World world, Entity projectile, LivingEntity owner) {
        return world.getEntitiesByClass(
                        LivingEntity.class,
                        projectile.getBoundingBox().expand(RANGE),
                        e -> e.isAlive() && e != owner
                ).stream().min(Comparator.comparingDouble(e -> e.squaredDistanceTo(projectile)))
                .orElse(null);
    }
}
