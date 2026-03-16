package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.api.DashState;
import ca.techgarage.pantheon.items.GlowItem;
import ca.techgarage.pantheon.items.material.ModToolMaterials;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import ca.techgarage.pantheon.api.Dash;
import org.jspecify.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

public class Varatha extends Item implements PolymerItem, GlowItem {

    private static final Random random = new Random();
    private static final String STYGIAN = "varatha_stygian";
    private static final Identifier MODEL =
            Identifier.of("pantheon", "varatha");
    public Varatha(Settings settings) {
        super(
                settings.spear(
                        ModToolMaterials.VARATHA_TOOL_MATERIAL,
                        1.5f,    // swingAnimationSeconds → attack speed math
                        1.5f,    // chargeDamageMultiplier
                        0f,   // chargeDelaySeconds
                        3.0f,    // maxDurationForDismountSeconds
                        0.1f,    // minSpeedForDismount
                        3.0f,    // maxDurationForChargeKnockbackInSeconds
                        0.1f,    // minSpeedForChargeKnockback
                        3.0f,    // maxDurationForChargeDamageInSeconds
                        0.1f     // minRelativeSpeedForChargeDamage
                )
                        .component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE)
                        .component(DataComponentTypes.ATTRIBUTE_MODIFIERS, getDefaultAttributeModifiers()).fireproof()
                        .component(DataComponentTypes.TOOLTIP_DISPLAY, new TooltipDisplayComponent(false, new LinkedHashSet<>(List.of(
                                DataComponentTypes.ATTRIBUTE_MODIFIERS,
                                DataComponentTypes.UNBREAKABLE
                        )))).component(DataComponentTypes.LORE, lore)
        );
        registerEvents();
    }


    private static final LoreComponent lore = new LoreComponent(List.of(
            Text.literal("A Bident Wielded by ")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.RED).withBold(false))
                    .append(Text.literal("Hades")
                            .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.RED).withBold(true))),
            Text.literal("Piercing Blows")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GOLD).withBold(true)),
            Text.literal("  Ignores a portion of opponent's armor")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY)),
            Text.literal("Stygian Wound")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GOLD).withBold(true)),
            Text.literal("   On Hitting an enemy inflict ").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY))
                    .append(Text.literal("Withering & Blidness").setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.AQUA).withBold(false))),
            Text.literal("   Cooldown: ")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY)).append(Text.literal("15s").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GREEN))),

            Text.literal("Gravebound Charge")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GOLD).withBold(true))
                    .append(Text.literal(" - Right Click")
                            .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY).withBold(false))),
            Text.literal("   Charge forward and leave a Trail that harms opponents")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY)),
            Text.literal("   Cooldown: 15s")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY)),
            Text.literal("Abyssal Recovery")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GOLD).withBold(true)),
            Text.literal("   Recover Health on slaying an enemy")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY)),
            Text.literal("Hellish Immunity")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GOLD).withBold(true)),
            Text.literal("   Grants Immunity to Debuffs")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY))
    ));


    public static AttributeModifiersComponent getDefaultAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                7.0,
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


    private static void registerEvents() {

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (getHeldVaratha(player) == null) continue;

                for (StatusEffectInstance status : List.copyOf(player.getStatusEffects())) {
                    RegistryEntry<StatusEffect> effectEntry = status.getEffectType();

                    if (effectEntry.value().getCategory() == StatusEffectCategory.HARMFUL) {
                        player.removeStatusEffect(effectEntry);
                    }
                }
            }
        });
    }
    private static ItemStack getHeldVaratha(PlayerEntity player) {
        if (player.getMainHandStack().getItem() instanceof Varatha)
            return player.getMainHandStack();
        if (player.getOffHandStack().getItem() instanceof Varatha)
            return player.getOffHandStack();
        return null;
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

            Dash.dashForward(user, 1.5f);
            DashState.start((ServerPlayerEntity) user, 15, ParticleTypes.RAID_OMEN);
            world.playSound(
                    null,
                    user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_BREEZE_WIND_BURST,
                    SoundCategory.PLAYERS,
                    1.0F, // volume
                    0.5F  // pitch
            );
            user.useRiptide(15,0, stack);

        }
        return ActionResult.SUCCESS;
    }
    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getEntityWorld().isClient()) return;
        if (!(attacker instanceof ServerPlayerEntity player)) return;

        float damage = 19f;

        // Check for Strength effect
        if (attacker.hasStatusEffect(StatusEffects.STRENGTH)) {
            StatusEffectInstance effect = attacker.getStatusEffect(StatusEffects.STRENGTH);

            assert effect != null;
            int level = effect.getAmplifier() + 1; // convert amplifier → level
            damage += level * 3f; // +3 damage per level
        }

        // Armor ignoring damage (magic)
        target.damage(
                (ServerWorld) attacker.getEntityWorld(),
                attacker.getDamageSources().magic(),
                damage
        );

        if (Cooldowns.isOnCooldown(player, STYGIAN)) return;

        target.addStatusEffect(
                new StatusEffectInstance(StatusEffects.BLINDNESS, 20 * 5, 1, true, false, false)
        );

        target.addStatusEffect(
                new StatusEffectInstance(StatusEffects.WITHER, 20 * 5, 1, true, true, false)
        );


        if (!player.isCreative()) Cooldowns.start(player, STYGIAN, 20 * 15);
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
    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.varatha").formatted();
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.STICK;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL;
    }

    @Override
    public String getGlowColor() {
        return "#FF5555";
    }
}