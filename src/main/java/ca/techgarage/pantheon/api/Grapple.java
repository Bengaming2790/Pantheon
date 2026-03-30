package ca.techgarage.pantheon.api;

import ca.techgarage.pantheon.items.weapons.Kynthia;
import ca.techgarage.pantheon.status.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Grapple {

    public static boolean isAimingDown(Player player) {
        return player.getXRot() > 80.0F;
    }



    public static void bouncePlayer(Player player, double amount) {
        Vec3 vel = player.getDeltaMovement();
        player.setDeltaMovement(vel.x * 0.3, amount, vel.z * 0.3);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
    }

    public static void spawnChainParticles(ServerLevel world, Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        int points = 128;
        for (int i = 0; i <= points; i++) {
            Vec3 pos = from.add(delta.scale(i / (double) points));
            world.sendParticles(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    public static void pullPlayer(Player player, Vec3 target) {
        Vec3 eyePos = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 delta = target.subtract(eyePos);

        double distance = delta.length();
        double speed = Math.min(4.0, Math.max(1.8, distance * 0.55));
        Vec3 pullVelocity = delta.normalize().scale(speed);
        pullVelocity = new Vec3(pullVelocity.x, pullVelocity.y * 0.45, pullVelocity.z);

        // Strip potion effect contribution from current velocity
        Vec3 rawVel = player.getDeltaMovement();
        double speedBoost = 0;
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.SPEED)) {
            int amp = player.getEffect(net.minecraft.world.effect.MobEffects.SPEED).getAmplifier();
            speedBoost = (amp + 1) * 0.2; // vanilla adds ~20% per level
        }
        double slowness = 0;
        if (player.hasEffect(MobEffects.SLOWNESS)) {
            int amp = player.getEffect(MobEffects.SLOWNESS).getAmplifier();
            slowness = (amp + 1) * 0.15;
        }
        double effectScale = 1.0 / Math.max(0.1, 1.0 + speedBoost - slowness);
        Vec3 currentVel = rawVel.scale(effectScale); // normalize out potion influence

        Vec3 pullDir = delta.normalize();
        double dot = currentVel.dot(pullDir);

        double resistanceFactor = Math.max(0.5, 1.0 - (Math.abs(dot) * 0.3));
        pullVelocity = pullVelocity.scale(resistanceFactor);

        Vec3 finalVelocity = currentVel.scale(0.25).add(pullVelocity.scale(0.75));

        player.setDeltaMovement(finalVelocity);
        player.hurtMarked = true;
        player.fallDistance = 0;

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
        }
    }

    public static void pullEntity(Player player, Entity target) {
        Vec3 playerEye = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 forward = player.getViewVector(1.0F).normalize();
        Vec3 destination = playerEye.add(forward.scale(2.0));

        Vec3 targetEye = target.position().add(0, target.getEyeHeight(), 0);
        Vec3 delta = destination.subtract(targetEye);

        double distance = delta.length();
        double speed = Math.max(2.0, distance * 0.5);
        Vec3 pullVelocity = delta.normalize().scale(speed);
        pullVelocity = new Vec3(pullVelocity.x, pullVelocity.y * 0.5, pullVelocity.z);

        // Get current velocity and check how much it opposes the pull direction
        Vec3 currentVel = target.getDeltaMovement();
        Vec3 pullDir = delta.normalize();
        double dot = currentVel.dot(pullDir); // negative = moving away, positive = moving toward

        // If moving against the pull, reduce pull strength proportionally
        double resistanceFactor = dot < 0 ? Math.max(0.3, 1.0 + (dot * 0.4)) : 1.0;
        pullVelocity = pullVelocity.scale(resistanceFactor);

        // Blend with existing velocity instead of hard overwrite
        Vec3 finalVelocity = currentVel.scale(0.2).add(pullVelocity.scale(0.8));

        target.setDeltaMovement(finalVelocity);
        target.hurtMarked = true;

        if (target instanceof ServerPlayer serverTarget) {
            serverTarget.connection.send(new ClientboundSetEntityMotionPacket(serverTarget));
        }
    }
    public static void fireKyn(Player player, double range) {
        if (!(player.level() instanceof ServerLevel world)) return;
        if (Cooldowns.isOnCooldown(player, Kynthia.KYNTHIA_GRAPPLE_CD) && !player.isCreative()) return;

        Vec3 start = player.getEyePosition(1.0F);
        Vec3 direction = player.getViewVector(1.0F).normalize();
        double step = 0.5;
        Vec3 hitPos = null;
        Entity hitEntity = null;

        for (double d = step; d <= range; d += step) {
            Vec3 point = start.add(direction.scale(d));

            BlockHitResult blockHit = world.clip(new ClipContext(
                    start,
                    point,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));

            if (blockHit.getType() == HitResult.Type.BLOCK) {
                hitPos = blockHit.getLocation();
                break;
            }

            hitEntity = world.getEntities(
                    player,
                    player.getBoundingBox().move(direction.scale(d)).inflate(1.0),
                    e -> e.isAlive() && e.isAttackable()
            ).stream().min((a, b) -> {
                double da = a.position().distanceToSqr(start);
                double db = b.position().distanceToSqr(start);
                return Double.compare(da, db);
            }).orElse(null);

            if (hitEntity != null) {
                hitPos = hitEntity.position().add(0, hitEntity.getEyeHeight(), 0);
                break;
            }
        }

        if (hitPos == null) return;

        spawnChainParticles(world, start, hitPos);

        player.level().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT,
                SoundSource.PLAYERS,
                1.0F,
                1.75F
        );

        Cooldowns.start(player, Kynthia.KYNTHIA_GRAPPLE_CD, 20 * 15);

        if (hitEntity instanceof LivingEntity livingEntity) {
            pullEntity(player, livingEntity);
        } else {
            Vec3 targetPos = hitPos.subtract(direction.scale(1.0));
            pullPlayer(player, targetPos);
        }

        if (isAimingDown(player)) {
            bouncePlayer(player, 1.2);
        }
    }

    public static void fireVar(Player player, double range) {
        if (!(player.level() instanceof ServerLevel world)) return;

        Vec3 start = player.getEyePosition(1.0F);
        Vec3 direction = player.getViewVector(1.0F).normalize();
        double step = 0.5;
        Vec3 hitPos = null;
        Entity hitEntity = null;

        for (double d = step; d <= range; d += step) {
            Vec3 point = start.add(direction.scale(d));

            BlockHitResult blockHit = world.clip(new ClipContext(
                    start,
                    point,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));

            if (blockHit.getType() == HitResult.Type.BLOCK) {
                hitPos = blockHit.getLocation();
                break;
            }

            hitEntity = world.getEntities(
                    player,
                    player.getBoundingBox().move(direction.scale(d)).inflate(1.0),
                    e -> e.isAlive() && e.isAttackable()
            ).stream().min((a, b) -> {
                double da = a.position().distanceToSqr(start);
                double db = b.position().distanceToSqr(start);
                return Double.compare(da, db);
            }).orElse(null);

            if (hitEntity != null) {
                hitPos = hitEntity.position().add(0, hitEntity.getEyeHeight(), 0);
                break;
            }
        }

        if (hitPos == null) return;

        spawnChainParticles(world, start, hitPos);

        player.level().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT,
                SoundSource.PLAYERS,
                1.0F,
                1.75F
        );

        if (hitEntity instanceof LivingEntity livingEntity) {
            pullEntity(player, livingEntity);
            livingEntity.addEffect(new MobEffectInstance(ModEffects.WRETCHED, 20 * 15, 0));
        }
    }
}