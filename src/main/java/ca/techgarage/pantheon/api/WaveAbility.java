package ca.techgarage.pantheon.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class WaveAbility {

    private static final int WAVE_STEPS = 20; // total expansion steps
    private static final int PARTICLE_PER_CIRCLE = 36;
    private static final int LAYERS = 3;
    private static final double HEIGHT_STEP = 0.25;

    public static void summonWave(ServerPlayer player) {
        ServerLevel world = player.level();
        ServerLevel serverWorld = world;

        Vec3 center = player.position();
        double radius = 6.0;
        double knockback = 1.5;
        int slownessDuration = 5 * 20;

        // Play sound once
        world.playSound(null, center.x, center.y, center.z,
                SoundEvents.BLAZE_SHOOT,
                SoundSource.PLAYERS,
                1.0f,
                1.5f);

        // Register a "wave step" counter in INT_DATA

        spawnParticles(player, player.level());
        // Each tick, call tickWave(player, serverWorld, center)
    }

    /** Call this on every server tick for players with active wave */
    public static void spawnParticles(ServerPlayer player, ServerLevel world) {



        Vec3 center = player.position();
        double radius = 3.5;

        double knockback = 2;
        int slownessDuration = 2;

        // Spawn particles
        for (int y = 0; y < LAYERS; y++) {
            double height = center.y + 0.5 + y * HEIGHT_STEP;
            for (int i = 0; i < PARTICLE_PER_CIRCLE; i++) {
                double angle = 2 * Math.PI * i / PARTICLE_PER_CIRCLE;
                double x = center.x + radius * Math.cos(angle);
                double z = center.z + radius * Math.sin(angle);

                world.sendParticles(
                        ParticleTypes.SWEEP_ATTACK,
                        x, height, z,
                        1, 0, 0, 0, 0
                );
            }
        }

        // Apply effect to entities in radius
        AABB area = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );
        for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && e.isAttackable())) {

            Vec3 direction = entity.position().subtract(center).normalize().scale(knockback);
            entity.setDeltaMovement(direction.x, 0.5, direction.z);
            entity.hurtMarked = true;

            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, slownessDuration, 1));
            entity.hurt(player.damageSources().playerAttack(player), 6);
        }

    }
}