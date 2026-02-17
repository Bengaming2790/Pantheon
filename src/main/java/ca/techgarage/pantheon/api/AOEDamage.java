package ca.techgarage.pantheon.api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class AOEDamage {

    public static void applyAoeDamage(LivingEntity attacker, LivingEntity primaryTarget, float radius, float damageAmount) {
        World world = attacker.getEntityWorld();


        Box box = new Box(
                attacker.getX() - radius, attacker.getY() - radius, attacker.getZ() - radius,
                attacker.getX() + radius, attacker.getY() + radius, attacker.getZ() + radius
        );

        List<LivingEntity> entities = world.getEntitiesByClass(
                LivingEntity.class,
                box,
                e -> e != attacker && e != primaryTarget && e.isAlive()
        );

        for (LivingEntity entity : entities) {
            if (entity.squaredDistanceTo(primaryTarget) <= radius * radius) {
                entity.damage((ServerWorld) primaryTarget.getEntityWorld(),
                        world.getDamageSources().playerAttack((PlayerEntity) attacker),
                        damageAmount
                );
            }
        }
    }
    public static void applyAoeDamage(LivingEntity attacker, ServerWorld world, Vec3d center, float radius, float damageAmount) {

        Box box = new Box(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );

        List<LivingEntity> entities = world.getEntitiesByClass(
                LivingEntity.class,
                box,
                e -> e != attacker && e.isAlive()
        );

        for (LivingEntity entity : entities) {
            if (entity.squaredDistanceTo(center) <= radius * radius) {
                entity.damage(world, world.getDamageSources().playerAttack((PlayerEntity) attacker), damageAmount);
            }
        }
    }

    public static void applyAoeDamage(LivingEntity attacker, LivingEntity primaryTarget, float radius, float damageAmount, boolean setOnFire, int secondsOnFire) {
        World world = attacker.getEntityWorld();


        Box box = new Box(
                attacker.getX() - radius, attacker.getY() - radius, attacker.getZ() - radius,
                attacker.getX() + radius, attacker.getY() + radius, attacker.getZ() + radius
        );

        List<LivingEntity> entities = world.getEntitiesByClass(
                LivingEntity.class,
                box,
                e -> e != attacker && e != primaryTarget && e.isAlive()
        );

        for (LivingEntity entity : entities) {
            if (entity.squaredDistanceTo(primaryTarget) <= radius * radius) {
                entity.damage((ServerWorld) primaryTarget.getEntityWorld(),
                        world.getDamageSources().playerAttack((PlayerEntity) attacker),
                        damageAmount
                );
                if (setOnFire) {
                    entity.setOnFireForTicks(20 * secondsOnFire);
                }
            }
        }
    }

}
