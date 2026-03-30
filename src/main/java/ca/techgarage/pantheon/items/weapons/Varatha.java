package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.*;
import ca.techgarage.pantheon.items.GlowItem;
import ca.techgarage.pantheon.items.material.ModToolMaterials;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.ItemLore;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Varatha extends Item implements PolymerItem, GlowItem {

    private static final String STYGIAN = "varatha_stygian";
    public static final String VARATHA_GRAPPLE_CD = "varatha_grapple_cd";
    public static final String VARATHA_CHARGE_CD = "varatha_charge_cd";

    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("pantheon", "varatha");

    public Varatha(Properties properties) {
        super(properties.spear(
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
                .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                .component(DataComponents.ATTRIBUTE_MODIFIERS, getDefaultAttributeModifiers())
                .fireResistant()
                .component(DataComponents.LORE, createLore)
        );
        registerEvents();
    }


    private static final ItemLore createLore = new ItemLore(List.of(
            Component.literal(""),
            Component.literal("A Bident Wielded by ")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.DARK_RED).withBold(true))
                    .append(Component.literal("Hades")
                            .setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.DARK_RED).withBold(true))),
            Component.literal(""),
            Component.literal("Piercing Blows")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GOLD).withBold(true)),
            Component.literal("  Ignores a portion of opponent's armor")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)),
            Component.literal(""),
            Component.literal("Stygian Wound")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GOLD).withBold(true)),
            Component.literal("   §r§7On hitting an enemy inflict §8§l§oWithering §r§7& §r§8§l§oBlindness"),
            Component.literal("   §r§7Cooldown: §2§l15s"),
            Component.literal(""),
            Component.literal("§r§6§lGravebound Charge §f§l(Right Click)"),
            Component.literal("   §r§7Charge forward and leave a trail that §8§l§oHarms"),
            Component.literal("   §r§7Cooldown: §2§l15s"),
            Component.literal(""),
            Component.literal("Abyssal Recovery")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GOLD).withBold(true)),
            Component.literal("   §r§8§l§oRecover Health §r§7on slaying an enemy"),
            Component.literal(""),
            Component.literal("Hellish Immunity")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GOLD).withBold(true)),
            Component.literal("   §r§7Grants §8§l§oDebuff Immunity")
    ));

    public static ItemAttributeModifiers getDefaultAttributeModifiers() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                BASE_ATTACK_DAMAGE_ID,
                                7.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                BASE_ATTACK_SPEED_ID,
                                -2.8,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    private static void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (getHeldVaratha(player) == null) continue;

                List<MobEffectInstance> effects = new ArrayList<>(player.getActiveEffects());
                for (MobEffectInstance status : effects) {
                    if (status.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                        player.removeEffect(status.getEffect());
                    }
                }
            }
        });
    }

    private static ItemStack getHeldVaratha(Player player) {
        if (player.getMainHandItem().getItem() instanceof Varatha) return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof Varatha) return player.getOffhandItem();
        return null;
    }


    @Override
    public @NonNull InteractionResult use(Level level, Player user, @NonNull InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!level.isClientSide()) {

            if (!Cooldowns.isOnCooldown(user, VARATHA_GRAPPLE_CD) && user.isShiftKeyDown()) {
                Grapple.fireVar(user, 32.0);
                if (!user.isCreative()) {
                    Cooldowns.start(user, VARATHA_GRAPPLE_CD, 20 * 20,  "Carthonic Grasp");
                }
                return InteractionResult.SUCCESS;
            } else if (Cooldowns.isOnCooldown(user, VARATHA_GRAPPLE_CD) && user.isShiftKeyDown()) {
                ServerPlayer serverPlayer = (ServerPlayer) user;
                serverPlayer.sendSystemMessage(Component.literal("Carthonic Grasp is on Cooldown"), true);
                return InteractionResult.FAIL;
            }
            if (user.pick(2.0, 0.0f, false).getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                return InteractionResult.PASS;
            }

            if (user instanceof ServerPlayer serverPlayer) {

                if (!Cooldowns.isOnCooldown(serverPlayer, VARATHA_CHARGE_CD)) {
                    if (!serverPlayer.isCreative()) {
                        Cooldowns.start(serverPlayer, VARATHA_CHARGE_CD, 20 * 15, "Gravebound Charge");
                    }
                    Dash.dashForward(user, 1.5f);
                    DashState.start(serverPlayer, 15, net.minecraft.core.particles.ParticleTypes.RAID_OMEN);
                    serverPlayer.startAutoSpinAttack(5, 2.5f, serverPlayer.getActiveItem());
                    level.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.0F, 0.5F);
                } else if (!Cooldowns.isOnCooldown(serverPlayer, VARATHA_CHARGE_CD)) {
                    serverPlayer.sendSystemMessage(Component.literal("Gravebound Charge is on Cooldown"), true);
                    return InteractionResult.FAIL;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void hurtEnemy(@NonNull ItemStack stack, @NonNull LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide()) return ;
        if (!(attacker instanceof ServerPlayer player)) return ;

        float damage = 19f;

        if (attacker.hasEffect(MobEffects.STRENGTH)) {
            MobEffectInstance effect = attacker.getEffect(MobEffects.STRENGTH);
            if (effect != null) {
                damage += (effect.getAmplifier() + 1) * 3f;
            }
        }

        target.hurt(attacker.damageSources().magic(), damage);

        if (!Cooldowns.isOnCooldown(player, STYGIAN)) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 1, true, false));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, true, true));

            if (!player.isCreative()) Cooldowns.start(player, STYGIAN, 300);
        }

    }

    @Override
    public void inventoryTick(@NonNull ItemStack stack, @NonNull ServerLevel serverLevel, @NonNull Entity entity, @Nullable EquipmentSlot equipmentSlot) {
        if (entity instanceof Player player) {
            if (stack.has(DataComponents.CUSTOM_NAME)) {
                player.sendSystemMessage(Component.translatable("item.anvil.rename").withStyle(ChatFormatting.RED));
                stack.remove(DataComponents.CUSTOM_NAME);
            }
            if (stack.has(DataComponents.ENCHANTMENTS)) {
                stack.remove(DataComponents.ENCHANTMENTS);
            }


        }
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack stack) {
        return Component.translatable("item.pantheon.varatha").withStyle(ChatFormatting.GOLD);
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return MODEL;
    }

    @Override
    public String getGlowColor() {
        return "#FF5555";
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.STICK;
    }



}