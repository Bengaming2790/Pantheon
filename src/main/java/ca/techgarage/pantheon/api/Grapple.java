package ca.techgarage.pantheon.api;

import ca.techgarage.pantheon.items.weapons.Kynthia;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Grapple {

    public static boolean isAimingDown(Player player) {
        return player.getXRot() > 80.0F;
    }

    public static void pullPlayer(Player player, Vec3 target) {
        if (player.isShiftKeyDown()) {
            player.setShiftKeyDown(false);
        }

        Vec3 look = player.getViewVector(1.0F).normalize();

        double speed = 1.6;
        double yBoost = 0.15;

        Vec3 velocity = new Vec3(
                look.x * speed,
                look.y * speed + yBoost,
                look.z * speed
        );

        player.setDeltaMovement(velocity);
        player.hurtMarked = true;
        player.fallDistance = 0;

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(
                    new ClientboundSetEntityMotionPacket(serverPlayer)
            );
        }
    }

    public static void bouncePlayer(Player player) {
        Vec3 vel = player.getDeltaMovement();

        player.setDeltaMovement(
                vel.x * 0.3,
                1.2,
                vel.z * 0.3
        );

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

    public static void pullEntity(Player player, Entity target) {
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 targetPos = target.position().add(0, target.getEyeHeight(), 0);

        Vec3 velocity = playerPos.subtract(targetPos)
                .normalize()
                .scale(1.6);

        target.setDeltaMovement(velocity);

        if (target instanceof ServerPlayer serverTarget) {
            serverTarget.connection.send(
                    new ClientboundSetEntityMotionPacket(serverTarget)
            );
        }
    }

    public static void fire(Player player, double range) {
        if (!(player.level() instanceof ServerLevel world)) return;
        if (Cooldowns.isOnCooldown(player, Kynthia.KYNTHIA_GRAPPLE_CD) && !player.isCreative()) return;

        Vec3 start = player.getEyePosition(1.0F);
        Vec3 direction = player.getViewVector(1.0F);
        Vec3 end = start.add(direction.scale(range));

        BlockHitResult blockHit = world.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        if (blockHit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        Vec3 hitPos = blockHit.getLocation();

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

        Entity closestEntity = world.getEntities(
                player,
                player.getBoundingBox()
                        .expandTowards(direction.scale(range))
                        .inflate(1.0),
                e -> e.isAlive() && e.isAttackable()
        ).stream().min((a, b) -> {
            double da = a.position().distanceToSqr(start);
            double db = b.position().distanceToSqr(start);
            return Double.compare(da, db);
        }).orElse(null);

        if (closestEntity != null &&
                closestEntity.position().distanceTo(start) < hitPos.distanceTo(start)) {
            pullEntity(player, closestEntity);
            return;
        }

        double distance = hitPos.distanceTo(player.position());

        if (distance <= 2.0 && isAimingDown(player)) {
            bouncePlayer(player);
        } else {
            pullPlayer(player, hitPos);
        }
    }
}