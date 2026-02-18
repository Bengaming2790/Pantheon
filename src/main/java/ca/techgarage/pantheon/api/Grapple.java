package ca.techgarage.pantheon.api;

import ca.techgarage.pantheon.items.weapons.Kynthia;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;

public class Grapple {

    public static boolean isAimingDown(PlayerEntity player) {
        return player.getPitch() > 80.0F;
    }
    public static void pullPlayer(PlayerEntity player, Vec3d target) {
        if (player.isSneaking()) {
            player.setSneaking(false);
        }

        Vec3d look = player.getRotationVec(1.0F).normalize();

        double speed = 1.6;
        double yBoost = 0.15;

        Vec3d velocity = new Vec3d(
                look.x * speed,
                look.y * speed + yBoost,
                look.z * speed
        );

        player.setVelocity(velocity);
        player.velocityDirty = true;
        player.fallDistance = 0;

        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.networkHandler.sendPacket(
                    new EntityVelocityUpdateS2CPacket(serverPlayer)
            );
        }
    }




    public static void bouncePlayer(PlayerEntity player) {
        Vec3d vel = player.getVelocity();

        player.setVelocity(
                vel.x * 0.3,
                1.2,
                vel.z * 0.3
        );
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
        }
    }

    public static void spawnChainParticles(ServerWorld world, Vec3d from, Vec3d to) {
        Vec3d delta = to.subtract(from);
        int points = 128;

        for (int i = 0; i <= points; i++) {
            Vec3d pos = from.add(delta.multiply(i / (double) points));
            world.spawnParticles(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }
    public static void pullEntity(PlayerEntity player, Entity target) {
        Vec3d playerPos = player.getEntityPos().add(0, player.getStandingEyeHeight(), 0);
        Vec3d targetPos = target.getEntityPos().add(0, target.getStandingEyeHeight(), 0);

        Vec3d velocity = playerPos.subtract(targetPos)
                .normalize()
                .multiply(1.6);

        target.setVelocity(velocity);

        if (target instanceof ServerPlayerEntity serverTarget) {
            serverTarget.networkHandler.sendPacket(
                    new EntityVelocityUpdateS2CPacket(serverTarget)
            );
        }
    }

    public static void fire(PlayerEntity player, double range) {
        if (!(player.getEntityWorld() instanceof ServerWorld world)) return;
        if (Cooldowns.isOnCooldown(player, Kynthia.KYNTHIA_GRAPPLE_CD) && (player.getGameMode() != GameMode.CREATIVE)) return;
        Vec3d start = player.getCameraPosVec(1.0F);
        Vec3d direction = player.getRotationVec(1.0F);
        Vec3d end = start.add(direction.multiply(range));

        BlockHitResult blockHit = world.raycast(new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        ));

        if (blockHit.getType() != BlockHitResult.Type.BLOCK) {
            return;
        }

        Vec3d hitPos = blockHit.getPos();

        spawnChainParticles(world, start, hitPos);
        player.getEntityWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_BLAZE_SHOOT,
                SoundCategory.PLAYERS,
                1.0F, // volume
                1.75F  // pitch
        );
        Cooldowns.start(player, Kynthia.KYNTHIA_GRAPPLE_CD, 20 * 15);
        Entity closestEntity = world.getOtherEntities(
                player,
                player.getBoundingBox()
                        .stretch(direction.multiply(range))
                        .expand(1.0),
                e -> e.isAlive() && e.isAttackable()
        ).stream().min((a, b) -> {
            double da = a.getEntityPos().squaredDistanceTo(start);
            double db = b.getEntityPos().squaredDistanceTo(start);
            return Double.compare(da, db);
        }).orElse(null);

        if (closestEntity != null &&
                closestEntity.getEntityPos().distanceTo(start) < hitPos.distanceTo(start)) {

            pullEntity(player, closestEntity);
            return;
        }

        double distance = hitPos.distanceTo(player.getEntityPos());

        if (distance <= 2.0 && isAimingDown(player)) {
            bouncePlayer(player);
        } else {
            pullPlayer(player, hitPos);
        }
    }



}
