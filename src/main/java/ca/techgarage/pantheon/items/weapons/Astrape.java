package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.entity.AstrapeEntity;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
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
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class Astrape extends TridentItem implements PolymerItem {

    public Astrape(Item.Settings settings) {
        super(settings.component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createAttributeModifiers()).component(DataComponentTypes.MAX_STACK_SIZE, 1) );
    }

    private static final String ASTRAPE_CD = "astrape_lightning_cd";

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
                                1.8,
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

        StatusEffectInstance conducting =
                target.getStatusEffect(ModEffects.CONDUCTING);

        if (conducting == null) return ;

        if (Cooldowns.isOnCooldown(player, ASTRAPE_CD)) return ;

        int level = conducting.getAmplifier() + 1;
        float extraDamage = 2.0f * level;

        target.damage((ServerWorld) attacker.getEntityWorld(), attacker.getEntityWorld().getDamageSources().playerAttack(player), extraDamage);

        LightningEntity lightning =
                new LightningEntity(EntityType.LIGHTNING_BOLT, attacker.getEntityWorld());

        lightning.setPosition(target.getX(), target.getY(), target.getZ());
        attacker.getEntityWorld().spawnEntity(lightning);

        Cooldowns.start(player, ASTRAPE_CD, 20 * 15);
    }


    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return false;

        int useTime = this.getMaxUseTime(stack, user) - remainingUseTicks;
        if (useTime < 10) return false;

        if (!world.isClient()) {
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

