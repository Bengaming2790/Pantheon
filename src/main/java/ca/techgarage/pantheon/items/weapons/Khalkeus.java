package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.AOEDamage;
import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.api.Dash;
import ca.techgarage.pantheon.api.DashState;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.Predicate;

public class Khalkeus extends MaceItem implements PolymerItem {
    public Khalkeus(Settings settings) {
        super(settings.component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, getDefaultAttributeModifiers()).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).component(DataComponentTypes.MAX_STACK_SIZE, 1).fireproof());
    }
    private static final Identifier MODEL =
            Identifier.of("pantheon", "khalkeus");
    private static final String AOE_CD = "khalkeus_aoe_cd";
    public static AttributeModifiersComponent getDefaultAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                5,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_SPEED_MODIFIER_ID,
                                -3.2,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            if(user.raycast(2, 0, false).getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                return ActionResult.PASS;
            }
            if (!(user.getGameMode() == GameMode.CREATIVE)) {
                user.getItemCooldownManager().set(stack, 200);
            }
            Dash.dashForward(user, 0.95f);
            DashState.start((ServerPlayerEntity) user, 10, ParticleTypes.FLAME);
            world.playSound(
                    null,
                    user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_FIRE_AMBIENT,
                    SoundCategory.PLAYERS,
                    1.5F, // volume
                    1.75F  // pitch
            );
            user.useRiptide(10, 1.0f, stack);

        }
        return ActionResult.SUCCESS;
    }
    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        PlayerEntity player = (PlayerEntity) entity;
        if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            player.sendMessage(Text.translatable("item.anvil.rename").formatted(), true);
            stack.remove(DataComponentTypes.CUSTOM_NAME);
        }
        if (stack.contains(DataComponentTypes.ENCHANTMENTS)) {
            stack.remove(DataComponentTypes.ENCHANTMENTS);
        }
    }
    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (attacker.getEntityWorld().isClient()) return;
        if (shouldDealAdditionalDamage(attacker)) {
            ServerWorld serverWorld = (ServerWorld)attacker.getEntityWorld();
            attacker.setVelocity(attacker.getVelocity().withAxis(Direction.Axis.Y, 0.01F));
            if (attacker instanceof ServerPlayerEntity serverPlayerEntity) {
                serverPlayerEntity.currentExplosionImpactPos = this.getCurrentExplosionImpactPos(serverPlayerEntity);
                serverPlayerEntity.setIgnoreFallDamageFromCurrentExplosion(true);
                serverPlayerEntity.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayerEntity));
            }

            if (target.isOnGround()) {
                if (attacker instanceof ServerPlayerEntity serverPlayerEntity) {
                    serverPlayerEntity.setSpawnExtraParticlesOnFall(true);
                }

                SoundEvent soundEvent = attacker.fallDistance > (double)5.0F ? SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY : SoundEvents.ITEM_MACE_SMASH_GROUND;
                serverWorld.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), soundEvent, attacker.getSoundCategory(), 1.0F, 1.0F);
            } else {
                serverWorld.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.ITEM_MACE_SMASH_AIR, attacker.getSoundCategory(), 1.0F, 1.0F);
            }

            knockbackNearbyEntities(serverWorld, attacker, target);
        }
        target.setOnFireForTicks(20 * 4);

        if (!Cooldowns.isOnCooldown((PlayerEntity) attacker, AOE_CD)) {
                AOEDamage.applyAoeDamage(attacker, target, 6.0f, 2.5f, true, 4);
                Cooldowns.start((PlayerEntity) attacker, AOE_CD, 20 * 15);
            }

    }

    private Vec3d getCurrentExplosionImpactPos(ServerPlayerEntity player) {
        return player.shouldIgnoreFallDamageFromCurrentExplosion() && player.currentExplosionImpactPos != null && player.currentExplosionImpactPos.y <= player.getEntityPos().y ? player.currentExplosionImpactPos : player.getEntityPos();
    }

    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.khalkeus").formatted();
    }
    private static void knockbackNearbyEntities(World world, Entity attacker, Entity attacked) {
        world.syncWorldEvent(2013, attacked.getSteppingPos(), 750);
        world.getEntitiesByClass(LivingEntity.class, attacked.getBoundingBox().expand(3.5F), getKnockbackPredicate(attacker, attacked)).forEach((entity) -> {
            Vec3d vec3d = entity.getEntityPos().subtract(attacked.getEntityPos());
            double d = getKnockback(attacker, entity, vec3d);
            Vec3d vec3d2 = vec3d.normalize().multiply(d);
            if (d > (double)0.0F) {
                entity.addVelocity(vec3d2.x, 0.7F, vec3d2.z);
                if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
                    serverPlayerEntity.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayerEntity));
                }
            }

        });
    }
    private static Predicate<LivingEntity> getKnockbackPredicate(Entity attacker, Entity attacked) {
        return (entity) -> {
            boolean bl;
            boolean bl2;
            boolean bl3;
            boolean var10000;
            label82: {
                bl = !entity.isSpectator();
                bl2 = entity != attacker && entity != attacked;
                bl3 = !attacker.isTeammate(entity);
                if (entity instanceof TameableEntity tameableEntity) {
                    if (attacked instanceof LivingEntity livingEntity) {
                        if (tameableEntity.isTamed() && tameableEntity.isOwner(livingEntity)) {
                            var10000 = true;
                            break label82;
                        }
                    }
                }

                var10000 = false;
            }

            boolean bl4;
            label74: {
                bl4 = !var10000;
                if (entity instanceof ArmorStandEntity armorStandEntity) {
                    if (armorStandEntity.isMarker()) {
                        break label74;
                    }
                }

                var10000 = true;
            }

            boolean bl5;
            boolean bl6;
            label68: {
                bl5 = var10000;
                bl6 = attacked.squaredDistanceTo(entity) <= Math.pow(3.5F, 2.0F);
                if (entity instanceof PlayerEntity playerEntity) {
                    if (playerEntity.isCreative() && playerEntity.getAbilities().flying) {
                        break label68;
                    }
                }

                var10000 = false;
            }

            boolean bl7 = !var10000;
            return bl && bl2 && bl3 && bl4 && bl5 && bl6 && bl7;
        };
    }
    private static double getKnockback(Entity attacker, LivingEntity attacked, Vec3d distance) {
        return ((double)3.5F - distance.length()) * (double)0.7F * (double)(attacker.fallDistance > (double)5.0F ? 2 : 1) * ((double)1.0F - attacked.getAttributeValue(EntityAttributes.KNOCKBACK_RESISTANCE));
    }
    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.DIAMOND;
    }
    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL;
    }


}
