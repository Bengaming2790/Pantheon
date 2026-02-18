package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.api.Grapple;
import ca.techgarage.pantheon.entity.AstrapeEntity;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Random;

public class Astrape extends TridentItem implements PolymerItem {

    public Astrape(Item.Settings settings) {
        super(settings.component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createAttributeModifiers()).component(DataComponentTypes.MAX_STACK_SIZE, 1) );
    }

    private static final String ASTRAPE_CD = "astrape_lightning_cd";
    private static final String ASTRAPE_THROW_CD = "astrape_throw_cd";
    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                11.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                BASE_ATTACK_SPEED_MODIFIER_ID,
                                -2.2,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.SAFE_FALL_DISTANCE,
                        new EntityAttributeModifier(
                                Identifier.of("pantheon", "astrape_safe_fall_distance"),
                                1024.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.ANY

                )
                .build();
    }



    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getEntityWorld().isClient()) return ;
        if (!(attacker instanceof PlayerEntity player)) return ;

        double randomConduct = Math.random();

        if (randomConduct <= 0.1) {
            for (int i = 0; i < 5; i ++) {
                LightningEntity lightning =
                        new LightningEntity(EntityType.LIGHTNING_BOLT, attacker.getEntityWorld());

                lightning.setPosition(target.getX(), target.getY(), target.getZ());
                attacker.getEntityWorld().spawnEntity(lightning);
            }
        }

        StatusEffectInstance conducting =
                target.getStatusEffect(ModEffects.CONDUCTING);

        if (conducting == null) return ;

        if (Cooldowns.isOnCooldown(player, ASTRAPE_CD)) return ;

        int level = conducting.getAmplifier() + 1;
        float extraDamage = 2.0f * level;

        target.damage((ServerWorld) attacker.getEntityWorld(), attacker.getEntityWorld().getDamageSources().playerAttack(player), extraDamage);
        for (int i = 0; i < 5; i ++) {
            LightningEntity lightning =
                    new LightningEntity(EntityType.LIGHTNING_BOLT, attacker.getEntityWorld());

            lightning.setPosition(target.getX(), target.getY(), target.getZ());
            attacker.getEntityWorld().spawnEntity(lightning);
        }
        Cooldowns.start(player, ASTRAPE_CD, 10);
    }


    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return false;

        int useTime = this.getMaxUseTime(stack, user) - remainingUseTicks;
        if (useTime < 10) return false;
        if (!world.isClient() && !Cooldowns.isOnCooldown(player, ASTRAPE_THROW_CD)) {


            AstrapeEntity trident = new AstrapeEntity(
                    world,
                    player,
                    stack
            );

            trident.setVelocity(
                    player,
                    player.getPitch(),
                    player.getYaw(),
                    0.0F,
                    2.5F,
                    0f
            );

            trident.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;

            world.spawnEntity(trident);
            if (!player.isCreative()) Cooldowns.start(player, ASTRAPE_THROW_CD, 20 * 15);
        }
        return false;
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
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.STICK;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.astrape");
    }
}

