package ca.techgarage.pantheon.entity;

import ca.techgarage.pantheon.api.AOEDamage;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Objects;

public class AstrapeEntity extends TridentEntity implements PolymerEntity {
    public AstrapeEntity(World world, PlayerEntity player, ItemStack stack) {
        super(world, player, stack);
    }
    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        float f = 8.0F;
        Entity entity2 = this.getOwner();
        DamageSource damageSource = this.getDamageSources().trident(this, entity2 == null ? this : entity2);
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld serverWorld) {
            f = EnchantmentHelper.getDamage(serverWorld, Objects.requireNonNull(this.getWeaponStack()), entity, damageSource, f);
        }

        if (entity.sidedDamage(damageSource, f)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }

            world = this.getEntityWorld();
            if (world instanceof ServerWorld serverWorld) {
                EnchantmentHelper.onTargetDamaged(serverWorld, entity, damageSource, this.getWeaponStack(), (item) -> this.kill(serverWorld));
            }

            if (entity instanceof LivingEntity livingEntity) {
                this.knockback(livingEntity, damageSource);
                this.onHit(livingEntity);
                livingEntity.setStatusEffect(
                        new StatusEffectInstance(StatusEffects.GLOWING, 20 * 8, 1, true, false, false),
                        livingEntity
                );
                livingEntity.setStatusEffect(
                        new StatusEffectInstance(ModEffects.CONDUCTING, 20 * 8, 1, true, false, false),
                        livingEntity
                );
            }
        }

        this.deflect(ProjectileDeflection.SIMPLE, entity, this.owner, false);
        this.setVelocity(this.getVelocity().multiply(0.02, 0.2, 0.02));
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Override
    protected void onBlockHitEnchantmentEffects(ServerWorld world, BlockHitResult blockHitResult, ItemStack weaponStack) {
        Vec3d vec3d = blockHitResult.getBlockPos().clampToWithin(blockHitResult.getPos());

        AOEDamage.applyAoeDamage(this.getEntity(), world, vec3d, 7f, 0f, 1.5f);

    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.TRIDENT;
    }
}
