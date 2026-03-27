package ca.techgarage.pantheon.entity;

import ca.techgarage.pantheon.api.AOEDamage;
import ca.techgarage.pantheon.items.ModItems;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;


public class AstrapeEntity extends Arrow implements PolymerEntity {
    private boolean hit = false;
    private Display.ItemDisplay display;
    private final ItemStack weaponStack;

    public AstrapeEntity(EntityType<? extends Arrow> type, Level level) {
        super(type, level);
        this.weaponStack = ItemStack.EMPTY;
    }

    public AstrapeEntity(Level level, Player owner, ItemStack stack) {
        // Ensure ModEntities.ASTRAPE is registered as an EntityType<? extends AbstractArrow>
        super(ModEntities.ASTRAPE, level);
        this.setOwner(owner);
        this.weaponStack = stack.copy();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) return;

        if (!hit && (display == null || !display.isAlive())) {
            spawnDisplay();
        }

        if (display != null) {
            display.setPos(this.getX(), this.getY(), this.getZ());

            Vec3 vel = this.getDeltaMovement();
            if (vel.lengthSqr() > 0.001) {
                float yaw = (float)(Math.atan2(vel.z, vel.x) * (180F / Math.PI)) - 90F;
                float pitch = (float)(-Math.atan2(vel.y, vel.horizontalDistance()) * (180F / Math.PI));

                Quaternionf rotation = new Quaternionf()
                        .rotateY((float)Math.toRadians(-yaw + 90))
                        .rotateX((float)Math.toRadians(pitch));

                Quaternionf offset = new Quaternionf()
                        .rotateX((float)Math.toRadians(80));

                rotation.mul(offset);

                display.setTransformation(new Transformation(
                        new Vector3f(),
                        rotation,
                        new Vector3f(1f, 1f, 1f),
                        new Quaternionf()
                ));
            }
        }

        if (this.isRemoved() && display != null) {
            display.discard();
        }
    }

    private void spawnDisplay() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, serverLevel);
        display.setPos(this.getX(), this.getY(), this.getZ());

        ItemStack stack = new ItemStack(ModItems.ASTRAPE);
        display.setItemStack(stack);
        display.setItemTransform(ItemDisplayContext.GROUND);

        // setDisplayWidth/Height are usually handled via DataTracker or specific setters in MojMap
        display.setWidth(1f);
        display.setHeight(1f);

        serverLevel.addFreshEntity(display);
    }

    public @Nullable EntityReference<Entity> owner() {
        return owner;
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        Entity entity = hit.getEntity();
        float damage = 8.0F;

        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().trident(this, owner == null ? this : owner);

        if (this.level() instanceof ServerLevel serverLevel) {
            EnchantmentHelper.modifyDamage(
                    serverLevel,
                    weaponStack,
                    entity,
                    source,
                    damage
            );

            if (entity.hurtMarked) {
                if (entity.getType() == EntityType.ENDERMAN) return;

                if (entity instanceof LivingEntity living) {
                    this.doPostHurtEffects(living);

                    living.addEffect(
                            new MobEffectInstance(MobEffects.GLOWING, 20 * 8, 1, true, false, false)
                    );

                    living.addEffect(
                            new MobEffectInstance(ModEffects.CONDUCTING, 20 * 8, 1, true, true, false),
                            entity
                    );
                }
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(0.02, 0.2, 0.02));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);

        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = hitResult.getBlockPos().getCenter();
            AOEDamage.applyAoeDamage(this, serverLevel, pos, 7f, 15f, 1.5f);
        }

        this.hit = true;

        if (display != null) {
            display.discard();
            display = null;
        }
        this.discard();
    }

    @Override
    protected ItemStack getPickupItem() {
        return weaponStack.isEmpty() ? new ItemStack(ModItems.ASTRAPE) : weaponStack;
    }


    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.ITEM_DISPLAY;
    }
}