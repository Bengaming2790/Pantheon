package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.DashState;
import ca.techgarage.pantheon.items.material.ModToolMaterials;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import ca.techgarage.pantheon.api.Dash;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Random;

public class Varatha extends Item implements PolymerItem {

    private static final Random random = new Random();
    private static final Identifier MODEL =
            Identifier.of("pantheon", "varatha");
    public Varatha(Settings settings) {
        super(
                settings.spear(
                        ModToolMaterials.VARATHA_TOOL_MATERIAL,
                        3.0f,    // swingAnimationSeconds → attack speed math
                        1.5f,    // chargeDamageMultiplier
                        0.5f,   // chargeDelaySeconds
                        0.4f,    // maxDurationForDismountSeconds
                        0.6f,    // minSpeedForDismount
                        0.3f,    // maxDurationForChargeKnockbackInSeconds
                        0.4f,    // minSpeedForChargeKnockback
                        0.3f,    // maxDurationForChargeDamageInSeconds
                        0.3f     // minRelativeSpeedForChargeDamage
                ).component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, getDefaultAttributeModifiers())
        );
    }



    public static AttributeModifiersComponent getDefaultAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                6.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_SPEED_MODIFIER_ID,
                                -2.8,
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
                user.getItemCooldownManager().set(stack, 200); //10 second cooldown
            }
            user.useRiptide(10, 1.0f, stack);
            Dash.dashForward(user, 0.75f);
            DashState.start((ServerPlayerEntity) user, 10, ParticleTypes.RAID_OMEN);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        stack.addEnchantment((RegistryEntry<Enchantment>) Enchantments.BREACH, 2);
        stack.addEnchantment((RegistryEntry<Enchantment>) Enchantments.SHARPNESS, 5);
        return stack;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.varatha");
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.DIAMOND_SPEAR;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL;
    }
}