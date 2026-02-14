package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.entity.AstrapeEntity;
import ca.techgarage.pantheon.items.ModItems;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class Astrape extends TridentItem implements PolymerItem {

    public Astrape(Item.Settings settings) {
        super(settings);
    }

    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                8.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                BASE_ATTACK_SPEED_MODIFIER_ID,
                                -2.9,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }
    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context) {
        out.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        out.remove(DataComponentTypes.CUSTOM_DATA);
        out.remove(DataComponentTypes.ITEM_MODEL);
    }


    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getEntityWorld().isClient()) return;

        net.minecraft.entity.effect.StatusEffectInstance conducting = target.getStatusEffect((RegistryEntry<StatusEffect>) ModItems.CONDUCTING);
        if (conducting == null) return;

        int level = conducting.getAmplifier() + 1;
        float extraDamage = 2.0f * level;

        // Apply extra damage attributed to the attacker
        target.damage((ServerWorld) attacker.getEntityWorld(), attacker.getRecentDamageSource(), extraDamage);
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
                    1.0F
            );

            trident.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;

            world.spawnEntity(trident);

            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }
        return false;
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return new ItemStack(Items.TRIDENT).getItem();
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.astrape");
    }
}

