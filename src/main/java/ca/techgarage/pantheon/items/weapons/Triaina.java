package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.*;
import ca.techgarage.pantheon.items.GlowItem;
import ca.techgarage.pantheon.items.ModItems;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashSet;
import java.util.List;

public class Triaina extends Item implements PolymerItem, GlowItem {
    public Triaina(Properties settings) {
        super(settings.component(DataComponents.UNBREAKABLE, Unit.INSTANCE).component(DataComponents.MAX_STACK_SIZE, 1).component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributeModifiers()).fireResistant()
                .component(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, new LinkedHashSet<>(List.of(
                        DataComponents.ATTRIBUTE_MODIFIERS,
                        DataComponents.UNBREAKABLE
                )))));
        applyEffects();
    }
    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath("pantheon", "triaina");
    private static final String TRIAINA_COMBO_TIMER = "triaina_hit_combo_timer";
    private static final String TRIAINA_COMBO_HITS = "triaina_hit_combo_hits";
    private static final String TRIAINA_TEMPEST_CD = "triaina_tempest_cd";
    private static final String TRIANA_RIPTIDERUSH_CD = "triana_riptiderush_cd";

    public static ItemAttributeModifiers createAttributeModifiers() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                BASE_ATTACK_DAMAGE_ID,
                                11.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                BASE_ATTACK_SPEED_ID,
                                -2.4,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }
    private static boolean isUsingTriaina(ServerPlayer player) {
        ItemStack active = player.getMainHandItem();
        return active.getItem() instanceof Triaina && player.isUsingItem();
    }
    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide()) {
            ServerPlayer serverPlayer = (ServerPlayer) user;
            ItemStack stack = user.getItemInHand(hand);
            if (!Cooldowns.isOnCooldown(user, TRIAINA_TEMPEST_CD) && user.isShiftKeyDown()) {

                if (!user.isCreative()) Cooldowns.start(user, TRIAINA_TEMPEST_CD, 20 * 20, "Tempest");

                // Trigger wave
                WaveAbility.summonWave(serverPlayer, 2.0);

                return InteractionResult.SUCCESS;
            } else if (Cooldowns.isOnCooldown(user, TRIAINA_TEMPEST_CD) && user.isShiftKeyDown()) {
                CooldownMessages.cooldownActiveMessage(serverPlayer, TRIAINA_TEMPEST_CD,
                        "Tempest is on cooldown!");
                return InteractionResult.FAIL;
            }
            if (!Cooldowns.isOnCooldown(user, TRIANA_RIPTIDERUSH_CD)) {

                if (user.pick(2, 0, false).getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                    return InteractionResult.PASS;
                }
                if (!(user.gameMode().isCreative())) {
                    Cooldowns.start(user, TRIANA_RIPTIDERUSH_CD, 200);
                }
                WaveAbility.summonWave(serverPlayer, -1.5);
                Dash.dashForward(user, 1.5f);
                DashState.start((ServerPlayer) user, 15, ParticleTypes.FALLING_WATER);

                world.playSound(
                        null,
                        user.getX(), user.getY(), user.getZ(),
                        SoundEvents.CONDUIT_ACTIVATE,
                        SoundSource.PLAYERS,
                        1.0F, // volume
                        1.0F  // pitch
                );
                user.startAutoSpinAttack(15, 5.0f, stack);
                return InteractionResult.SUCCESS;
            } else if (Cooldowns.isOnCooldown(user, TRIANA_RIPTIDERUSH_CD)) {
                CooldownMessages.cooldownActiveMessage(serverPlayer, TRIAINA_TEMPEST_CD,
                        "Riptide Rush is on Cooldown");
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.SUCCESS;
    }
    private static ItemStack getHeldEnyalios(Player player) {
        if (player.getActiveItem().getItem() instanceof Triaina)
            return player.getActiveItem();
        return null;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide()) return;
        if (!(attacker instanceof Player player)) return;
        if (!(stack.getItem() instanceof Triaina)) return;

        // First hit → start combo
        if (!Cooldowns.isOnCooldown(player, TRIAINA_COMBO_TIMER)) {
            Cooldowns.start(player, TRIAINA_COMBO_TIMER, 20 * 10); // 10s window
            Cooldowns.setInt(player, TRIAINA_COMBO_HITS, 1);
            return;
        }

        // Combo already active
        int hits = Cooldowns.getInt(player, TRIAINA_COMBO_HITS) + 1;
        Cooldowns.setInt(player, TRIAINA_COMBO_HITS, hits);

        // Third hit → knockback
        if (hits >= 3) {
            applyComboKnockback(player, target);
            target.hurt( player.damageSources().playerAttack(player), 5);
            // Reset combo
            Cooldowns.clear(player, TRIAINA_COMBO_TIMER);
            Cooldowns.clear(player, TRIAINA_COMBO_HITS);
        }
    }
    private static void applyComboKnockback(Player player, LivingEntity target) {
        Vec3 direction = target.getDeltaMovement()
                .subtract(player.getDeltaMovement())
                .normalize();
        target.hurt( target.damageSources().playerAttack(player), 2f);
        Vec3 velocity = new Vec3(
                direction.x * 1.2,
                0.45,
                direction.z * 1.2
        );

        target.push(velocity);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
    }


    public static void applyEffects(){
        // Sea God's Boon
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!hasTriania(player)) continue;
                player.addEffect(
                        new MobEffectInstance(
                                MobEffects.DOLPHINS_GRACE,
                                40,
                                2,
                                true,
                                false,
                                false
                        )
                );
                player.addEffect(
                        new MobEffectInstance(
                                MobEffects.WATER_BREATHING,
                                40,
                                2,
                                true,
                                false,
                                false
                        )
                );
            }
        });
    }

    private static boolean hasTriania(ServerPlayer player) {

        boolean hasItem = false;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(ModItems.TRIAINA)) {
                hasItem = true;
                break;
            }
        }

        return hasItem;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return MODEL;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.pantheon.triaina");
    }
    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.STICK;
    }

    @Override
    public String getGlowColor() {
        return "#55FFFF";
    }
}
