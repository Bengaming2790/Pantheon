package ca.techgarage.pantheon.api;

import ca.techgarage.pantheon.items.weapons.Glaciera;
import ca.techgarage.pantheon.status.ModEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
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

    public static void applyAoeDamage(LivingEntity attacker, float radius, float damageAmount, boolean freeze) {
        if (!freeze) return;

        World world = attacker.getEntityWorld();

        Box box = new Box(
                attacker.getX() - radius, attacker.getY() - radius, attacker.getZ() - radius,
                attacker.getX() + radius, attacker.getY() + radius, attacker.getZ() + radius
        );

        List<LivingEntity> entities = world.getEntitiesByClass(
                LivingEntity.class,
                box,
                e -> e != attacker && e.isAlive()
        );

        for (LivingEntity entity : entities) {
            if (entity.squaredDistanceTo(attacker) <= radius * radius) {

                entity.damage(
                        (ServerWorld) world,
                        world.getDamageSources().playerAttack((PlayerEntity) attacker),
                        damageAmount
                );

                Glaciera.applyFreeze(entity);
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
    public static void applyAoeDamage(LivingEntity attacker, ServerWorld world, Vec3d center, float radius, float damageAmount, float knockbackStrength ) {

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

                // Apply damage
                entity.damage(
                        world,
                        world.getDamageSources().playerAttack((PlayerEntity) attacker),
                        damageAmount
                );

                // ---- Knockback ----
                Vec3d direction = entity.getEntityPos().subtract(center);

                if (direction.lengthSquared() > 0) {
                    direction = direction.normalize();

                    Vec3d knockback = new Vec3d(
                            direction.x * knockbackStrength,
                            0.4 * knockbackStrength, // slight upward boost
                            direction.z * knockbackStrength
                    );

                    entity.addVelocity(knockback.x, knockback.y, knockback.z);
                    entity.velocityDirty = true;
                    if (entity instanceof ServerPlayerEntity serverTarget) {
                        serverTarget.networkHandler.sendPacket(
                                new EntityVelocityUpdateS2CPacket(serverTarget)
                        );
                    }
                }

                if (entity instanceof PlayerEntity player && player != attacker) {
                    continue;
                }

                LightningEntity lightning =
                        new LightningEntity(EntityType.LIGHTNING_BOLT, entity.getEntityWorld());

                lightning.setPosition(entity.getX(), entity.getY(), entity.getZ());
                entity.getEntityWorld().spawnEntity(lightning);
                entity.setStatusEffect(
                        new StatusEffectInstance(StatusEffects.GLOWING, 20 * 8, 1, true, false, false),
                        entity
                );
                entity.setStatusEffect(
                        new StatusEffectInstance(ModEffects.CONDUCTING, 20 * 8, 1, true, true, false),
                        entity
                );



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
