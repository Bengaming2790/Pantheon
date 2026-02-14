package ca.techgarage.pantheon.entity;

import ca.techgarage.pantheon.items.ModItems;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class AstrapeEntity extends TridentEntity implements PolymerEntity {

    public AstrapeEntity(EntityType<? extends TridentEntity> entityType, World world) {
        super(entityType, world);
    }
    public AstrapeEntity(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }

    public AstrapeEntity(World world, PlayerEntity player, ItemStack stack) {
        super(world, player, stack);
    }
    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        float f = 8.0F;
        Entity entity2 = this.getOwner();
        DamageSource damageSource = this.getDamageSources().trident(this, (Entity)(entity2 == null ? this : entity2));
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld serverWorld) {
            f = EnchantmentHelper.getDamage(serverWorld, this.getWeaponStack(), entity, damageSource, f);
        }

        if (entity.sidedDamage(damageSource, f)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }

            world = this.getEntityWorld();
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld) world;
                EnchantmentHelper.onTargetDamaged(serverWorld, entity, damageSource, this.getWeaponStack(), (item) -> this.kill(serverWorld));
            }

            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                this.knockback(livingEntity, damageSource);
                this.onHit(livingEntity);
                livingEntity.setStatusEffect(
                        new StatusEffectInstance(StatusEffects.GLOWING, 30, 1, true, false, false),
                        livingEntity
                );
                livingEntity.setStatusEffect(
                        new StatusEffectInstance(ModEffects.CONDUCTING, 30, 1, true, false, false),
                        livingEntity
                );
            }
        }

        this.deflect(ProjectileDeflection.SIMPLE, entity, this.owner, false);
        this.setVelocity(this.getVelocity().multiply(0.02, 0.2, 0.02));
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0F, 1.0F);
    }
    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.TRIDENT;
    }
}
