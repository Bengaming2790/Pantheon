package ca.techgarage.pantheon.entity;

import ca.techgarage.pantheon.api.AOEDamage;
import ca.techgarage.pantheon.items.ModItems;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import xyz.nucleoid.packettweaker.PacketContext;

public class AstrapeEntity extends PersistentProjectileEntity implements PolymerEntity {
    private boolean hit = false;
    private DisplayEntity.ItemDisplayEntity display;
    private final ItemStack weaponStack;

    public AstrapeEntity(EntityType<? extends PersistentProjectileEntity> type, World world) {
        super(type, world);
        this.weaponStack = ItemStack.EMPTY;
    }

    public AstrapeEntity(World world, PlayerEntity owner, ItemStack stack) {
        super(ModEntities.ASTRAPE, world);
        this.setOwner(owner);
        this.weaponStack = stack.copy();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getEntityWorld().isClient()) return;

        if (!hit && (display == null || !display.isAlive())) {
            spawnDisplay();
        }

        if (display != null) {
            display.setPosition(this.getX(), this.getY(), this.getZ());

            Vec3d vel = this.getVelocity();
            if (vel.lengthSquared() > 0.001) {
                float yaw = (float)(Math.atan2(vel.z, vel.x) * (180F / Math.PI)) - 90F;
                float pitch = (float)(-Math.atan2(vel.y, vel.horizontalLength()) * (180F / Math.PI));

                Quaternionf rotation = new Quaternionf()
                        .rotateY((float)Math.toRadians(-yaw + 90))
                        .rotateX((float)Math.toRadians(pitch));

                Quaternionf offset = new Quaternionf()
                        .rotateX((float)Math.toRadians(80));

                rotation.mul(offset);

                display.setTransformation(new AffineTransformation(
                        new org.joml.Vector3f(),
                        rotation,
                        new org.joml.Vector3f(1f, 1f, 1f),
                        new Quaternionf()
                ));
            }
        }

        if (this.isRemoved() && display != null) {
            display.discard();
        }
    }

    private void spawnDisplay() {
        if (!(this.getEntityWorld() instanceof ServerWorld world)) return;

        display = new DisplayEntity.ItemDisplayEntity(EntityType.ITEM_DISPLAY, world);

        display.setPosition(this.getX(), this.getY(), this.getZ());

        ItemStack stack = new ItemStack(ModItems.ASTRAPE);

        display.setItemStack(stack);
        display.setItemDisplayContext(ItemDisplayContext.GROUND);

        display.setDisplayWidth(1f);
        display.setDisplayHeight(1f);

        world.spawnEntity(display);
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        Entity entity = hit.getEntity();
        float damage = 8.0F;

        Entity owner = this.getOwner();
        DamageSource source = this.getDamageSources().trident(this, owner == null ? this : owner);

        World world = this.getEntityWorld();

        if (world instanceof ServerWorld serverWorld) {
            damage = EnchantmentHelper.getDamage(
                    serverWorld,
                    weaponStack,
                    entity,
                    source,
                    damage
            );
        }

        assert world instanceof ServerWorld;
        if (entity.damage((ServerWorld) world, source, damage)) {
            if (entity.getType() == EntityType.ENDERMAN) return;

            if (world instanceof ServerWorld serverWorld) {
                EnchantmentHelper.onTargetDamaged(
                        serverWorld,
                        entity,
                        source,
                        weaponStack,
                        (item) -> this.kill(serverWorld)
                );
            }

            if (entity instanceof LivingEntity living) {
                this.knockback(living, source);

                living.setStatusEffect(
                        new StatusEffectInstance(StatusEffects.GLOWING, 20 * 8, 1, true, false, false),
                        this
                );

                living.setStatusEffect(
                        new StatusEffectInstance(ModEffects.CONDUCTING, 20 * 8, 1, true, false, false),
                        this
                );
            }
        }

        this.setVelocity(this.getVelocity().multiply(0.02, 0.2, 0.02));
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Override
    protected void onBlockHit(BlockHitResult hitResult) {
        super.onBlockHit(hitResult);

        if (this.getEntityWorld() instanceof ServerWorld world) {
            Vec3d pos = hitResult.getBlockPos().toCenterPos();
            AOEDamage.applyAoeDamage(this.getEntity(), world, pos, 7f, 15f, 1.5f);
        }

        this.hit = true;

        if (display != null) {
            display.discard();
            display = null;
        }
        this.discard();
    }

    @Override
    protected ItemStack asItemStack() {
        return weaponStack.isEmpty() ? new ItemStack(ModItems.ASTRAPE) : weaponStack;
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(ModItems.ASTRAPE);
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.ITEM_DISPLAY;
    }

}