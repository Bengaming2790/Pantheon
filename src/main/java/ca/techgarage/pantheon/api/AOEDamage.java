package ca.techgarage.pantheon.api;

import ca.techgarage.pantheon.entity.AstrapeEntity;
import ca.techgarage.pantheon.items.weapons.Glaciera;
import ca.techgarage.pantheon.status.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AOEDamage {

    public static void applyAoeDamage(LivingEntity attacker, LivingEntity primaryTarget, float radius, float damageAmount) {
        Level world = attacker.level();

        AABB box = new AABB(
                attacker.getX() - radius, attacker.getY() - radius, attacker.getZ() - radius,
                attacker.getX() + radius, attacker.getY() + radius, attacker.getZ() + radius
        );

        List<LivingEntity> entities = world.getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> e != attacker && e != primaryTarget && e.isAlive()
        );

        for (LivingEntity entity : entities) {
            if (entity.distanceToSqr(primaryTarget) <= radius * radius) {
                entity.hurtServer(
                        (ServerLevel) primaryTarget.level(),
                        world.damageSources().playerAttack((Player) attacker),
                        damageAmount
                );
            }
        }
    }

    public static void applyAoeDamage(LivingEntity attacker, float radius, float damageAmount, boolean freeze) {
        if (!freeze) return;

        Level world = attacker.level();

        AABB box = new AABB(
                attacker.getX() - radius, attacker.getY() - radius, attacker.getZ() - radius,
                attacker.getX() + radius, attacker.getY() + radius, attacker.getZ() + radius
        );

        List<LivingEntity> entities = world.getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> e != attacker && e.isAlive()
        );

        for (LivingEntity entity : entities) {
            if (entity.distanceToSqr(attacker) <= radius * radius) {

                entity.hurtServer(
                        (ServerLevel) world,
                        world.damageSources().playerAttack((Player) attacker),
                        damageAmount
                );

                Glaciera.applyFreeze(entity);
            }
        }
    }

    public static void applyAoeDamage(LivingEntity attacker, ServerLevel world, Vec3 center, float radius, float damageAmount) {

        AABB box = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );

        List<LivingEntity> entities = world.getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> e != attacker && e.isAlive()
        );

        for (LivingEntity entity : entities) {
            if (entity.distanceToSqr(center) <= radius * radius) {
                entity.hurtServer(world, world.damageSources().playerAttack((Player) attacker), damageAmount);
            }
        }
    }
    public static void applyAoeDamage(LivingEntity attacker, ServerLevel world, boolean fire, Vec3 center, float radius, float damageAmount) {

        AABB box = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );

        List<LivingEntity> entities = world.getEntitiesOfClass(
                LivingEntity.class,
                box,
                LivingEntity::isAlive
        );

        for (LivingEntity entity : entities) {

            if (entity.distanceToSqr(center) > radius * radius) continue;

            // 🔥 Direction from explosion → entity
            Vec3 direction = entity.position().subtract(center);

            if (direction.lengthSqr() > 0) {
                direction = direction.normalize();
            }


            if (entity.equals(attacker)) {
                Vec3 bounce = direction.scale(1.5);

                entity.push(bounce.x, 0.8, bounce.z);
                entity.hurtMarked = true;

                if (entity instanceof ServerPlayer sp) {
                    sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
                }

                continue;
            }

            entity.hurtServer(
                    world,
                    world.damageSources().playerAttack((Player) attacker),
                    damageAmount
            );

            entity.igniteForTicks(20 * 3);
            Vec3 knockback = direction.scale(0.6);
            entity.push(knockback.x, 0.4, knockback.z);
        }
    }
    public static void applyAoeDamage(AstrapeEntity attacker, ServerLevel world, Vec3 center, float radius, float damageAmount, float knockbackStrength) {

        AABB box = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );

        List<LivingEntity> entities = world.getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> !e.equals(attacker.owner()) && e.isAlive()
        );

        for (LivingEntity entity : entities) {

            if (entity.distanceToSqr(center) <= radius * radius) {

                // Apply damage
                entity.hurtServer(
                        world,
                        world.damageSources().generic(),
                        damageAmount
                );

                // ---- Knockback ----
                Vec3 direction = entity.position().subtract(center);

                if (direction.lengthSqr() > 0) {
                    direction = direction.normalize();

                    Vec3 knockback = new Vec3(
                            direction.x * knockbackStrength,
                            0.4 * knockbackStrength, // slight upward boost
                            direction.z * knockbackStrength
                    );

                    entity.push(knockback.x, knockback.y, knockback.z);
                    entity.hurtMarked = true;
                    if (entity instanceof ServerPlayer serverTarget) {
                        serverTarget.connection.send(
                                new ClientboundSetEntityMotionPacket(serverTarget)
                        );
                    }
                }

                if (entity instanceof Player player && player.isCreative()) {
                    continue;
                }

                LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
                lightning.setPos(entity.getX(), entity.getY(), entity.getZ());
                world.addFreshEntity(lightning);

                entity.addEffect(
                        new MobEffectInstance(MobEffects.GLOWING, 20 * 8, 1, true, false, false),
                        entity
                );
                entity.addEffect(
                        new MobEffectInstance(ModEffects.CONDUCTING, 20 * 8, 1, true, true, false),
                        entity
                );
            }
        }
    }

    public static void applyAoeDamage(LivingEntity attacker, LivingEntity primaryTarget, float radius, float damageAmount, boolean setOnFire, int secondsOnFire) {
        Level world = attacker.level();

        AABB box = new AABB(
                attacker.getX() - radius, attacker.getY() - radius, attacker.getZ() - radius,
                attacker.getX() + radius, attacker.getY() + radius, attacker.getZ() + radius
        );

        List<LivingEntity> entities = world.getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> e != attacker && e != primaryTarget && e.isAlive()
        );

        for (LivingEntity entity : entities) {
            if (entity.distanceToSqr(primaryTarget) <= radius * radius) {
                entity.hurtServer(
                        (ServerLevel) primaryTarget.level(),
                        world.damageSources().playerAttack((Player) attacker),
                        damageAmount
                );
                if (setOnFire) {
                    entity.igniteForTicks(20 * secondsOnFire);
                }
            }
        }
    }
}